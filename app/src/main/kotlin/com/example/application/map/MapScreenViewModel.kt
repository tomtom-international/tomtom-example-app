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

package com.example.application.map

import android.util.Log
import androidx.annotation.RestrictTo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.application.common.PlaceDetails
import com.example.application.common.tts.TextToSpeechEngine
import com.example.application.horizon.DefaultHorizonUpdatedListener
import com.example.application.horizon.element.UpcomingHorizonElements
import com.example.application.map.model.MapScreenAction
import com.example.application.map.model.MapScreenUiState
import com.example.application.map.model.MapScreenUiState.ErrorState
import com.example.application.map.model.MapScreenUiState.ErrorState.MapStyleError
import com.example.application.map.model.MapScreenUiState.ErrorState.RoutingError
import com.example.application.map.model.MapScreenUiState.ErrorState.SearchError
import com.example.application.map.model.Scenario
import com.example.application.map.model.Scenario.FREE_DRIVING
import com.example.application.map.model.Scenario.GUIDANCE
import com.example.application.map.model.Scenario.HOME
import com.example.application.map.model.Scenario.POI_FOCUS
import com.example.application.map.model.Scenario.ROUTE_PREVIEW
import com.example.application.map.scenarios.freedriving.FreeDrivingDetector
import com.example.application.map.scenarios.freedriving.FreeDrivingManager
import com.example.application.map.scenarios.guidance.BetterRouteProposal
import com.example.application.map.scenarios.guidance.BetterRouteProposalHandler
import com.example.application.map.scenarios.guidance.GuidanceUpdatesHandler
import com.example.application.map.scenarios.guidance.NextInstruction
import com.example.application.search.SearchResultItemContent
import com.tomtom.sdk.common.configuration.SdkContext
import com.tomtom.sdk.location.GeoLocation
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.location.OnLocationUpdateListener
import com.tomtom.sdk.map.display.MapLocationInfrastructure
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.camera.CameraTrackingMode
import com.tomtom.sdk.map.display.camera.CameraTrackingMode.Companion.FollowRouteDirection
import com.tomtom.sdk.map.display.camera.CameraTrackingMode.Companion.RouteOverview
import com.tomtom.sdk.map.display.camera.InitialCameraOptions
import com.tomtom.sdk.map.display.compose.model.MapDisplayInfrastructure
import com.tomtom.sdk.map.display.style.StandardStyles
import com.tomtom.sdk.map.display.style.StyleDescriptor
import com.tomtom.sdk.navigation.LocationContextUpdatedListener
import com.tomtom.sdk.navigation.NavigationOptions
import com.tomtom.sdk.navigation.TomTomNavigation
import com.tomtom.sdk.navigation.guidance.LaneGuidance
import com.tomtom.sdk.navigation.horizon.buildHorizonOptions
import com.tomtom.sdk.navigation.horizon.elements.hazard.HazardElementType
import com.tomtom.sdk.navigation.horizon.elements.safetylocation.SafetyLocationElementType
import com.tomtom.sdk.navigation.horizon.elements.traffic.TrafficElementType
import com.tomtom.sdk.navigation.locationcontext.LocationContext
import com.tomtom.sdk.navigation.progress.RouteProgress
import com.tomtom.sdk.search.common.error.SearchFailure
import com.tomtom.sdk.search.reversegeocoder.ReverseGeocoder
import com.tomtom.sdk.search.reversegeocoder.ReverseGeocoderCallback
import com.tomtom.sdk.search.reversegeocoder.ReverseGeocoderOptions
import com.tomtom.sdk.search.reversegeocoder.ReverseGeocoderResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/**
 * Coordinates map screen state, location/camera control, POI data, route preview, and turn-by-turn guidance.
 */
