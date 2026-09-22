/*
Copyright 2026 TomTom International BV.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.example.automotive.carapp

import android.util.Log
import androidx.car.app.model.DateTimeWithZone
import androidx.car.app.navigation.NavigationManager
import androidx.car.app.navigation.NavigationManagerCallback
import androidx.car.app.navigation.model.Step
import androidx.car.app.navigation.model.TravelEstimate
import androidx.car.app.navigation.model.Trip
import androidx.lifecycle.ViewModel
import com.example.automotive.carapp.Scenario.DESTINATION_ARRIVAL
import com.example.automotive.carapp.Scenario.GUIDANCE
import com.example.automotive.carapp.Scenario.HOME
import com.example.automotive.carapp.Scenario.ROUTE_PREVIEW
import com.example.automotive.carapp.cluster.ClusterData
import com.example.automotive.common.PlaceDetails
import com.example.automotive.common.extension.toCarManeuver
import com.example.automotive.map.camera.CameraController
import com.example.automotive.map.camera.CameraController.Companion.DEFAULT_POSITION
import com.example.automotive.vehicle.VehicleRepository
import com.tomtom.quantity.Distance
import com.tomtom.quantity.Energy
import com.tomtom.sdk.common.Cancellable
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.navigation.DestinationArrivalListener
import com.tomtom.sdk.navigation.GuidanceUpdatedListener
import com.tomtom.sdk.navigation.NavigationOptions
import com.tomtom.sdk.navigation.ProgressUpdatedListener
import com.tomtom.sdk.navigation.RoutePlan
import com.tomtom.sdk.navigation.TomTomNavigation
import com.tomtom.sdk.navigation.guidance.GuidanceAnnouncement
import com.tomtom.sdk.navigation.guidance.InstructionPhase
import com.tomtom.sdk.navigation.guidance.instruction.GuidanceInstruction
import com.tomtom.sdk.navigation.progress.RouteProgress
import com.tomtom.sdk.routing.RoutePlanner
import com.tomtom.sdk.routing.RoutePlanningCallback
import com.tomtom.sdk.routing.RoutePlanningResponse
import com.tomtom.sdk.routing.RoutingFailure
import com.tomtom.sdk.routing.buildEvRoutePlanningOptions
import com.tomtom.sdk.routing.options.ChargingOptions
import com.tomtom.sdk.routing.options.Itinerary
import com.tomtom.sdk.routing.options.RoutePlanningOptions
import com.tomtom.sdk.search.common.error.SearchFailure
import com.tomtom.sdk.search.reversegeocoder.ReverseGeocoder
import com.tomtom.sdk.search.reversegeocoder.ReverseGeocoderCallback
import com.tomtom.sdk.search.reversegeocoder.ReverseGeocoderResponse
import com.tomtom.sdk.search.reversegeocoder.buildReverseGeocoderOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.TimeZone
import androidx.car.app.model.Distance as CarDistance

/**
 * Manages EV route planning state and operations, and acts as the single source of truth
 * for all state needed to render MainScreen.
 *
 * Must only be created after the TomTom SDK has been initialized: all SDK components are
 * required at construction time.
 *
 * @param routePlanner The instance used for route planning operations.
 * @param reverseGeocoder The instance used to resolve map clicks into places.
 * @param navigation The navigation instance driving guidance.
 * @param locationProvider Provider of the current location; enabled once the location
 *   permission is confirmed via [setPermissionsGranted].
 * @param navigationManager Car app service used to report navigation state to the host.
 * @param vehicleRepository Repository for vehicle data.
 * @param clusterData Container for cluster map state flows.
 */
