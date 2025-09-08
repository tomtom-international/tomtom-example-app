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

package com.example.demo.search.ev

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.application.common.CAMERA_OPTIONS_DEFAULT_TILT
import com.example.application.common.CAMERA_OPTIONS_POI_FOCUS_ZOOM
import com.example.application.common.TOMTOM_AMSTERDAM_OFFICE
import com.example.application.common.getPinMarkerProperties
import com.example.application.common.isDeviceInLandscape
import com.example.application.map.MapScreenUiState.ErrorState.SearchError
import com.example.application.search.EvSearchViewModel
import com.example.application.search.EvSearchViewModel.Companion.GEO_POINT_KEY
import com.example.application.search.EvSearchViewModel.Companion.LOCATION_PROVIDER_KEY
import com.example.application.search.EvSearchViewModel.Companion.SEARCH_FAILURE_KEY
import com.example.application.search.EvSearchViewModel.Companion.SEARCH_KEY
import com.example.application.search.EvSearchViewModel.Companion.SEARCH_SUCCESS_KEY
import com.example.application.search.SearchResultItemContent
import com.example.application.ui.CloseButton
import com.example.demo.DemoMap
import com.example.demo.DemoViewModel
import com.tomtom.sdk.annotations.AlphaSdkInitializationApi
import com.tomtom.sdk.entrypoint.TomTomSdk
import com.tomtom.sdk.map.display.annotation.BetaInitialCameraOptionsApi
import com.tomtom.sdk.map.display.camera.InitialCameraOptions
import com.tomtom.sdk.map.display.compose.BetaMapComposableApi
import com.tomtom.sdk.map.display.compose.model.MarkerData
import com.tomtom.sdk.map.display.compose.nodes.Marker
import com.tomtom.sdk.map.display.compose.state.rememberMapViewState
import com.tomtom.sdk.map.display.compose.state.rememberMarkerState
import com.tomtom.sdk.map.display.style.StyleMode
import com.tomtom.sdk.search.common.error.SearchFailure