@Suppress("detekt:LongParameterList")
class MapScreenViewModel(
    sdkContext: SdkContext,
    private val defaultLocationProvider: LocationProvider,
    private val reverseGeocoder: ReverseGeocoder,
    private val navigation: TomTomNavigation,
    private val freeDrivingManager: FreeDrivingManager,
    private val onClearMap: () -> Unit,
    private val onCheckLocationPermission: () -> Boolean,
    private val textToSpeechEngine: TextToSpeechEngine,
) : ViewModel() {
    private val _mapDisplayInfrastructure = MutableStateFlow(
        MapDisplayInfrastructure(
            sdkContext = sdkContext,
        ) {
            locationInfrastructure = MapLocationInfrastructure {
                locationProvider = defaultLocationProvider
            }
        },
    )
    val mapDisplayInfrastructure: StateFlow<MapDisplayInfrastructure> = _mapDisplayInfrastructure

    // StateFlow that represents the current state of the map screen.
    private val _mapScreenUiState: MutableStateFlow<MapScreenUiState> = MutableStateFlow(MapScreenUiState(HOME))
    val mapScreenUiState: StateFlow<MapScreenUiState> = _mapScreenUiState.asStateFlow()

    val styleDescriptor: StateFlow<StyleDescriptor> = mapScreenUiState
        .map { state ->
            when {
                state.isDrivingScenario() -> StandardStyles.TomTomOrbisMaps.DRIVING
                else -> StandardStyles.TomTomOrbisMaps.BROWSING
            }
        }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = StandardStyles.TomTomOrbisMaps.BROWSING,
        )

    private val _errorState: MutableStateFlow<ErrorState?> = MutableStateFlow(null)
    val errorState: StateFlow<ErrorState?> = _errorState.asStateFlow()

    private val _userLocation = MutableStateFlow(defaultLocationProvider.lastKnownLocation?.position)
    val userLocation: StateFlow<GeoPoint?> = _userLocation.asStateFlow()

    private val _cameraOptions = MutableStateFlow<CameraOptions?>(null)
    val cameraOptions: StateFlow<CameraOptions?> = _cameraOptions.asStateFlow()

    private var _isBottomSheetExpanded = MutableStateFlow(false)
    val isBottomSheetExpanded: StateFlow<Boolean> = _isBottomSheetExpanded.asStateFlow()

    val initialCameraOptions: InitialCameraOptions = defaultLocationProvider.lastKnownLocation?.position?.let {
        InitialCameraOptions.LocationBased(position = it, zoom = INITIAL_CAMERA_ZOOM)
    } ?: InitialCameraOptions.LocationBased(position = DEFAULT_LOCATION)

    private val _locationProvider = MutableStateFlow(defaultLocationProvider)
    val locationProvider: StateFlow<LocationProvider> = _locationProvider.asStateFlow()

    private val onLocationUpdateListener = OnLocationUpdateListener { location: GeoLocation ->
        if (!mapScreenUiState.value.isInteractiveMode) {
            _userLocation.tryEmit(location.position)
        }
    }

    // Guidance listeners/signals live in GuidanceUpdatesHandler; arrival is reported back to keep screen state here.
    private val guidanceUpdatesHandler = GuidanceUpdatesHandler(
        navigation = navigation,
        textToSpeechEngine = textToSpeechEngine,
        onDestinationArrival = {
            stopGuidance()
            _mapScreenUiState.update {
                it.copy(
                    scenario = Scenario.DESTINATION_ARRIVAL,
                    markerCoordinates = mapScreenUiState.value.placeDetails?.place?.coordinate,
                )
            }
        },
    )

    // Re-exposed from GuidanceUpdatesHandler so the UI reads guidance state from one place.
    val nextInstruction: StateFlow<NextInstruction?> get() = guidanceUpdatesHandler.nextInstruction
    val laneGuidance: StateFlow<LaneGuidance?> get() = guidanceUpdatesHandler.laneGuidance
    val routeProgress: StateFlow<RouteProgress?> get() = guidanceUpdatesHandler.routeProgress

    private val betterRouteProposalHandler = BetterRouteProposalHandler(
        navigation = navigation,
        routeProgress = guidanceUpdatesHandler.routeProgress,
    )

    val betterRouteProposal: StateFlow<BetterRouteProposal?> get() = betterRouteProposalHandler.proposal

    private val _upcomingHorizonElements = MutableStateFlow<UpcomingHorizonElements?>(null)
    val upcomingHorizonElements: StateFlow<UpcomingHorizonElements?> = _upcomingHorizonElements

    private val _locationContext = MutableStateFlow<LocationContext?>(null)
    val locationContext: StateFlow<LocationContext?> = _locationContext.asStateFlow()

    private val locationContextUpdatedListener = LocationContextUpdatedListener { locationContext: LocationContext ->
        if (locationContext.address != null) {
            _locationContext.tryEmit(locationContext)
        }
    }

    private val horizonUpdatedListener = DefaultHorizonUpdatedListener { newHorizonElements ->
        _upcomingHorizonElements.update { newHorizonElements }
    }

    private val freeDrivingDetector = FreeDrivingDetector(freeDrivingManager, viewModelScope)

    init {
        if (onCheckLocationPermission()) {
            startLocationProvider()
        }
    }

    fun startLocationProvider() {
        defaultLocationProvider.addOnLocationUpdateListener(onLocationUpdateListener)
        defaultLocationProvider.enable()
    }

    fun dispatchAction(action: MapScreenAction) {
        when (action) {
            is MapScreenAction.ClearMap -> clearMap()
            is MapScreenAction.ClearSearch -> {
                cleanPoiPlaces()
                recenterMap()
            }

            is MapScreenAction.RecenterMap -> recenterMap()
            is MapScreenAction.StartInteractiveMode -> startInteractiveMode()
            is MapScreenAction.ShowPoiCategorySearchResultFocus -> showPoiCategoryFocus(action.poiResults)
            is MapScreenAction.ShowSearchResultFocus -> showPoiFocus(action.placeDetails)
            is MapScreenAction.ShowPoiFocus -> getPoiData(action.geoPoint) { showPoiFocus(it) }
            is MapScreenAction.ShowRenderedPoiInfo -> getPoiData(action.geoPoint) {
                showPoiFocus(it, markerPosition = action.geoPoint, renderedPoiName = action.poiName)
            }
            is MapScreenAction.CleanRoutePreview -> cleanRoutePreview(action.onClearRoutes)

            is MapScreenAction.ToggleBottomSheet -> toggleBottomSheet(isExpanded = action.isExpanded)

            is MapScreenAction.ShowRoutePreview -> {
                _mapScreenUiState.update {
                    it.copy(
                        scenario = ROUTE_PREVIEW,
                        destinationDetails = _mapScreenUiState.value.placeDetails?.copy(),
                    )
                }
            }

            is MapScreenAction.ShowRoutingFailure -> {
                Log.e(TAG, "Route planning failed with error: ${action.routingFailure}")
                _errorState.update { RoutingError }
            }

            is MapScreenAction.ShowSearchFailure -> {
                Log.e(TAG, "Search failed with error: ${action.searchFailure}")
                _errorState.update { SearchError }
            }

            is MapScreenAction.StartGuidance -> startGuidance(action.navigationOptions)
            is MapScreenAction.StopGuidance -> {
                stopGuidance()
                clearMap()
            }

            is MapScreenAction.AcceptBetterRouteProposal -> betterRouteProposalHandler.accept()
            is MapScreenAction.DismissBetterRouteProposal -> betterRouteProposalHandler.dismiss()

            is MapScreenAction.ToggleCameraTrackingMode -> toggleCameraTrackingMode()

            is MapScreenAction.ShowMapStyleFailure -> {
                Log.e(TAG, "Map style loading failed with cause: ${action.cause}")
                _errorState.update { MapStyleError(action.cause) }
            }
        }
    }

    fun onResume() {
        detectFreeDriving()
    }

    fun onPause() {
        freeDrivingDetector.stopDetecting()
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    public override fun onCleared() {
        super.onCleared()
        if (mapScreenUiState.value.scenario == FREE_DRIVING) {
            stopFreeDriving()
        } else if (mapScreenUiState.value.scenario == GUIDANCE) {
            stopGuidance()
        }
        defaultLocationProvider.removeOnLocationUpdateListener(onLocationUpdateListener)
        defaultLocationProvider.disable()
        textToSpeechEngine.shutdown()
    }

    private fun clearMap() {
        _mapScreenUiState.update {
            it.copy(
                scenario = HOME,
                isInteractiveMode = false,
                markerCoordinates = null,
                placeDetails = null,
            )
        }

        onClearMap()
        if (mapScreenUiState.value.poiPlaces.isNotEmpty()) {
            zoomToAllMarkers()
        } else {
            recenterMap()
        }
        detectFreeDriving()
    }

    fun toggleBottomSheet(isExpanded: Boolean? = null) {
        if (isExpanded == null) {
            _isBottomSheetExpanded.update { !it }
        } else {
            _isBottomSheetExpanded.update { isExpanded }
        }
    }

    private fun zoomToAllMarkers() {
        _mapScreenUiState.update { it.copy(zoomToAllMarkers = !_mapScreenUiState.value.zoomToAllMarkers) }
    }

    private fun startInteractiveMode() {
        _mapScreenUiState.update { it.copy(isInteractiveMode = true, cameraTrackingMode = CameraTrackingMode.None) }
    }

    private fun recenterMap() {
        _mapScreenUiState.update {
            it.copy(
                isInteractiveMode = false,
                cameraTrackingMode = mapScreenUiState.value.getRecenterCameraTrackingMode(),
            )
        }

        mapScreenUiState.value.getRecenterCameraOptions(locationProvider = defaultLocationProvider)?.let {
            _cameraOptions.tryEmit(it)
        }
    }

    fun clearErrorState() {
        _errorState.update { null }
    }

    private fun getPoiData(
        geoPoint: GeoPoint,
        onGetPoiDataSuccess: (PlaceDetails) -> Unit,
    ) {
        val reverseGeocoderOptions = ReverseGeocoderOptions(position = geoPoint)
        reverseGeocoder.reverseGeocode(
            reverseGeocoderOptions,
            object : ReverseGeocoderCallback {
                override fun onSuccess(result: ReverseGeocoderResponse) {
                    result.places.firstOrNull()?.let { firstPlace ->
                        onGetPoiDataSuccess(PlaceDetails(firstPlace.place))
                    }
                }

                override fun onFailure(failure: SearchFailure) {
                    Log.e(TAG, "Reverse geocoding failed with error: $failure")
                    _errorState.update { SearchError }
                }
            },
        )
    }

    // Update the map state so that all the POI found are shown to the user
    private fun showPoiCategoryFocus(searchResults: List<SearchResultItemContent>) {
        Log.d(TAG, "showPoiCategoryFocus: $searchResults")
        _mapScreenUiState.update {
            it.copy(
                poiPlaces = searchResults.map { searchResult -> searchResult.placeDetails },
                cameraTrackingMode = CameraTrackingMode.None,
            )
        }
        zoomToAllMarkers()
    }

    // Remove all the results from the poiPlaces list so that the map is cleaned
    fun cleanPoiPlaces() {
        _mapScreenUiState.update { it.copy(poiPlaces = emptyList()) }
    }

    // Update the map state so that the camera focuses on the selected place
    private fun showPoiFocus(
        placeDetails: PlaceDetails,
        markerPosition: GeoPoint? = null,
        renderedPoiName: String? = null,
    ) {
        stopFreeDriving()
        freeDrivingDetector.stopDetecting()
        val resolvedPlaceDetails = if (renderedPoiName != null) {
            placeDetails.copy(renderedPoiName = renderedPoiName)
        } else {
            placeDetails
        }
        _mapScreenUiState.update {
            it.copy(
                scenario = POI_FOCUS,
                placeDetails = resolvedPlaceDetails,
                markerCoordinates = markerPosition ?: placeDetails.place.coordinate,
            )
        }
        recenterMap()
    }

    private fun cleanRoutePreview(onClearRoutes: () -> Unit) {
        onClearRoutes()
        mapScreenUiState.value.placeDetails?.let { showPoiFocus(it) }
    }

    // Location context and horizon listeners are shared by guidance and free driving.
    private fun startAmbientObservation() {
        navigation.addLocationContextUpdatedListener(locationContextUpdatedListener)
        navigation.addHorizonUpdatedListener(
            options = buildHorizonOptions(
                elementTypes = listOf(
                    HazardElementType,
                    TrafficElementType,
                    SafetyLocationElementType,
                ),
            ),
            listener = horizonUpdatedListener,
        )
    }

    private fun stopAmbientObservation() {
        navigation.removeLocationContextUpdatedListener(locationContextUpdatedListener)
        navigation.removeHorizonUpdatedListener(horizonUpdatedListener)
    }

    // Start turn-by-turn navigation with optional simulation
    private fun startGuidance(navigationOptions: NavigationOptions) {
        stopGuidance()

        toggleBottomSheet(false)

        startAmbientObservation()
        guidanceUpdatesHandler.start()
        betterRouteProposalHandler.start()

        navigation.start(navigationOptions)
        _mapScreenUiState.update {
            it.copy(
                scenario = GUIDANCE,
                cameraTrackingMode = FollowRouteDirection,
                placeDetails = null,
            )
        }
    }

    private fun stopGuidance() {
        guidanceUpdatesHandler.stop()
        betterRouteProposalHandler.stop()
        stopAmbientObservation()
        navigation.stop()
    }

    // Starts free driving scenario
    private fun startFreeDriving() {
        _mapScreenUiState.update { it.copy(scenario = FREE_DRIVING) }
        recenterMap()

        startAmbientObservation()
        navigation.start()
    }

    // Ends free driving scenario
    private fun stopFreeDriving() {
        stopAmbientObservation()
        navigation.stop()
    }

    // Observes the driving state and lets FreeDrivingDetector drive the scenario transitions.
    private fun detectFreeDriving() {
        freeDrivingDetector.startDetecting(
            scenario = mapScreenUiState.value.scenario,
            locationProvider = locationProvider.value,
            onStartFreeDriving = ::startFreeDriving,
            onStopFreeDriving = {
                stopFreeDriving()
                clearMap()
            },
        )
    }

    private fun toggleCameraTrackingMode() {
        _mapScreenUiState.update {
            it.copy(
                isInteractiveMode = false,
                cameraTrackingMode = if (it.cameraTrackingMode == FollowRouteDirection) {
                    RouteOverview
                } else {
                    FollowRouteDirection
                },
            )
        }
    }

    fun updateSafeAreaTopPadding(topPadding: Int) {
        _mapScreenUiState.update { it.copy(safeAreaTopPadding = topPadding) }
    }

    fun updateSafeAreaBottomPadding(bottomPadding: Int) {
        _mapScreenUiState.update { it.copy(safeAreaBottomPadding = bottomPadding) }
    }

    companion object {
        private const val TAG = "MapScreenViewModel"

        private val DEFAULT_LOCATION = GeoPoint(0.0, 0.0)
        private const val INITIAL_CAMERA_ZOOM = 12.0

        val SDK_CONTEXT = object : CreationExtras.Key<SdkContext> {}
        val DEFAULT_LOCATION_PROVIDER_KEY = object : CreationExtras.Key<LocationProvider> {}
        val REVERSE_GEOCODER_KEY = object : CreationExtras.Key<ReverseGeocoder> {}
        val NAVIGATION_KEY = object : CreationExtras.Key<TomTomNavigation> {}
        val FREE_DRIVING_MANAGER_KEY = object : CreationExtras.Key<FreeDrivingManager> {}

        val ON_CLEAR_MAP_KEY = object : CreationExtras.Key<() -> Unit> {}
        val ON_CHECK_LOCATION_PERMISSION = object : CreationExtras.Key<() -> Boolean> {}
        val TEXT_TO_SPEECH_ENGINE_KEY = object : CreationExtras.Key<TextToSpeechEngine> {}

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MapScreenViewModel(
                    sdkContext = this[SDK_CONTEXT] as SdkContext,
                    defaultLocationProvider = this[DEFAULT_LOCATION_PROVIDER_KEY] as LocationProvider,
                    reverseGeocoder = this[REVERSE_GEOCODER_KEY] as ReverseGeocoder,
                    navigation = this[NAVIGATION_KEY] as TomTomNavigation,
                    freeDrivingManager = this[FREE_DRIVING_MANAGER_KEY] as FreeDrivingManager,
                    onClearMap = this[ON_CLEAR_MAP_KEY] as () -> Unit,
                    onCheckLocationPermission = this[ON_CHECK_LOCATION_PERMISSION] as () -> Boolean,
                    textToSpeechEngine = this[TEXT_TO_SPEECH_ENGINE_KEY] as TextToSpeechEngine,
                )
            }
        }
    }
}
