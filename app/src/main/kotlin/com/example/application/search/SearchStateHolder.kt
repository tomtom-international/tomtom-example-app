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

package com.example.application.search

import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Stable
import com.example.application.common.PlaceDetails
import com.tomtom.sdk.location.poi.StandardCategoryId
import kotlinx.coroutines.flow.StateFlow

/**
 * UI state and callbacks for the Search panel.
 *
 * @param isBottomPanelExpandedFlow: whether the bottom sheet is currently expanded.
 * @param searchPanelStateFlow: current state of the search panel (idle, results, etc.).
 * @param inputQueryFlow: current query text entered by the user.
 * @param searchResultsFlow: stream of search results to display.
 * @param scaffoldState: scaffold state controlling the Material bottom sheet.
 * @param onClearSearch: clears the current search query and results.
 * @param onResetSearch: resets panel to initial state.
 * @param onUpdateSearch: updates the search query.
 * @param onToggleBottomSheet: expands/collapses the bottom sheet (null toggles).
 * @param onSearchResultClick: invoked when a result is selected.
 * @param onSearchPoiClick: invoked when a POI category chip is selected.
 * @param isDeviceInLandscape: whether the device is currently in landscape orientation.
 */
@Stable
@OptIn(ExperimentalMaterial3Api::class)
data class SearchStateHolder(
    val isBottomPanelExpandedFlow: StateFlow<Boolean>,
    val searchPanelStateFlow: StateFlow<SearchPanelState>,
    val inputQueryFlow: StateFlow<String>,
    val searchResultsFlow: StateFlow<List<SearchResultItemContent>>,
    val scaffoldState: BottomSheetScaffoldState,
    val onClearSearch: () -> Unit,
    val onResetSearch: () -> Unit,
    val onUpdateSearch: (String) -> Unit,
    val onToggleBottomSheet: (Boolean?) -> Unit,
    val onSearchResultClick: (PlaceDetails) -> Unit,
    val onSearchPoiClick: (StandardCategoryId, String) -> Unit,
    val isDeviceInLandscape: Boolean,
)
