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

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.application.arrival.ArrivalStateHolder
import com.example.application.arrival.ArrivalUiComponents
import com.example.application.common.MARKERS_ZOOM_PADDING_DP
import com.example.application.common.PlaceDetails
import com.example.application.common.getPinMarkerProperties
import com.example.application.common.isDeviceInLandscape
import com.example.application.common.safeAreaStartPadding
import com.example.application.freedriving.FreeDrivingManager
import com.example.application.freedriving.FreeDrivingStateHolder
import com.example.application.freedriving.FreeDrivingUiComponents
import com.example.application.guidance.GuidanceStateHolder
import com.example.application.guidance.GuidanceUiComponents
import com.example.application.home.HomeStateHolder
import com.example.application.home.HomeUiComponents
import com.example.application.map.MapScreenAction.CleanRoutePreview
import com.example.application.map.MapScreenAction.ClearMap
import com.example.application.map.MapScreenAction.ClearSearch
import com.example.application.map.MapScreenAction.CloseWaypointPanel
import com.example.application.map.MapScreenAction.RecenterMap
import com.example.application.map.MapScreenAction.ShowAddWaypointPanel
import com.example.application.map.MapScreenAction.ShowPoiCategorySearchResultFocus
import com.example.application.map.MapScreenAction.ShowPoiFocus
import com.example.application.map.MapScreenAction.ShowRemoveWaypointPanel
import com.example.application.map.MapScreenAction.ShowRoutePreview
import com.example.application.map.MapScreenAction.ShowRoutingFailure
import com.example.application.map.MapScreenAction.ShowSearchFailure
import com.example.application.map.MapScreenAction.ShowSearchResultFocus
import com.example.application.map.MapScreenAction.StartGuidance
import com.example.application.map.MapScreenAction.StartInteractiveMode
import com.example.application.map.MapScreenAction.StopGuidance
import com.example.application.map.MapScreenAction.ToggleBottomSheet
import com.example.application.map.MapScreenAction.ToggleCameraTrackingMode
import com.example.application.map.MapScreenAction.UpdateRoute
import com.example.application.map.MapScreenUiState.ErrorState
import com.example.application.map.MapScreenUiState.Scenario.DESTINATION_ARRIVAL
import com.example.application.map.MapScreenUiState.Scenario.FREE_DRIVING
import com.example.application.map.MapScreenUiState.Scenario.GUIDANCE
import com.example.application.map.MapScreenUiState.Scenario.HOME
import com.example.application.map.MapScreenUiState.Scenario.POI_FOCUS
import com.example.application.map.MapScreenUiState.Scenario.ROUTE_PREVIEW
import com.example.application.poifocus.PoiFocusStateHolder
import com.example.application.poifocus.PoiFocusUiComponents
import com.example.application.routepreview.RoutePreviewStateHolder
import com.example.application.routepreview.RoutePreviewUiComponents
import com.example.application.search.SearchStateHolder
import com.example.application.search.SearchViewModel
import com.example.application.search.getPoiIcon
import com.example.application.settings.data.SettingsRepository
import com.example.application.tts.TextToSpeechEngine
import com.example.application.ui.ErrorSnackbarHost
import com.example.application.ui.RecenterMapStateHolder
import com.tomtom.sdk.annotations.AlphaSdkInitializationApi
import com.tomtom.sdk.entrypoint.TomTomSdk
import com.tomtom.sdk.location.mapmatched.MapMatchedLocationProviderFactory
import com.tomtom.sdk.location.poi.StandardCategoryId
import com.tomtom.sdk.map.display.annotation.BetaInitialCameraOptionsApi
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.camera.InitialCameraOptions
import com.tomtom.sdk.map.display.compose.BetaMapComposableApi
import com.tomtom.sdk.map.display.compose.TomTomMap
import com.tomtom.sdk.map.display.compose.model.MapInfrastructure
import com.tomtom.sdk.map.display.compose.model.MarkerData
import com.tomtom.sdk.map.display.compose.nodes.CurrentLocationMarker
import com.tomtom.sdk.map.display.compose.nodes.Marker
import com.tomtom.sdk.map.display.compose.nodes.Traffic
import com.tomtom.sdk.map.display.compose.properties.CurrentLocationMarkerProperties
import com.tomtom.sdk.map.display.compose.state.MapViewState
import com.tomtom.sdk.map.display.compose.state.rememberMapViewState
import com.tomtom.sdk.map.display.compose.state.rememberMarkerState
import com.tomtom.sdk.map.display.compose.state.rememberTrafficState
import com.tomtom.sdk.map.display.location.LocationMarkerOptions
import com.tomtom.sdk.map.display.mapdatastore.common.ExperimentalMapDataStoreApi
import com.tomtom.sdk.map.display.style.StandardStyles
import com.tomtom.sdk.map.display.style.StyleMode
import com.tomtom.sdk.map.display.visualization.navigation.compose.BetterRouteVisualization
import com.tomtom.sdk.map.display.visualization.navigation.compose.NavigationVisualization
import com.tomtom.sdk.map.display.visualization.navigation.compose.model.NavigationVisualizationInfrastructure
import com.tomtom.sdk.map.display.visualization.navigation.compose.state.rememberBetterRouteVisualizationState
import com.tomtom.sdk.map.display.visualization.routing.compose.TrafficVisualization
import com.tomtom.sdk.map.display.visualization.routing.compose.state.rememberTrafficVisualizationState
import com.tomtom.sdk.navigation.NavigationOptions
import com.tomtom.sdk.navigation.RoutePlan
import com.tomtom.sdk.routing.RoutingFailure
import com.tomtom.sdk.routing.route.RouteStop
import com.tomtom.sdk.routing.route.RouteStopId
import com.tomtom.sdk.search.common.error.SearchFailure
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@OptIn(
    BetaInitialCameraOptionsApi::class,
    BetaMapComposableApi::class,
    AlphaSdkInitializationApi::class,
    ExperimentalMapDataStoreApi::class,
    ExperimentalMaterial3Api::class,
)
@Composable
fun MapScreen(
    settingsRepository: SettingsRepository,
    modifier: Modifier = Modifier,
    onCheckLocationPermission: () -> Boolean = { false },
    locationRequestGrantedFlow: StateFlow<Boolean?> = MutableStateFlow(null).asStateFlow(),
    onSettingsClick: () -> Unit,
    routesViewModel: RoutesViewModel = viewModel(
        factory = RoutesViewModel.Factory,
        extras = MutableCreationExtras().apply {
            set(RoutesViewModel.ROUTE_PLANNER_KEY, TomTomSdk.createRoutePlanner())
            set(RoutesViewModel.NAVIGATION_KEY, TomTomSdk.navigation)
        },
    ),
    mapScreenViewModel: MapScreenViewModel = viewModel(
        factory = MapScreenViewModel.Factory,
        extras = MutableCreationExtras().apply {
            set(
                MapScreenViewModel.MAP_DATA_STORE,
                TomTomSdk.sdkContext.mapDataStore,
            )
            set(MapScreenViewModel.DEFAULT_LOCATION_PROVIDER_KEY, TomTomSdk.locationProvider)
            set(
                MapScreenViewModel.MAP_MATCHED_LOCATION_PROVIDER_KEY,
                MapMatchedLocationProviderFactory.create(TomTomSdk.navigation),
            )
            set(MapScreenViewModel.REVERSE_GEOCODER_KEY, TomTomSdk.createReverseGeocoder())
            set(MapScreenViewModel.NAVIGATION_KEY, TomTomSdk.navigation)
            set(MapScreenViewModel.FREE_DRIVING_MANAGER_KEY, FreeDrivingManager())
            set(MapScreenViewModel.SETTINGS_REPOSITORY_KEY, settingsRepository)
            set(MapScreenViewModel.ON_CLEAR_MAP_KEY) { routesViewModel.clearRoutes() }
            set(MapScreenViewModel.ON_CHECK_LOCATION_PERMISSION, onCheckLocationPermission)
            set(MapScreenViewModel.TEXT_TO_SPEECH_ENGINE_KEY, TextToSpeechEngine(LocalContext.current))
        },
    ),
    searchViewModel: SearchViewModel = viewModel(
        factory = SearchViewModel.Factory,
        extras = MutableCreationExtras().apply {
            set(SearchViewModel.LOCATION_PROVIDER_KEY, TomTomSdk.locationProvider)
            set(SearchViewModel.SEARCH_KEY, TomTomSdk.createSearch())
            set(SearchViewModel.SEARCH_FAILURE_KEY) { failure: SearchFailure ->
                mapScreenViewModel.dispatchAction(ShowSearchFailure(failure))
            }
            set(SearchViewModel.POI_SEARCH_SUCCESS_KEY) { searchResults ->
                mapScreenViewModel.dispatchAction(ShowPoiCategorySearchResultFocus(searchResults))
            }
            set(SearchViewModel.CLEAN_MAP_KEY) { mapScreenViewModel.dispatchAction(ClearSearch) }
            set(SearchViewModel.TOGGLE_BOTTOM_SHEET_KEY) { toggleBottomSheetValue ->
                mapScreenViewModel.dispatchAction(ToggleBottomSheet(toggleBottomSheetValue))
            }
        },
    ),
) {
    val isDeviceInLandscape = isDeviceInLandscape()

    LifecycleResumeEffect(mapScreenViewModel) {
        mapScreenViewModel.onResume()

        onPauseOrDispose {
            mapScreenViewModel.onPause()
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val mapInfrastructure by mapScreenViewModel.mapInfrastructure.collectAsStateWithLifecycle()
    val navigationInfrastructure by routesViewModel.navigationInfrastructure.collectAsStateWithLifecycle()
    val mapScreenUiState by mapScreenViewModel.mapScreenUiState.collectAsStateWithLifecycle()

    ManageLocationRequestGranted(locationRequestGrantedFlow) {
        mapScreenViewModel.startLocationProvider()
    }

    // Remember a mapViewState to update the map state and camera position state
    val mapViewState = rememberStyledMapViewState(initialCameraOptions = mapScreenViewModel.initialCameraOptions)

    val onAnimateCamera: (CameraOptions) -> Unit =
        { cameraOptions ->
            coroutineScope.launch {
                mapViewState.cameraState.animateCamera(cameraOptions)
            }
        }

    val cameraOptions by mapScreenViewModel.cameraOptions.collectAsStateWithLifecycle()

    LaunchedEffect(mapScreenUiState.cameraTrackingMode) {
        mapViewState.cameraState.trackingMode = mapScreenUiState.cameraTrackingMode
    }

    LaunchedEffect(cameraOptions) {
        cameraOptions?.let { mapViewState.cameraState.animateCamera(it) }
    }

    val recenterMapStateHolder = remember(mapScreenUiState.isInteractiveMode) {
        RecenterMapStateHolder(mapScreenUiState.isInteractiveMode) { mapScreenViewModel.dispatchAction(RecenterMap) }
    }
    val onClearClick = remember { { mapScreenViewModel.dispatchAction(ClearMap) } }
    val onSafeAreaTopPaddingUpdate = remember<(Int) -> Unit> { { mapScreenViewModel.updateSafeAreaTopPadding(it) } }
    val onSafeAreaBottomPaddingUpdate =
        remember<(Int) -> Unit> { { mapScreenViewModel.updateSafeAreaBottomPadding(it) } }

    val searchStateHolder = SearchStateHolder(
        isBottomPanelExpandedFlow = mapScreenViewModel.isBottomSheetExpanded,
        searchPanelStateFlow = searchViewModel.searchPanelState,
        inputQueryFlow = searchViewModel.inputQuery,
        searchResultsFlow = searchViewModel.searchResults,
        scaffoldState = rememberBottomSheetScaffoldState(),
        onClearSearch = { searchViewModel.clearSearch() },
        onResetSearch = { searchViewModel.resetSearch() },
        onUpdateSearch = { query -> searchViewModel.updateInputQuery(query) },
        onSearchResultClick = { placeDetails: PlaceDetails ->
            mapScreenViewModel.dispatchAction(ShowSearchResultFocus(placeDetails))
        },
        onSearchPoiClick = { categoryId: StandardCategoryId, categoryDescription: String ->
            searchViewModel.newPoiInputQuery(categoryId, categoryDescription)
        },
        onToggleBottomSheet = { mapScreenViewModel.dispatchAction(ToggleBottomSheet(it)) },
        isDeviceInLandscape = isDeviceInLandscape,
    )

    val homeStateHolder = HomeStateHolder(
        onAnimateCamera = onAnimateCamera,
        poiPlaces = mapScreenUiState.poiPlaces,
        recenterMapStateHolder = recenterMapStateHolder,
        searchStateHolder = searchStateHolder,
        locationUpdates = mapScreenViewModel.userLocation,
        isDeviceInLandscape = isDeviceInLandscape,
        onSafeAreaTopPaddingUpdate = onSafeAreaTopPaddingUpdate,
        onSafeAreaBottomPaddingUpdate = onSafeAreaBottomPaddingUpdate,
        onSettingsClick = onSettingsClick,
    )

    val poiFocusStateHolder = PoiFocusStateHolder(
        recenterMapStateHolder = recenterMapStateHolder,
        placeDetails = mapScreenUiState.placeDetails,
        onAnimateCamera = onAnimateCamera,
        onClearClick = onClearClick,
        onRouteButtonClick = {
            mapScreenViewModel.locationProvider.value.lastKnownLocation?.position?.let { origin ->
                mapScreenUiState.placeDetails?.place?.coordinate?.let { destination ->
                    routesViewModel.planRoute(
                        origin,
                        destination,
                        { mapScreenViewModel.dispatchAction(ShowRoutePreview) },
                        { mapScreenViewModel.dispatchAction(ShowRoutingFailure(it)) },
                    )
                }
            }
        },
        isDeviceInLandscape = isDeviceInLandscape,
        onSafeAreaTopPaddingUpdate = onSafeAreaTopPaddingUpdate,
        onSafeAreaBottomPaddingUpdate = onSafeAreaBottomPaddingUpdate,
    )

    val routePreviewStateHolder = RoutePreviewStateHolder(
        recenterMapStateHolder = recenterMapStateHolder,
        routesFlow = routesViewModel.routes,
        onClearClick = onClearClick,
        onDriveButtonClick = { startGuidance(routesViewModel, mapScreenViewModel, searchViewModel) },
        onSimulateButtonClick = {
            startGuidance(routesViewModel, mapScreenViewModel, searchViewModel)
        },
        isDeviceInLandscape = isDeviceInLandscape,
        onSafeAreaTopPaddingUpdate = onSafeAreaTopPaddingUpdate,
        onSafeAreaBottomPaddingUpdate = onSafeAreaBottomPaddingUpdate,
        onBackClick = { mapScreenViewModel.dispatchAction(CleanRoutePreview { routesViewModel.clearRoutes() }) },
    )

    val onRoutePlanningSuccess = {
        routesViewModel.selectedRoute.value?.let { route ->
            mapScreenViewModel.dispatchAction(
                UpdateRoute(
                    route,
                    routesViewModel.routePlanningOptions,
                ),
            )
        } ?: mapScreenViewModel.dispatchAction(
            ShowRoutingFailure(RoutingFailure.NoRouteFoundFailure("Selected route is null")),
        )
    }
    val onRoutePlanningFailure: (RoutingFailure) -> Unit = { mapScreenViewModel.dispatchAction(ShowRoutingFailure(it)) }

    val guidanceStateHolder = GuidanceStateHolder(
        recenterMapStateHolder = recenterMapStateHolder,
        selectedRouteFlow = routesViewModel.selectedRoute,
        locationContext = mapScreenViewModel.locationContext,
        routeProgress = mapScreenViewModel.routeProgress,
        onExitButtonClick = { mapScreenViewModel.dispatchAction(StopGuidance) },
        nextInstruction = mapScreenViewModel.nextInstruction,
        horizonElementsFlow = mapScreenViewModel.upcomingHorizonElements,
        laneGuidance = mapScreenViewModel.laneGuidance,
        onMapModeToggleClick = { mapScreenViewModel.dispatchAction(ToggleCameraTrackingMode(it)) },
        cameraTrackingMode = mapScreenUiState.cameraTrackingMode,
        placeDetails = mapScreenUiState.placeDetails,
        routeStop = mapScreenUiState.routeStop,
        onAddStopButtonClick = {
            mapScreenUiState.placeDetails?.place?.let {
                routesViewModel.addWayPoint(
                    it,
                    onRoutePlanningSuccess = onRoutePlanningSuccess,
                    onRoutePlanningFailure = onRoutePlanningFailure,
                )
            }
        },
        onRemoveStopButtonClick = {
            mapScreenUiState.routeStop?.let {
                routesViewModel.removeWayPoint(
                    it,
                    onRoutePlanningSuccess = onRoutePlanningSuccess,
                    onRoutePlanningFailure = onRoutePlanningFailure,
                )
            }
        },
        onCloseWaypointPanelButtonClick = { mapScreenViewModel.dispatchAction(CloseWaypointPanel) },
        isDeviceInLandscape = isDeviceInLandscape,
        onSafeAreaTopPaddingUpdate = onSafeAreaTopPaddingUpdate,
        onSafeAreaBottomPaddingUpdate = onSafeAreaBottomPaddingUpdate,
    )

    val freeDrivingStateHolder = FreeDrivingStateHolder(
        recenterMapStateHolder = recenterMapStateHolder,
        searchStateHolder = searchStateHolder,
        locationContext = mapScreenViewModel.locationContext,
        isDeviceInLandscape = isDeviceInLandscape,
        onSafeAreaTopPaddingUpdate = onSafeAreaTopPaddingUpdate,
        onSafeAreaBottomPaddingUpdate = onSafeAreaBottomPaddingUpdate,
        horizonElementsFlow = mapScreenViewModel.upcomingHorizonElements,
    )

    val arrivalStateHolder = ArrivalStateHolder(
        recenterMapStateHolder = recenterMapStateHolder,
        destinationDetails = mapScreenUiState.destinationDetails,
        onArrivalButtonClick = onClearClick,
        isDeviceInLandscape = isDeviceInLandscape,
        onSafeAreaTopPaddingUpdate = onSafeAreaTopPaddingUpdate,
        onSafeAreaBottomPaddingUpdate = onSafeAreaBottomPaddingUpdate,
    )

    MapScreenContent(
        mapInfrastructure = mapInfrastructure,
        navigationInfrastructure = navigationInfrastructure,
        mapScreenUiState = mapScreenUiState,
        isDeviceInLandscape = isDeviceInLandscape,
        mapViewState = mapViewState,
        errorStateFlow = mapScreenViewModel.errorState,
        onDispatchAction = { mapScreenViewModel.dispatchAction(it) },
        onGetRouteStop = { routesViewModel.getRouteStop(it) },
        homeStateHolder = homeStateHolder,
        poiFocusStateHolder = poiFocusStateHolder,
        routePreviewStateHolder = routePreviewStateHolder,
        guidanceStateHolder = guidanceStateHolder,
        freeDrivingStateHolder = freeDrivingStateHolder,
        arrivalStateHolder = arrivalStateHolder,
        modifier = modifier,
        onErrorShown = { mapScreenViewModel.clearErrorState() },
    )
}

@OptIn(
    BetaInitialCameraOptionsApi::class,
    BetaMapComposableApi::class,
)
@Composable
private fun rememberStyledMapViewState(initialCameraOptions: InitialCameraOptions): MapViewState {
    val styleMode = if (isSystemInDarkTheme()) StyleMode.DARK else StyleMode.MAIN

    val mapViewState = rememberMapViewState(initialCameraOptions = initialCameraOptions) {
        styleState.styleMode = styleMode
    }

    SideEffect { mapViewState.styleState.styleMode = styleMode }

    return mapViewState
}

private fun startGuidance(
    routesViewModel: RoutesViewModel,
    mapScreenViewModel: MapScreenViewModel,
    searchViewModel: SearchViewModel,
) {
    routesViewModel.selectedRoute.value?.let { route ->
        mapScreenViewModel.dispatchAction(
            StartGuidance(
                NavigationOptions(
                    RoutePlan(
                        route = route,
                        routePlanningOptions = routesViewModel.routePlanningOptions,
                    ),
                ),
            ),
        )
    } ?: mapScreenViewModel.dispatchAction(
        ShowRoutingFailure(RoutingFailure.NoRouteFoundFailure("Selected route is null")),
    )
    searchViewModel.clearSearch()
    mapScreenViewModel.cleanPoiPlaces()
}

@OptIn(BetaMapComposableApi::class)
@Suppress("detekt:CyclomaticComplexMethod")
@Composable
fun MapScreenContent(
    mapInfrastructure: MapInfrastructure,
    navigationInfrastructure: NavigationVisualizationInfrastructure,
    mapScreenUiState: MapScreenUiState,
    isDeviceInLandscape: Boolean,
    mapViewState: MapViewState,
    errorStateFlow: StateFlow<ErrorState?>,
    onDispatchAction: (MapScreenAction) -> Unit,
    onGetRouteStop: (RouteStopId) -> RouteStop?,
    homeStateHolder: HomeStateHolder,
    poiFocusStateHolder: PoiFocusStateHolder,
    routePreviewStateHolder: RoutePreviewStateHolder,
    guidanceStateHolder: GuidanceStateHolder,
    freeDrivingStateHolder: FreeDrivingStateHolder,
    arrivalStateHolder: ArrivalStateHolder,
    modifier: Modifier = Modifier,
    onErrorShown: () -> Unit,
) {
    LaunchedEffect(mapScreenUiState.scenario) {
        val mapStyle = if (mapScreenUiState.isDrivingScenario()) StandardStyles.DRIVING else StandardStyles.BROWSING
        mapViewState.styleState.loadStyle(mapStyle)
    }

    LaunchedEffect(mapScreenUiState.zoomToAllMarkers) {
        mapViewState.cameraState.zoomToAllMarkers(padding = MARKERS_ZOOM_PADDING_DP)
    }

    val localDensity = LocalDensity.current
    mapViewState.safeArea = PaddingValues(
        start = safeAreaStartPadding(isDeviceInLandscape),
        bottom = localDensity.run { mapScreenUiState.safeAreaBottomPadding.toDp() },
        top = localDensity.run { mapScreenUiState.safeAreaTopPadding.toDp() },
    )

    Box(modifier = modifier) {
        TomTomMap(
            infrastructure = mapInfrastructure,
            state = mapViewState,
            onMapLongClick = { destination ->
                if (mapScreenUiState.scenario == GUIDANCE) {
                    onDispatchAction(ShowAddWaypointPanel(destination))
                } else {
                    onDispatchAction(ShowPoiFocus(destination))
                }
            },
            onMapDoubleClickListener = { onDispatchAction(StartInteractiveMode) },
            onMapPanningListener = { onDispatchAction(StartInteractiveMode) },
        ) {
            Traffic(
                state = rememberTrafficState(
                    showTrafficFlow = false,
                    showTrafficIncidents = true,
                ),
            )
            CurrentLocationMarker(
                CurrentLocationMarkerProperties {
                    type = LocationMarkerOptions.Type.Chevron
                },
            )

            if ((mapScreenUiState.scenario == HOME || mapScreenUiState.scenario == FREE_DRIVING) &&
                mapScreenUiState.poiPlaces.isNotEmpty()
            ) {
                mapScreenUiState.poiPlaces.forEach { placeDetails ->
                    Marker(
                        data = MarkerData(geoPoint = placeDetails.place.coordinate),
                        properties = getPinMarkerProperties(
                            getPoiIcon(placeDetails.poi?.categoryIds?.elementAt(0)?.standard),
                        ),
                        state = rememberMarkerState(),
                        onClick = { onDispatchAction(ShowSearchResultFocus(placeDetails)) },
                    )
                }
            }

            if (mapScreenUiState.destinationMarker != null &&
                listOf(POI_FOCUS, DESTINATION_ARRIVAL, GUIDANCE).any { it == mapScreenUiState.scenario }
            ) {
                Marker(
                    data = MarkerData(geoPoint = mapScreenUiState.destinationMarker),
                    properties = getPinMarkerProperties(
                        getPoiIcon(mapScreenUiState.placeDetails?.poi?.categoryIds?.elementAt(0)?.standard),
                    ),
                    state = rememberMarkerState(),
                )
            }

            NavigationVisualization(
                infrastructure = navigationInfrastructure,
                onRouteStopClick = { id ->
                    onGetRouteStop(id)?.let { onDispatchAction(ShowRemoveWaypointPanel(it)) }
                },
            ) {
                TrafficVisualization(
                    state = rememberTrafficVisualizationState(
                        trafficIncidentsEnabled = true,
                    ),
                )
                BetterRouteVisualization(
                    state = rememberBetterRouteVisualizationState(
                        enabled = true,
                    ),
                )
            }
        }

        ScenarioUiComponents(
            scenario = mapScreenUiState.scenario,
            homeStateHolder = homeStateHolder,
            poiFocusStateHolder = poiFocusStateHolder,
            routePreviewStateHolder = routePreviewStateHolder,
            guidanceStateHolder = guidanceStateHolder,
            arrivalStateHolder = arrivalStateHolder,
            freeDrivingStateHolder = freeDrivingStateHolder,
        )

        ErrorSnackbarHost(
            errorStateFlow = errorStateFlow,
            modifier = modifier.align(Alignment.BottomCenter),
            onErrorShown = onErrorShown,
        )
    }
}

@Composable
private fun BoxScope.ScenarioUiComponents(
    scenario: MapScreenUiState.Scenario,
    homeStateHolder: HomeStateHolder,
    poiFocusStateHolder: PoiFocusStateHolder,
    routePreviewStateHolder: RoutePreviewStateHolder,
    guidanceStateHolder: GuidanceStateHolder,
    freeDrivingStateHolder: FreeDrivingStateHolder,
    arrivalStateHolder: ArrivalStateHolder,
) {
    when (scenario) {
        HOME -> HomeUiComponents(homeStateHolder)

        POI_FOCUS -> PoiFocusUiComponents(poiFocusStateHolder)

        ROUTE_PREVIEW -> RoutePreviewUiComponents(routePreviewStateHolder)

        GUIDANCE -> GuidanceUiComponents(guidanceStateHolder)

        DESTINATION_ARRIVAL -> ArrivalUiComponents(arrivalStateHolder)

        FREE_DRIVING -> FreeDrivingUiComponents(freeDrivingStateHolder)
    }
}

@Composable
private fun ManageLocationRequestGranted(
    locationRequestGrantedFlow: StateFlow<Boolean?>,
    onLocationRequestGranted: () -> Unit,
) {
    val locationRequestGranted by locationRequestGrantedFlow.collectAsStateWithLifecycle()

    LaunchedEffect(locationRequestGranted) {
        if (locationRequestGranted == true) {
            onLocationRequestGranted()
        }
    }
}
