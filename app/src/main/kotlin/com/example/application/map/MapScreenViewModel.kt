/*
 * © 2025 TomTom NV. All rights reserved.
 *
 * This software is the proprietary copyright of TomTom NV and its subsidiaries and may be
 * used for internal evaluation purposes or commercial use strictly subject to separate
 * license agreement between you and TomTom NV. If you are the licensee, you are only permitted
 * to use this software in accordance with the terms of your license agreement. If you are
 * not the licensee, you are not authorized to use this software in any manner and should
 * immediately return or destroy it.
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
import com.example.BuildConfig
import com.example.application.common.PlaceDetails
import com.example.application.freedriving.FreeDrivingManager
import com.example.application.guidance.NextInstruction
import com.example.application.guidance.toManeuverType
import com.example.application.horizon.DefaultHorizonUpdatedListener
import com.example.application.horizon.element.UpcomingHorizonElements
import com.example.application.map.MapScreenUiState.ErrorState
import com.example.application.map.MapScreenUiState.ErrorState.RoutingError
import com.example.application.map.MapScreenUiState.ErrorState.SearchError
import com.example.application.map.MapScreenUiState.Scenario
import com.example.application.map.MapScreenUiState.Scenario.FREE_DRIVING
import com.example.application.map.MapScreenUiState.Scenario.GUIDANCE
import com.example.application.map.MapScreenUiState.Scenario.HOME
import com.example.application.map.MapScreenUiState.Scenario.POI_FOCUS
import com.example.application.map.MapScreenUiState.Scenario.ROUTE_PREVIEW
import com.example.application.search.SearchResultItemContent
import com.example.application.settings.data.SettingsRepository
import com.example.application.tts.TextToSpeechEngine
import com.tomtom.quantity.Distance
import com.tomtom.sdk.location.BetaLocationApi
import com.tomtom.sdk.location.GeoLocation
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.location.OnLocationUpdateListener
import com.tomtom.sdk.map.display.MapDataInfrastructure
import com.tomtom.sdk.map.display.MapLocationInfrastructure
import com.tomtom.sdk.map.display.annotation.BetaInitialCameraOptionsApi
import com.tomtom.sdk.map.display.annotation.BetaMapInfrastructureApi
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.camera.CameraTrackingMode
import com.tomtom.sdk.map.display.camera.CameraTrackingMode.Companion.FollowRouteDirection
import com.tomtom.sdk.map.display.camera.CameraTrackingMode.Companion.RouteOverview
import com.tomtom.sdk.map.display.camera.InitialCameraOptions
import com.tomtom.sdk.map.display.compose.BetaMapComposableApi
import com.tomtom.sdk.map.display.compose.model.MapInfrastructure
import com.tomtom.sdk.map.display.mapdatastore.common.MapDataStore
import com.tomtom.sdk.map.display.visualization.navigation.NavigationEventDispatcher
import com.tomtom.sdk.map.display.visualization.navigation.NavigationEventDispatcherFactory
import com.tomtom.sdk.navigation.DestinationArrivalListener
import com.tomtom.sdk.navigation.GuidanceUpdatedListener
import com.tomtom.sdk.navigation.LaneGuidanceUpdatedListener
import com.tomtom.sdk.navigation.LocationContextUpdatedListener
import com.tomtom.sdk.navigation.NavigationOptions
import com.tomtom.sdk.navigation.ProgressUpdatedListener
import com.tomtom.sdk.navigation.RoutePlan
import com.tomtom.sdk.navigation.TomTomNavigation
import com.tomtom.sdk.navigation.guidance.GuidanceAnnouncement
import com.tomtom.sdk.navigation.guidance.InstructionPhase
import com.tomtom.sdk.navigation.guidance.LaneGuidance
import com.tomtom.sdk.navigation.guidance.instruction.GuidanceInstruction
import com.tomtom.sdk.navigation.horizon.annotation.BetaHorizonOptionsCreationApi
import com.tomtom.sdk.navigation.horizon.annotation.BetaHorizonTrafficElementApi
import com.tomtom.sdk.navigation.horizon.createHorizonOptions
import com.tomtom.sdk.navigation.horizon.elements.safetylocation.SafetyLocationElementType
import com.tomtom.sdk.navigation.horizon.elements.traffic.TrafficElementType
import com.tomtom.sdk.navigation.locationcontext.LocationContext
import com.tomtom.sdk.navigation.progress.RouteProgress
import com.tomtom.sdk.routing.options.RoutePlanningOptions
import com.tomtom.sdk.routing.route.Route
import com.tomtom.sdk.routing.route.RouteStop
import com.tomtom.sdk.search.common.error.SearchFailure
import com.tomtom.sdk.search.reversegeocoder.ReverseGeocoder
import com.tomtom.sdk.search.reversegeocoder.ReverseGeocoderCallback
import com.tomtom.sdk.search.reversegeocoder.ReverseGeocoderOptions
import com.tomtom.sdk.search.reversegeocoder.ReverseGeocoderResponse
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Suppress("detekt:TooManyFunctions", "detekt:LongParameterList")
@OptIn(
    BetaInitialCameraOptionsApi::class,
    BetaMapComposableApi::class,
    BetaMapInfrastructureApi::class,
    BetaHorizonTrafficElementApi::class,
)
class MapScreenViewModel(
    mapDataStore: MapDataStore,
    private val defaultLocationProvider: LocationProvider,
    private val mapMatchedLocationProvider: LocationProvider,
    private val reverseGeocoder: ReverseGeocoder,
    private val navigation: TomTomNavigation,
    private val freeDrivingManager: FreeDrivingManager,
    private val settingsRepository: SettingsRepository,
    private val onClearMap: () -> Unit,
    private val onCheckLocationPermission: () -> Boolean,
    private val textToSpeechEngine: TextToSpeechEngine,
) : ViewModel() {
    private val navigationEventDispatcher: NavigationEventDispatcher = NavigationEventDispatcherFactory.create()

    val isTtpLogsEnabled: StateFlow<Boolean> = settingsRepository.settings.map { it.isNavSdkTtpLogsEnabled }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = false,
        )

    private val mapDataInfrastructure = MapDataInfrastructure(
        apiKey = BuildConfig.TOMTOM_API_KEY,
        dataStore = mapDataStore,
    )

    private val _mapInfrastructure = MutableStateFlow(
        MapInfrastructure(
            dataInfrastructure = mapDataInfrastructure,
        ) {
            locationInfrastructure = MapLocationInfrastructure {
                locationProvider = defaultLocationProvider
            }
        },
    )
    val mapInfrastructure: StateFlow<MapInfrastructure> = _mapInfrastructure

    // StateFlow that represents the current state of the map screen.
    private val _mapScreenUiState: MutableStateFlow<MapScreenUiState> = MutableStateFlow(MapScreenUiState(HOME))
    val mapScreenUiState: StateFlow<MapScreenUiState> = _mapScreenUiState.asStateFlow()

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

    private val _nextInstruction = MutableStateFlow<NextInstruction?>(null)
    val nextInstruction: StateFlow<NextInstruction?> = _nextInstruction

    private val _upcomingHorizonElements = MutableStateFlow<UpcomingHorizonElements?>(null)
    val upcomingHorizonElements: StateFlow<UpcomingHorizonElements?> = _upcomingHorizonElements

    private val _laneGuidance = MutableStateFlow<LaneGuidance?>(null)
    val laneGuidance: StateFlow<LaneGuidance?> = _laneGuidance.asStateFlow()

    private val _routeProgress = MutableStateFlow<RouteProgress?>(null)
    val routeProgress: StateFlow<RouteProgress?> = _routeProgress.asStateFlow()

    private val _locationContext = MutableStateFlow<LocationContext?>(null)
    val locationContext: StateFlow<LocationContext?> = _locationContext.asStateFlow()

    private val locationContextUpdatedListener = LocationContextUpdatedListener { locationContext: LocationContext ->
        if (locationContext.address != null) {
            _locationContext.tryEmit(locationContext)
        }
    }

    private val progressUpdatedListener = ProgressUpdatedListener { routeProgress ->
        _routeProgress.tryEmit(routeProgress)
    }

    private val destinationArrivalListener = DestinationArrivalListener { _ ->
        stopGuidance()
        _mapScreenUiState.update {
            it.copy(
                scenario = Scenario.DESTINATION_ARRIVAL,
                destinationMarker = mapScreenUiState.value.placeDetails?.place?.coordinate,
            )
        }
    }

    private val laneGuidanceUpdatedListener = object : LaneGuidanceUpdatedListener {
        override fun onLaneGuidanceEnded(laneGuidance: LaneGuidance) {
            Log.d(TAG, "Lane guidance ended: $laneGuidance")
            _laneGuidance.update { null }
        }

        override fun onLaneGuidanceStarted(laneGuidance: LaneGuidance) {
            Log.d(TAG, "Lane guidance started: $laneGuidance")
            _laneGuidance.update { laneGuidance }
        }
    }

    private val guidanceUpdatedListener = object : GuidanceUpdatedListener {
        override fun onAnnouncementGenerated(
            announcement: GuidanceAnnouncement,
            shouldPlay: Boolean,
        ) {
            Log.d(TAG, "Announcement: $announcement")

            if (shouldPlay) {
                textToSpeechEngine.playMessage(
                    message = announcement.plainTextMessage,
                    onError = { error -> Log.e(TAG, "Error playing announcement: $error") },
                )
            }
        }

        override fun onDistanceToNextInstructionChanged(
            distance: Distance,
            instructions: List<GuidanceInstruction>,
            currentPhase: InstructionPhase,
        ) {
            Log.d(TAG, "New distance to instruction $distance")
            _nextInstruction.update { it?.copy(distanceToManeuver = distance) }
        }

        override fun onInstructionsChanged(instructions: List<GuidanceInstruction>) {
            Log.d(TAG, "New instruction received from GuidanceUpdatedListener: $instructions")
            _nextInstruction.update { processNextInstruction(instructions) }
        }
    }

    private val horizonUpdatedListener = DefaultHorizonUpdatedListener { newHorizonElements ->
        _upcomingHorizonElements.update { newHorizonElements }
    }

    private var freeDrivingJob: Job? = null

    init {
        registerNavigationEventDispatcherListeners()
        if (onCheckLocationPermission()) {
            startLocationProvider()
        }
    }

    fun startLocationProvider() {
        defaultLocationProvider.addOnLocationUpdateListener(onLocationUpdateListener)
        defaultLocationProvider.enable()
    }

    @OptIn(BetaLocationApi::class)
    @Suppress("detekt:CyclomaticComplexMethod")
    fun dispatchAction(action: MapScreenAction) {
        when (action) {
            is MapScreenAction.ClearMap -> clearMap()
            is MapScreenAction.ClearSearch -> {
                cleanPoiPlaces()
                recenterMap()
            }

            is MapScreenAction.CloseWaypointPanel -> closeWaypointPanel()
            is MapScreenAction.RecenterMap -> recenterMap()
            is MapScreenAction.StartInteractiveMode -> startInteractiveMode()
            is MapScreenAction.ShowPoiCategorySearchResultFocus -> showPoiCategoryFocus(action.poiResults)
            is MapScreenAction.ShowSearchResultFocus -> showPoiFocus(action.placeDetails)
            is MapScreenAction.ShowPoiFocus -> getPoiData(action.geoPoint) { showPoiFocus(it) }
            is MapScreenAction.CleanRoutePreview -> cleanRoutePreview(action.onClearRoutes)
            is MapScreenAction.ShowAddWaypointPanel -> getPoiData(action.geoPoint) { showAddWaypointPanel(it) }
            is MapScreenAction.ShowRemoveWaypointPanel -> getPoiData(action.routeStop.place.coordinate) {
                showRemoveWaypointPanel(it, action.routeStop)
            }

            is MapScreenAction.ToggleBottomSheet -> toggleBottomSheet(isExpanded = action.isExpanded)

            is MapScreenAction.UpdateRoute -> updateRoute(action.route, action.routePlanningOptions)
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

            is MapScreenAction.ToggleCameraTrackingMode -> toggleCameraTrackingMode()
        }
    }

    fun onResume() {
        startFreeDrivingJob()
    }

    fun onPause() {
        stopFreeDrivingJob()
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
        mapMatchedLocationProvider.disable()
        textToSpeechEngine.shutdown()
    }

    private fun clearMap() {
        _mapScreenUiState.update {
            it.copy(
                scenario = HOME,
                isInteractiveMode = false,
                destinationMarker = null,
                placeDetails = null,
                routeStop = null,
            )
        }

        onClearMap()
        if (mapScreenUiState.value.poiPlaces.isNotEmpty()) {
            zoomToAllMarkers()
        } else {
            recenterMap()
        }
        startFreeDrivingJob()
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

    @OptIn(BetaLocationApi::class)
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
    private fun showPoiFocus(placeDetails: PlaceDetails) {
        stopFreeDriving()
        stopFreeDrivingJob()

        _mapScreenUiState.update {
            it.copy(
                scenario = POI_FOCUS,
                placeDetails = placeDetails,
                destinationMarker = placeDetails.place.coordinate,
            )
        }
        recenterMap()
    }

    private fun cleanRoutePreview(onClearRoutes: () -> Unit) {
        onClearRoutes()
        mapScreenUiState.value.placeDetails?.let { showPoiFocus(it) }
    }

    private fun showRemoveWaypointPanel(
        placeDetails: PlaceDetails,
        routeStop: RouteStop,
    ) {
        _mapScreenUiState.update {
            it.copy(
                placeDetails = placeDetails,
                destinationMarker = null,
                cameraTrackingMode = CameraTrackingMode.None,
                routeStop = routeStop,
            )
        }

        _cameraOptions.tryEmit(
            CameraOptions(
                zoom = MapScreenUiState.POI_CAMERA_ZOOM,
                tilt = MapScreenUiState.DEFAULT_TILT,
                rotation = MapScreenUiState.DEFAULT_ROTATION,
                position = placeDetails.place.coordinate,
            ),
        )
    }

    private fun showAddWaypointPanel(placeDetails: PlaceDetails) {
        _mapScreenUiState.update {
            it.copy(
                placeDetails = placeDetails,
                destinationMarker = placeDetails.place.coordinate,
                cameraTrackingMode = CameraTrackingMode.None,
                routeStop = null,
            )
        }

        _cameraOptions.tryEmit(
            CameraOptions(
                zoom = MapScreenUiState.POI_CAMERA_ZOOM,
                tilt = MapScreenUiState.DEFAULT_TILT,
                rotation = MapScreenUiState.DEFAULT_ROTATION,
                position = placeDetails.place.coordinate,
            ),
        )
    }

    private fun closeWaypointPanel() {
        _mapScreenUiState.update {
            it.copy(
                placeDetails = null,
                destinationMarker = null,
                cameraTrackingMode = FollowRouteDirection,
                routeStop = null,
            )
        }
    }

    private fun updateRoute(
        route: Route,
        routePlanningOptions: RoutePlanningOptions,
    ) {
        navigation.setActiveRoutePlan(
            RoutePlan(
                route = route,
                routePlanningOptions = routePlanningOptions,
            ),
        )

        closeWaypointPanel()
    }

    // Start turn-by-turn navigation with optional simulation
    @OptIn(
        BetaHorizonOptionsCreationApi::class,
        BetaHorizonTrafficElementApi::class,
    )
    private fun startGuidance(navigationOptions: NavigationOptions) {
        stopGuidance()

        navigation.addLocationContextUpdatedListener(locationContextUpdatedListener)
        navigation.addGuidanceUpdatedListener(guidanceUpdatedListener)
        navigation.addProgressUpdatedListener(progressUpdatedListener)
        navigation.addDestinationArrivalListener(destinationArrivalListener)
        navigation.addLaneGuidanceUpdatedListener(laneGuidanceUpdatedListener)
        navigation.addHorizonUpdatedListener(
            options = createHorizonOptions(
                elementTypes = listOf(
                    TrafficElementType,
                    SafetyLocationElementType,
                ),
            ),
            listener = horizonUpdatedListener,
        )
        navigation.locationProvider = defaultLocationProvider

        mapMatchedLocationProvider.enable().also {
            updateLocationProvider(mapMatchedLocationProvider)
        }

        navigation.start(navigationOptions)
        _mapScreenUiState.update {
            it.copy(
                scenario = GUIDANCE,
                cameraTrackingMode = FollowRouteDirection,
                placeDetails = null,
                routeStop = null,
            )
        }
    }

    private fun stopGuidance() {
        navigation.removeLocationContextUpdatedListener(locationContextUpdatedListener)
        navigation.removeProgressUpdatedListener(progressUpdatedListener)
        navigation.removeGuidanceUpdatedListener(guidanceUpdatedListener)
        navigation.removeDestinationArrivalListener(destinationArrivalListener)
        navigation.removeLaneGuidanceUpdatedListener(laneGuidanceUpdatedListener)
        navigation.removeHorizonUpdatedListener(horizonUpdatedListener)

        navigation.stop()
        mapMatchedLocationProvider.disable().also {
            updateLocationProvider(defaultLocationProvider)
        }
    }

    private fun processNextInstruction(instructions: List<GuidanceInstruction>?): NextInstruction? =
        instructions?.firstOrNull()?.let { firstInstruction ->
            NextInstruction(
                maneuverType = firstInstruction.toManeuverType(),
                distanceToManeuver = firstInstruction.routeOffset,
                roadName = firstInstruction.nextSignificantRoad?.name,
                towardName = firstInstruction.signpost?.towardName,
                exitNumber = firstInstruction.signpost?.exitNumber,
                exitName = firstInstruction.signpost?.exitName,
            )
        }

    // Starts free driving scenario
    @OptIn(BetaHorizonOptionsCreationApi::class)
    private fun startFreeDriving() {
        _mapScreenUiState.update { it.copy(scenario = FREE_DRIVING) }
        recenterMap()

        navigation.addHorizonUpdatedListener(
            options = createHorizonOptions(
                elementTypes = listOf(
                    TrafficElementType,
                    SafetyLocationElementType,
                ),
            ),
            listener = horizonUpdatedListener,
        )

        mapMatchedLocationProvider.enable().also {
            updateLocationProvider(mapMatchedLocationProvider)
        }

        navigation.locationProvider = defaultLocationProvider
        navigation.addLocationContextUpdatedListener(locationContextUpdatedListener)
        navigation.start()
    }

    // Ends free driving scenario
    private fun stopFreeDriving() {
        mapMatchedLocationProvider.disable().also {
            updateLocationProvider(defaultLocationProvider)
        }

        navigation.removeLocationContextUpdatedListener(locationContextUpdatedListener)
        navigation.removeHorizonUpdatedListener(horizonUpdatedListener)
        navigation.stop()
    }

    // Starts a flow that checks every second if the user is driving
    private fun startFreeDrivingJob() {
        if (mapScreenUiState.value.scenario == HOME || mapScreenUiState.value.scenario == FREE_DRIVING) {
            freeDrivingJob = viewModelScope.launch {
                freeDrivingManager.getIsDrivingFlow { locationProvider.value }.collect { isDriving ->
                    Log.d(TAG, "isDriving: $isDriving")
                    when {
                        isDriving && mapScreenUiState.value.scenario == HOME -> startFreeDriving()
                        !isDriving && mapScreenUiState.value.scenario == FREE_DRIVING -> {
                            stopFreeDriving()
                            clearMap()
                            cancel()
                        }
                    }
                }
            }
        }
    }

    private fun stopFreeDrivingJob() {
        freeDrivingJob?.cancel()
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

    private fun registerNavigationEventDispatcherListeners() {
        navigation.apply {
            addNavigationStateChangedListener { navigationEventDispatcher.dispatchOnNavigationStateChanged(it) }
            addProgressUpdatedListener { navigationEventDispatcher.dispatchOnProgressUpdated(it) }
            addActiveRouteChangedListener { navigationEventDispatcher.dispatchOnActiveRouteChanged(it) }
            addDestinationArrivalListener { navigationEventDispatcher.dispatchOnDestinationArrived(it) }
            addRouteUpdatedListener { route, reason ->
                navigationEventDispatcher.dispatchOnRouteUpdated(route, reason)
            }
            addRouteAddedListener { route, options, reason ->
                navigationEventDispatcher.dispatchOnRouteAddedListener(route, options, reason)
            }
            addRouteRemovedListener { route, reason ->
                navigationEventDispatcher.dispatchOnRouteRemovedListener(route, reason)
            }
        }
    }

    private fun updateLocationProvider(locationProvider: LocationProvider) {
        _locationProvider.update { locationProvider }

        _mapInfrastructure.update {
            it.copy {
                locationInfrastructure = MapLocationInfrastructure {
                    this.locationProvider = locationProvider
                }
            }
        }
    }

    companion object {
        private const val TAG = "MapScreenViewModel"

        private val DEFAULT_LOCATION = GeoPoint(0.0, 0.0)
        private const val INITIAL_CAMERA_ZOOM = 12.0

        val MAP_DATA_STORE = object : CreationExtras.Key<MapDataStore> {}
        val DEFAULT_LOCATION_PROVIDER_KEY = object : CreationExtras.Key<LocationProvider> {}
        val MAP_MATCHED_LOCATION_PROVIDER_KEY = object : CreationExtras.Key<LocationProvider> {}
        val REVERSE_GEOCODER_KEY = object : CreationExtras.Key<ReverseGeocoder> {}
        val NAVIGATION_KEY = object : CreationExtras.Key<TomTomNavigation> {}
        val FREE_DRIVING_MANAGER_KEY = object : CreationExtras.Key<FreeDrivingManager> {}
        val SETTINGS_REPOSITORY_KEY = object : CreationExtras.Key<SettingsRepository> {}
        val ON_CLEAR_MAP_KEY = object : CreationExtras.Key<() -> Unit> {}
        val ON_CHECK_LOCATION_PERMISSION = object : CreationExtras.Key<() -> Boolean> {}
        val TEXT_TO_SPEECH_ENGINE_KEY = object : CreationExtras.Key<TextToSpeechEngine> {}

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MapScreenViewModel(
                    mapDataStore = this[MAP_DATA_STORE] as MapDataStore,
                    defaultLocationProvider = this[DEFAULT_LOCATION_PROVIDER_KEY] as LocationProvider,
                    mapMatchedLocationProvider = this[MAP_MATCHED_LOCATION_PROVIDER_KEY] as LocationProvider,
                    reverseGeocoder = this[REVERSE_GEOCODER_KEY] as ReverseGeocoder,
                    navigation = this[NAVIGATION_KEY] as TomTomNavigation,
                    freeDrivingManager = this[FREE_DRIVING_MANAGER_KEY] as FreeDrivingManager,
                    settingsRepository = this[SETTINGS_REPOSITORY_KEY] as SettingsRepository,
                    onClearMap = this[ON_CLEAR_MAP_KEY] as () -> Unit,
                    onCheckLocationPermission = this[ON_CHECK_LOCATION_PERMISSION] as () -> Boolean,
                    textToSpeechEngine = this[TEXT_TO_SPEECH_ENGINE_KEY] as TextToSpeechEngine,
                )
            }
        }
    }
}
