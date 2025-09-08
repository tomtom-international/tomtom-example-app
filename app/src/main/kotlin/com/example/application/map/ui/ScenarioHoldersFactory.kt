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

package com.example.application.map.ui

import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import com.example.application.common.PlaceDetails
import com.example.application.common.ui.RecenterMapStateHolder
import com.example.application.map.MapScreenViewModel
import com.example.application.map.RoutesViewModel
import com.example.application.map.model.MapScreenAction.CleanRoutePreview
import com.example.application.map.model.MapScreenAction.ClearMap
import com.example.application.map.model.MapScreenAction.RecenterMap
import com.example.application.map.model.MapScreenAction.ShowRoutePreview
import com.example.application.map.model.MapScreenAction.ShowRoutingFailure
import com.example.application.map.model.MapScreenAction.ShowSearchResultFocus
import com.example.application.map.model.MapScreenAction.StopGuidance
import com.example.application.map.model.MapScreenAction.ToggleBottomSheet
import com.example.application.map.model.MapScreenAction.ToggleCameraTrackingMode
import com.example.application.map.model.MapScreenUiState
import com.example.application.map.model.ScenarioHolders
import com.example.application.map.scenarios.arrival.ArrivalStateHolder
import com.example.application.map.scenarios.freedriving.FreeDrivingStateHolder
import com.example.application.map.scenarios.guidance.GuidanceStateHolder
import com.example.application.map.scenarios.home.HomeStateHolder
import com.example.application.map.scenarios.poifocus.PoiFocusStateHolder
import com.example.application.map.scenarios.routepreview.RoutePreviewStateHolder
import com.example.application.search.SearchStateHolder
import com.example.application.search.SearchViewModel
import com.tomtom.sdk.location.poi.StandardCategoryId
import com.tomtom.sdk.map.display.camera.CameraOptions