@OptIn(
    BetaInitialCameraOptionsApi::class,
    BetaMapComposableApi::class,
    AlphaSdkInitializationApi::class,
)
@Composable
fun EvSearchScreen(
    demoViewModel: DemoViewModel,
    modifier: Modifier = Modifier,
    viewModel: EvSearchViewModel = viewModel(
        factory = EvSearchViewModel.Factory,
        extras = MutableCreationExtras().apply {
            set(LOCATION_PROVIDER_KEY, TomTomSdk.locationProvider)
            set(SEARCH_KEY, TomTomSdk.createSearch())
            set(GEO_POINT_KEY, TOMTOM_AMSTERDAM_OFFICE)
            set(SEARCH_SUCCESS_KEY) {
                demoViewModel.zoomToAllMarkers()
            }
            set(SEARCH_FAILURE_KEY) { _: SearchFailure ->
                demoViewModel.updateErrorState { SearchError }
            }
        },
    ),
) {
    val isDeviceInLandscape = isDeviceInLandscape()
    val mapUiState by demoViewModel.mapUiState.collectAsStateWithLifecycle()
    val mapInfrastructure by demoViewModel.mapInfrastructure.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val cameraOptions by demoViewModel.cameraOptions.collectAsStateWithLifecycle()
    val activeFilters by viewModel.activeFilters.collectAsStateWithLifecycle()
    val zoomToAllMarkers by demoViewModel.zoomToAllMarkers.collectAsStateWithLifecycle()
    val evSearchUiState by viewModel.evSearchUiState.collectAsStateWithLifecycle()
    val isBottomPanelExpanded by demoViewModel.isBottomSheetExpanded.collectAsStateWithLifecycle()
    val initialCameraOptions = InitialCameraOptions.LocationBased(position = TOMTOM_AMSTERDAM_OFFICE)

    val mapViewState = rememberMapViewState(initialCameraOptions = initialCameraOptions) {
        this.styleState.styleMode = StyleMode.MAIN
    }

    var showBottomPanel by rememberSaveable { mutableStateOf(false) }

    val onSearchResultClick = { searchResult: SearchResultItemContent ->
        showBottomPanel = true
        demoViewModel.toggleBottomSheet(false)
        viewModel.setEvPoiFocusDetails(searchResult.placeDetails)
        demoViewModel.updateCameraOptions(
            coordinate = searchResult.placeDetails.place.coordinate,
            zoom = CAMERA_OPTIONS_POI_FOCUS_ZOOM,
            tilt = CAMERA_OPTIONS_DEFAULT_TILT,
        )
    }

    Box(modifier = modifier) {
        DemoMap(
            mapUiState = mapUiState,
            mapInfrastructure = mapInfrastructure,
            isDeviceInLandscape = isDeviceInLandscape,
            mapViewState = mapViewState,
            cameraOptions = cameraOptions,
            zoomToAllMarkers = zoomToAllMarkers,
            disableGestures = true,
            onMapReady = { viewModel.performEvSearch() },
        ) {
            searchResults.forEach { searchResult ->
                Marker(
                    data = MarkerData(geoPoint = searchResult.placeDetails.place.coordinate),
                    properties = getPinMarkerProperties(R.drawable.outline_ev_station_24px),
                    state = rememberMarkerState(),
                    onClick = { onSearchResultClick(searchResult) },
                )
            }
        }

        if (showBottomPanel) {
            CloseButton(
                onClick = {
                    showBottomPanel = false
                    viewModel.setEvPoiFocusDetails(null)
                    demoViewModel.zoomToAllMarkers()
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(
                        top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                        end = 16.dp,
                    ),
            )

            evSearchUiState.evPoiFocusDetails?.let {
                EvPoiFocusBottomSheet(
                    placeDetails = it,
                    isExpanded = isBottomPanelExpanded,
                    onBottomSheetExpand = { demoViewModel.toggleBottomSheet(true) },
                    onBottomSheetPartialExpand = { demoViewModel.toggleBottomSheet(false) },
                    isDeviceInLandscape = isDeviceInLandscape,
                )
            }
        } else {
            EvSearchBottomSheet(
                activeFilters = activeFilters,
                isExpanded = isBottomPanelExpanded,
                searchResults = searchResults,
                selectedFilterCategory = evSearchUiState.selectedFilterCategory,
                onSearchResultClick = { searchResultItemContent: SearchResultItemContent ->
                    onSearchResultClick(searchResultItemContent)
                },
                onBottomSheetPartialExpand = {
                    demoViewModel.toggleBottomSheet(false)
                    viewModel.setSelectedFilterCategory(null)
                },
                onBottomSheetExpand = {
                    demoViewModel.toggleBottomSheet(true)
                },
                onBackArrowClick = {
                    if (evSearchUiState.selectedFilterCategory != null) {
                        viewModel.setSelectedFilterCategory(null)
                    } else {
                        demoViewModel.toggleBottomSheet(false)
                    }
                },
                onResetClick = {
                    viewModel.setSelectedFilterCategory(null)
                    viewModel.resetActiveFilters()
                },
                onEvFilterCategoryClick = { filterCategory ->
                    demoViewModel.toggleBottomSheet(true)
                    if (filterCategory == evSearchUiState.selectedFilterCategory) {
                        viewModel.setSelectedFilterCategory(null)
                    } else {
                        viewModel.setSelectedFilterCategory(filterCategory)
                    }
                },
                onEvFilterOptionClick = { evFilterCategory, evFilterOption ->
                    if (evFilterOption.isOptionSelected(evFilterCategory, activeFilters)) {
                        viewModel.removeActiveFilter(
                            category = evFilterCategory,
                            filterOption = evFilterOption,
                        )
                    } else {
                        viewModel.addActiveFilter(
                            category = evFilterCategory,
                            filterOption = evFilterOption,
                        )
                    }
                },
                isDeviceInLandscape = isDeviceInLandscape,
                modifier = Modifier,
            )
        }
    }
}
