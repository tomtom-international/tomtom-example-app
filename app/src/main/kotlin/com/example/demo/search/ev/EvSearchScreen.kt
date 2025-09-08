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
import com.example.application.common.ui.CloseButton
import com.example.application.common.ui.getPinMarkerProperties
import com.example.application.common.ui.isDeviceInLandscape
import com.example.application.map.model.MapScreenUiState.ErrorState.SearchError
import com.example.application.search.SearchResultItemContent
import com.example.application.search.ev.EvSearchViewModel
import com.example.application.search.ev.EvSearchViewModel.Companion.GEO_POINT_KEY
import com.example.application.search.ev.EvSearchViewModel.Companion.LOCATION_PROVIDER_KEY
import com.example.application.search.ev.EvSearchViewModel.Companion.SEARCH_FAILURE_KEY
import com.example.application.search.ev.EvSearchViewModel.Companion.SEARCH_KEY
import com.example.application.search.ev.EvSearchViewModel.Companion.SEARCH_SUCCESS_KEY
import com.example.demo.DemoMap
import com.example.demo.DemoViewModel
import com.tomtom.sdk.init.TomTomSdk
import com.tomtom.sdk.init.createSearch
import com.tomtom.sdk.map.display.camera.InitialCameraOptions
import com.tomtom.sdk.map.display.compose.model.MarkerData
import com.tomtom.sdk.map.display.compose.nodes.Marker
import com.tomtom.sdk.map.display.compose.state.rememberMapViewState
import com.tomtom.sdk.map.display.compose.state.rememberMarkerState
import com.tomtom.sdk.map.display.style.StyleMode
import com.tomtom.sdk.search.common.error.SearchFailure

/** EV charging search demo screen.
 * Shows nearby charging stations and a filterable bottom sheet; focuses the map on selection.
 */
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
    val mapDisplayInfrastructure by demoViewModel.mapDisplayInfrastructure.collectAsStateWithLifecycle()
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
            mapDisplayInfrastructure = mapDisplayInfrastructure,
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
                    properties = getPinMarkerProperties(R.drawable.tt_asset_icon_evcharger_fill_32),
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
