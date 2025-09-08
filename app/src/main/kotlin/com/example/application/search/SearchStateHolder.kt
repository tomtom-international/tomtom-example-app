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

package com.example.application.search

import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Stable
import com.example.application.common.PlaceDetails
import com.tomtom.sdk.location.poi.StandardCategoryId
import kotlinx.coroutines.flow.StateFlow

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
