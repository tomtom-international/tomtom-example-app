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

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.application.common.MARKERS_ZOOM_PADDING_DP
import com.example.application.common.ui.ErrorSnackbarHost
import com.example.application.common.ui.isDeviceInLandscape
import com.example.application.common.ui.safeAreaStartPadding
import com.example.application.map.di.rememberMapScreenViewModels
import com.example.application.map.model.MapCallbacks
import com.example.application.map.model.MapEnvironment
import com.example.application.map.model.MapScreenAction
import com.example.application.map.model.MapScreenUiState
import com.example.application.map.model.MapScreenUiState.ErrorState
import com.example.application.map.model.ScenarioHolders
import com.example.application.map.ui.MapView
import com.example.application.map.ui.ScenarioHost
import com.example.application.map.ui.rememberScenarioHolders
import com.example.application.settings.data.SettingsRepository
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.camera.InitialCameraOptions
import com.tomtom.sdk.map.display.compose.state.MapViewState
import com.tomtom.sdk.map.display.compose.state.rememberMapViewState
import com.tomtom.sdk.map.display.style.StandardStyles
import com.tomtom.sdk.map.display.style.StyleMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Full map host used by the navigation activity.
 * Connects map/search/routing view models and renders routes, markers, and guidance UI.
 */
@Composable
fun MapScreen(
    settingsRepository: SettingsRepository,
    modifier: Modifier = Modifier,
    onCheckLocationPermission: () -> Boolean = { false },
    locationRequestGrantedFlow: StateFlow<Boolean?> = MutableStateFlow(null).asStateFlow(),
    onSettingsClick: () -> Unit,
) {
    val viewModels = rememberMapScreenViewModels(
        settingsRepository = settingsRepository,
        onCheckLocationPermission = onCheckLocationPermission,
    )
    val routesViewModel = viewModels.routesViewModel
    val mapScreenViewModel = viewModels.mapScreenViewModel
    val searchViewModel = viewModels.searchViewModel
    val isDeviceInLandscape = isDeviceInLandscape()

    LifecycleResumeEffect(mapScreenViewModel) {
        mapScreenViewModel.onResume()

        onPauseOrDispose {
            mapScreenViewModel.onPause()
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val mapDisplayInfrastructure by mapScreenViewModel.mapDisplayInfrastructure.collectAsStateWithLifecycle()
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

    val snackBarHostState = remember { SnackbarHostState() }

    val onStartGuidance: () -> Unit = {
        val selected = routesViewModel.selectedRoute.value
        if (selected != null) {
            mapScreenViewModel.dispatchAction(
                MapScreenAction.StartGuidance(
                    com.tomtom.sdk.navigation.NavigationOptions(
                        com.tomtom.sdk.navigation.RoutePlan(
                            route = selected,
                            routePlanningOptions = routesViewModel.routePlanningOptions,
                        ),
                    ),
                ),
            )
        } else {
            mapScreenViewModel.dispatchAction(
                MapScreenAction.ShowRoutingFailure(
                    com.tomtom.sdk.routing.RoutingFailure.NoRouteFoundFailure("Selected route is null"),
                ),
            )
        }
        searchViewModel.clearSearch()
        mapScreenViewModel.cleanPoiPlaces()
    }

    val holders = rememberScenarioHolders(
        mapScreenUiState = mapScreenUiState,
        routesViewModel = routesViewModel,
        mapScreenViewModel = mapScreenViewModel,
        searchViewModel = searchViewModel,
        isDeviceInLandscape = isDeviceInLandscape,
        snackbarHostState = snackBarHostState,
        onSettingsClick = onSettingsClick,
        onAnimateCamera = onAnimateCamera,
        onStartGuidance = onStartGuidance,
    )

    MapScreenContent(
        mapScreenUiState = mapScreenUiState,
        mapEnvironment = MapEnvironment(
            mapDisplayInfrastructure = mapDisplayInfrastructure,
            navigationVisualizationInfrastructure = navigationInfrastructure,
            mapViewState = mapViewState,
            isDeviceInLandscape = isDeviceInLandscape,
        ),
        errorStateFlow = mapScreenViewModel.errorState,
        callbacks = MapCallbacks(
            onDispatchMapScreenAction = { mapScreenViewModel.dispatchAction(it) },
            onGetRouteStop = { routesViewModel.getRouteStop(it) },
        ),
        holders = holders,
        snackbarHostState = snackBarHostState,
        modifier = modifier,
        onErrorShown = { mapScreenViewModel.clearErrorState() },
    )
}

@Composable
private fun rememberStyledMapViewState(initialCameraOptions: InitialCameraOptions): MapViewState {
    val styleMode = if (isSystemInDarkTheme()) StyleMode.DARK else StyleMode.MAIN

    val mapViewState = rememberMapViewState(initialCameraOptions = initialCameraOptions) {
        styleState.styleMode = styleMode
    }

    SideEffect { mapViewState.styleState.styleMode = styleMode }

    return mapViewState
}

@Suppress("detekt:CyclomaticComplexMethod")
@Composable
fun MapScreenContent(
    mapScreenUiState: MapScreenUiState,
    mapEnvironment: MapEnvironment,
    errorStateFlow: StateFlow<ErrorState?>,
    callbacks: MapCallbacks,
    holders: ScenarioHolders,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    onErrorShown: () -> Unit,
) {
    LaunchedEffect(mapScreenUiState.scenario) {
        val mapStyle = if (mapScreenUiState.isDrivingScenario()) {
            StandardStyles.TomTomOrbisMaps.DRIVING
        } else {
            StandardStyles.TomTomOrbisMaps.BROWSING
        }

        mapEnvironment.mapViewState.styleState.loadStyle(mapStyle)
    }

    LaunchedEffect(mapScreenUiState.zoomToAllMarkers) {
        mapEnvironment.mapViewState.cameraState.zoomToAllMarkers(padding = MARKERS_ZOOM_PADDING_DP)
    }

    val localDensity = LocalDensity.current
    mapEnvironment.mapViewState.safeArea = PaddingValues(
        start = safeAreaStartPadding(mapEnvironment.isDeviceInLandscape),
        bottom = localDensity.run { mapScreenUiState.safeAreaBottomPadding.toDp() },
        top = localDensity.run { mapScreenUiState.safeAreaTopPadding.toDp() },
    )

    Box(modifier = modifier) {
        MapView(
            mapEnvironment = mapEnvironment,
            mapScreenUiState = mapScreenUiState,
            mapCallbacks = callbacks,
        )

        ScenarioHost(
            scenario = mapScreenUiState.scenario,
            holders = holders,
        )

        ErrorSnackbarHost(
            errorStateFlow = errorStateFlow,
            modifier = modifier.align(Alignment.BottomCenter),
            onErrorShown = onErrorShown,
            snackbarHostState = snackbarHostState,
        )
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