/**
 * Creates and remembers the set of state holders for all map scenarios, wiring UI callbacks
 * to the relevant view models and screen actions.
 *
 * @param mapScreenUiState current map screen UI state that drives what holders are needed.
 * @param routesViewModel view model providing routing data and actions.
 * @param mapScreenViewModel parent screen view model used to dispatch map actions.
 * @param searchViewModel view model that manages search queries and results.
 * @param isDeviceInLandscape whether the device is currently in landscape orientation.
 * @param snackbarHostState snackbar host used to show transient messages.
 * @param onSettingsClick invoked when the settings button is clicked.
 * @param onAnimateCamera called to animate the map camera to the provided options.
 * @param onStartGuidance invoked to start turn‑by‑turn guidance.
 * @return container with all scenario-specific holders used by ScenarioHost.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberScenarioHolders(
    mapScreenUiState: MapScreenUiState,
    routesViewModel: RoutesViewModel,
    mapScreenViewModel: MapScreenViewModel,
    searchViewModel: SearchViewModel,
    isDeviceInLandscape: Boolean,
    snackbarHostState: SnackbarHostState,
    onSettingsClick: () -> Unit,
    onAnimateCamera: (CameraOptions) -> Unit,
    onStartGuidance: () -> Unit,
): ScenarioHolders {
    val recenterMapStateHolder = remember(mapScreenUiState.isInteractiveMode) {
        RecenterMapStateHolder(mapScreenUiState.isInteractiveMode) { mapScreenViewModel.dispatchAction(RecenterMap) }
    }

    val onClearClick = remember { { mapScreenViewModel.dispatchAction(ClearMap) } }
    val onSafeAreaTopPaddingUpdate = remember<(Int) -> Unit> { { mapScreenViewModel.updateSafeAreaTopPadding(it) } }
    val onSafeAreaBottomPaddingUpdate = remember<(Int) -> Unit> {
        { mapScreenViewModel.updateSafeAreaBottomPadding(it) }
    }

    val density = LocalDensity.current

    val searchScaffoldState = remember(mapScreenUiState.scenario) {
        val sheetState = SheetState(
            skipPartiallyExpanded = false,
            density = density,
            initialValue = SheetValue.PartiallyExpanded,
        )
        BottomSheetScaffoldState(
            bottomSheetState = sheetState,
            snackbarHostState = snackbarHostState,
        )
    }

    val searchStateHolder = remember(
        mapScreenViewModel.isBottomSheetExpanded,
        searchViewModel.searchPanelState,
        searchViewModel.inputQuery,
        searchViewModel.searchResults,
        isDeviceInLandscape,
        searchScaffoldState,
    ) {
        SearchStateHolder(
            isBottomPanelExpandedFlow = mapScreenViewModel.isBottomSheetExpanded,
            searchPanelStateFlow = searchViewModel.searchPanelState,
            inputQueryFlow = searchViewModel.inputQuery,
            searchResultsFlow = searchViewModel.searchResults,
            scaffoldState = searchScaffoldState,
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
    }

    val homeStateHolder = remember(
        mapScreenUiState.poiPlaces,
        recenterMapStateHolder,
        searchStateHolder,
        mapScreenViewModel.userLocation,
        isDeviceInLandscape,
        onSafeAreaTopPaddingUpdate,
        onSafeAreaBottomPaddingUpdate,
    ) {
        HomeStateHolder(
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
    }

    val poiFocusStateHolder = remember(
        recenterMapStateHolder,
        mapScreenUiState.placeDetails,
        onAnimateCamera,
        onClearClick,
        mapScreenViewModel.locationProvider,
        isDeviceInLandscape,
        onSafeAreaTopPaddingUpdate,
        onSafeAreaBottomPaddingUpdate,
    ) {
        PoiFocusStateHolder(
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
    }

    val routePreviewStateHolder = remember(
        recenterMapStateHolder,
        routesViewModel.routes,
        onClearClick,
        isDeviceInLandscape,
        onSafeAreaTopPaddingUpdate,
        onSafeAreaBottomPaddingUpdate,
        onStartGuidance,
    ) {
        RoutePreviewStateHolder(
            recenterMapStateHolder = recenterMapStateHolder,
            routesFlow = routesViewModel.routes,
            onClearClick = onClearClick,
            onDriveButtonClick = { onStartGuidance() },
            onSimulateButtonClick = { onStartGuidance() },
            isDeviceInLandscape = isDeviceInLandscape,
            onSafeAreaTopPaddingUpdate = onSafeAreaTopPaddingUpdate,
            onSafeAreaBottomPaddingUpdate = onSafeAreaBottomPaddingUpdate,
            onBackClick = { mapScreenViewModel.dispatchAction(CleanRoutePreview { routesViewModel.clearRoutes() }) },
        )
    }

    val guidanceStateHolder = remember(
        recenterMapStateHolder,
        routesViewModel.selectedRoute,
        mapScreenViewModel.locationContext,
        mapScreenViewModel.routeProgress,
        mapScreenViewModel.nextInstruction,
        mapScreenViewModel.upcomingHorizonElements,
        mapScreenViewModel.laneGuidance,
        mapScreenUiState.cameraTrackingMode,
        isDeviceInLandscape,
        onSafeAreaTopPaddingUpdate,
        onSafeAreaBottomPaddingUpdate,
    ) {
        GuidanceStateHolder(
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
            isDeviceInLandscape = isDeviceInLandscape,
            onSafeAreaTopPaddingUpdate = onSafeAreaTopPaddingUpdate,
            onSafeAreaBottomPaddingUpdate = onSafeAreaBottomPaddingUpdate,
        )
    }

    val freeDrivingStateHolder = remember(
        recenterMapStateHolder,
        searchStateHolder,
        mapScreenViewModel.locationContext,
        mapScreenViewModel.upcomingHorizonElements,
        isDeviceInLandscape,
        onSafeAreaTopPaddingUpdate,
        onSafeAreaBottomPaddingUpdate,
    ) {
        FreeDrivingStateHolder(
            recenterMapStateHolder = recenterMapStateHolder,
            searchStateHolder = searchStateHolder,
            locationContext = mapScreenViewModel.locationContext,
            isDeviceInLandscape = isDeviceInLandscape,
            onSafeAreaTopPaddingUpdate = onSafeAreaTopPaddingUpdate,
            onSafeAreaBottomPaddingUpdate = onSafeAreaBottomPaddingUpdate,
            horizonElementsFlow = mapScreenViewModel.upcomingHorizonElements,
        )
    }

    val arrivalStateHolder = remember(
        recenterMapStateHolder,
        mapScreenUiState.destinationDetails,
        isDeviceInLandscape,
        onSafeAreaTopPaddingUpdate,
        onSafeAreaBottomPaddingUpdate,
        onClearClick,
    ) {
        ArrivalStateHolder(
            recenterMapStateHolder = recenterMapStateHolder,
            destinationDetails = mapScreenUiState.destinationDetails,
            onArrivalButtonClick = onClearClick,
            isDeviceInLandscape = isDeviceInLandscape,
            onSafeAreaTopPaddingUpdate = onSafeAreaTopPaddingUpdate,
            onSafeAreaBottomPaddingUpdate = onSafeAreaBottomPaddingUpdate,
        )
    }

    val scenarioHolders = remember(
        homeStateHolder,
        poiFocusStateHolder,
        routePreviewStateHolder,
        guidanceStateHolder,
        freeDrivingStateHolder,
        arrivalStateHolder,
    ) {
        ScenarioHolders(
            homeStateHolder = homeStateHolder,
            poiFocusStateHolder = poiFocusStateHolder,
            routePreviewStateHolder = routePreviewStateHolder,
            guidanceStateHolder = guidanceStateHolder,
            freeDrivingStateHolder = freeDrivingStateHolder,
            arrivalStateHolder = arrivalStateHolder,
        )
    }

    return scenarioHolders
}