class MainViewModel(
    private val routePlanner: RoutePlanner,
    private val reverseGeocoder: ReverseGeocoder,
    private val navigation: TomTomNavigation,
    private val locationProvider: LocationProvider,
    private val navigationManager: NavigationManager,
    private val vehicleRepository: VehicleRepository,
    private val clusterData: ClusterData,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainScreenUIState())
    val uiState: StateFlow<MainScreenUIState> = _uiState.asStateFlow()

    private val _routeProgress = MutableStateFlow<RouteProgress?>(null)
    val routeProgress: StateFlow<RouteProgress?> = _routeProgress.asStateFlow()

    private var distanceToNextInstruction: Distance? = null

    private val progressUpdatedListener = ProgressUpdatedListener { routeProgress ->
        _routeProgress.tryEmit(routeProgress)
    }

    private val guidanceUpdatedListener = object : GuidanceUpdatedListener {
        override fun onDistanceToNextInstructionChanged(
            distance: Distance,
            instructions: List<GuidanceInstruction>,
            currentPhase: InstructionPhase,
        ) {
            distanceToNextInstruction = distance
            instructions.firstOrNull()?.let { navigationManager.updateTrip(buildTrip(it)) }
        }

        override fun onInstructionsChanged(instructions: List<GuidanceInstruction>) {
            val firstInstruction = instructions.firstOrNull()
            distanceToNextInstruction = firstInstruction?.routeOffset
            firstInstruction?.let { navigationManager.updateTrip(buildTrip(it)) }
        }

        override fun onAnnouncementGenerated(
            announcement: GuidanceAnnouncement,
            shouldPlay: Boolean,
        ) {
            Log.d(TAG, "Guidance announcement: $announcement")
        }
    }

    private val destinationArrivalListener = DestinationArrivalListener { _ ->
        stopGuidance()
        _uiState.update { it.copy(scenario = DESTINATION_ARRIVAL) }
        recenterCamera()
    }

    private var currentRequestCallback: RoutePlanningCallback? = null

    private var routePlannerCancellable: Cancellable? = null
    private var reverseGeocoderCancellable: Cancellable? = null

    private var currentRoutePlanningOptions: RoutePlanningOptions? = null

    private var cameraController: CameraController? = null

    init {
        navigationManager.setNavigationManagerCallback(object : NavigationManagerCallback {
            override fun onStopNavigation() {
                super.onStopNavigation()
                stopGuidance()
            }
        })
    }

    /**
     * Sets permissions granted state from the UI layer and enables the location provider on the first grant.
     */
    fun setPermissionsGranted(granted: Boolean) {
        if (granted && !uiState.value.permissionsGranted) {
            locationProvider.enable()
        }
        _uiState.update { it.copy(permissionsGranted = granted) }
    }

    /** Sets location enabled state from the UI layer. */
    fun setLocationEnabled(enabled: Boolean) {
        _uiState.update { it.copy(locationEnabled = enabled) }
    }

    fun setCameraController(cameraController: CameraController) {
        this.cameraController = cameraController
    }

    /** Handles a click on the map by reverse geocoding the tapped point. */
    fun onMapClick(geoPoint: GeoPoint) {
        if (uiState.value.scenario != GUIDANCE) {
            Log.d(TAG, "Map Click at: $geoPoint")
            val options = buildReverseGeocoderOptions(position = geoPoint)
            reverseGeocoderCancellable?.cancel()
            reverseGeocoderCancellable = reverseGeocoder.reverseGeocode(
                options,
                object : ReverseGeocoderCallback {
                    override fun onSuccess(result: ReverseGeocoderResponse) {
                        Log.d(TAG, "Reverse geocoding result: ${result.places}")
                        result.places.firstOrNull()?.let { firstPlace ->
                            _uiState.update {
                                it.copy(
                                    scenario = Scenario.POI_FOCUS,
                                    placeDetails = PlaceDetails(firstPlace.place),
                                )
                            }
                            recenterCamera()
                        }
                    }

                    override fun onFailure(failure: SearchFailure) {
                        Log.e(TAG, "Reverse geocoding failed: $failure")
                        _uiState.update { it.copy(failureMessage = failure.message) }
                    }
                },
            )
        }
    }

    /** Plans a route for an EV vehicle from the current location to the given place details location. */
    fun planRoute(placeDetails: PlaceDetails) {
        _uiState.update { it.copy(isLoading = true) }

        vehicleRepository.configureVehicle()

        val origin = locationProvider.lastKnownLocation?.position ?: DEFAULT_POSITION
        val destination = placeDetails.place.coordinate
        val routePlanningOptions = buildEvRoutePlanningOptions(
            Itinerary(origin, destination),
            chargingOptions = ChargingOptions(
                minChargeAtDestination = INITIAL_MIN_CHARGE,
                minChargeAtChargingStops = INITIAL_MIN_CHARGE,
            ),
        )

        val callback = object : RoutePlanningCallback {
            override fun onSuccess(result: RoutePlanningResponse) {
                if (currentRequestCallback == this) {
                    _uiState.update {
                        it.copy(
                            scenario = ROUTE_PREVIEW,
                            routes = result.routes,
                            selectedRoute = result.routes.firstOrNull(),
                            isLoading = false,
                            placeDetails = null,
                        )
                    }
                    currentRequestCallback = null
                }
            }

            override fun onFailure(failure: RoutingFailure) {
                if (currentRequestCallback == this) {
                    Log.e(TAG, "Route planning failed: $failure")
                    _uiState.update {
                        it.copy(
                            routes = emptyList(),
                            selectedRoute = null,
                            isLoading = false,
                        )
                    }
                    currentRequestCallback = null
                }
            }
        }

        currentRequestCallback = callback
        currentRoutePlanningOptions = routePlanningOptions

        routePlannerCancellable?.cancel()
        routePlannerCancellable = routePlanner.planRoute(routePlanningOptions, callback)
    }

    /** Dismisses every panel. */
    fun clearClicked() {
        stopGuidance()
        _uiState.update {
            it.copy(
                scenario = HOME,
                placeDetails = null,
                routes = emptyList(),
                selectedRoute = null,
                isLoading = false,
            )
        }
        recenterCamera()
    }

    /** Clears the failure message after it has been shown. */
    fun dismissFailureMessage() {
        _uiState.update { it.copy(failureMessage = null) }
    }

    /** Starts guidance for the currently selected route. */
    fun startGuidance() {
        stopGuidance()
        uiState.value.selectedRoute?.let { selectedRoute ->
            currentRoutePlanningOptions?.let { planningOptions ->
                val navigationOptions = NavigationOptions(RoutePlan(selectedRoute, planningOptions))
                navigation.addProgressUpdatedListener(progressUpdatedListener)
                navigation.addGuidanceUpdatedListener(guidanceUpdatedListener)
                navigation.addDestinationArrivalListener(destinationArrivalListener)
                navigation.start(navigationOptions)
                navigationManager.navigationStarted()
                clusterData.isActiveGuidance.value = true
                clusterData.clusterRoutes.value = uiState.value.routes
                clusterData.clusterSelectedRoute.value = selectedRoute
                _uiState.update { it.copy(scenario = GUIDANCE) }
                recenterCamera()
            }
        }
    }

    /** Stops guidance and clears the navigation listeners. */
    private fun stopGuidance() {
        navigation.removeProgressUpdatedListener(progressUpdatedListener)
        navigation.removeGuidanceUpdatedListener(guidanceUpdatedListener)
        navigation.removeDestinationArrivalListener(destinationArrivalListener)
        navigation.stop()
        if (uiState.value.scenario == GUIDANCE) {
            navigationManager.navigationEnded()
        }
        clusterData.isActiveGuidance.value = false
        clusterData.clusterRoutes.value = emptyList()
        clusterData.clusterSelectedRoute.value = null
        distanceToNextInstruction = null
    }

    private fun buildTrip(instruction: GuidanceInstruction): Trip {
        val stepMeters = distanceToNextInstruction?.inMeters()
        val remainingMs = _routeProgress.value?.remainingTime?.inWholeMilliseconds

        if (stepMeters == null || remainingMs == null) {
            return Trip.Builder().setLoading(true).build()
        } else {
            val maneuver = instruction.toCarManeuver()
            val cue = instruction.nextSignificantRoad?.name ?: instruction.signpost?.towardName
                ?: instruction.signpost?.exitName ?: ""
            val step = Step.Builder(cue).setManeuver(maneuver).build()

            val stepTravelEstimate = TravelEstimate.Builder(
                CarDistance.create(stepMeters, CarDistance.UNIT_METERS),
                DateTimeWithZone.create(System.currentTimeMillis() + remainingMs, TimeZone.getDefault()),
            ).build()

            return Trip.Builder()
                .addStep(step, stepTravelEstimate)
                .setLoading(false)
                .build()
        }
    }

    /** Animates the camera back to the center depending on [com.example.automotive.carapp.Scenario]. */
    fun recenterCamera() {
        val trackingMode = uiState.value.scenario.defaultTrackingMode()
        Log.d(TAG, "Camera tracking mode for Scenario ${uiState.value.scenario}: $trackingMode")
        cameraController?.setCameraTracking(trackingMode)

        uiState.value.scenario.defaultCameraOptions(
            place = uiState.value.placeDetails,
            locationProvider = locationProvider,
        )?.let { cameraOptions ->
            Log.d(TAG, "Camera options: $cameraOptions")
            cameraController?.animateCamera(cameraOptions)
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (uiState.value.scenario == GUIDANCE) {
            stopGuidance()
        }
        locationProvider.disable()
        currentRequestCallback = null
        currentRoutePlanningOptions = null
    }

    companion object {
        private const val TAG = "MainViewModel"

        private const val INITIAL_MIN_CHARGE_KWH = 5.0
        private val INITIAL_MIN_CHARGE: Energy = Energy.kilowattHours(INITIAL_MIN_CHARGE_KWH)
    }
}
