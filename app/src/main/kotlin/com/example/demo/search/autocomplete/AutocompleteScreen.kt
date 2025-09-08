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

package com.example.demo.search.autocomplete

import android.view.KeyEvent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.application.common.BottomSheet
import com.example.application.common.TOMTOM_AMSTERDAM_OFFICE
import com.example.application.common.fillMaxWidthByOrientation
import com.example.application.common.getPinMarkerProperties
import com.example.application.common.isDeviceInLandscape
import com.example.application.extension.toPx
import com.example.application.map.MapScreenUiState.ErrorState.SearchError
import com.example.application.search.SearchResultItem
import com.example.application.search.SearchResultItemContent
import com.example.application.ui.SearchIcon
import com.example.demo.DemoMap
import com.example.demo.DemoViewModel
import com.example.demo.search.autocomplete.AutocompleteViewModel.Companion.SEARCH_KEY
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
import com.tomtom.sdk.map.display.visualization.navigation.compose.NavigationVisualization
import com.tomtom.sdk.search.common.error.SearchFailure
import com.tomtom.sdk.search.model.result.AutocompleteSegment
import kotlinx.coroutines.flow.StateFlow

@OptIn(
    BetaMapComposableApi::class,
    BetaInitialCameraOptionsApi::class,
    AlphaSdkInitializationApi::class,
)
@Composable
fun AutocompleteScreen(
    demoViewModel: DemoViewModel,
    modifier: Modifier = Modifier,
    viewModel: AutocompleteViewModel = viewModel(
        factory = AutocompleteViewModel.Factory,
        extras = MutableCreationExtras().apply {
            set(SEARCH_KEY, TomTomSdk.createSearch())
        },
    ),
) {
    val isDeviceInLandscape = isDeviceInLandscape()
    val mapUiState by demoViewModel.mapUiState.collectAsStateWithLifecycle()
    val mapInfrastructure by demoViewModel.mapInfrastructure.collectAsStateWithLifecycle()
    val navigationInfrastructure by demoViewModel.navigationInfrastructure.collectAsStateWithLifecycle()
    val zoomToAllMarkers by demoViewModel.zoomToAllMarkers.collectAsStateWithLifecycle()
    val isBottomPanelExpanded by demoViewModel.isBottomSheetExpanded.collectAsStateWithLifecycle()

    val initialCameraOptions = InitialCameraOptions.LocationBased(position = TOMTOM_AMSTERDAM_OFFICE)

    val mapViewState = rememberMapViewState(initialCameraOptions = initialCameraOptions) {
        this.styleState.styleMode = StyleMode.MAIN
    }

    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()

    Box(modifier = modifier) {
        DemoMap(
            mapUiState = mapUiState,
            mapInfrastructure = mapInfrastructure,
            isDeviceInLandscape = isDeviceInLandscape,
            zoomToAllMarkers = zoomToAllMarkers,
            disableGestures = true,
            mapViewState = mapViewState,
        ) {
            NavigationVisualization(infrastructure = navigationInfrastructure)

            searchResults.forEach { searchResult ->
                Marker(
                    data = MarkerData(geoPoint = searchResult.placeDetails.place.coordinate),
                    properties = getPinMarkerProperties(searchResult.iconId),
                    state = rememberMarkerState(),
                )
            }
        }

        val sheetPeekHeight = remember { 150.dp }
        val localDensity = LocalDensity.current

        LaunchedEffect(Unit) {
            demoViewModel.updateSafeAreaTopPadding(0)
            demoViewModel.updateSafeAreaBottomPadding(sheetPeekHeight.toPx(localDensity))
        }

        val onSearchSuccess = remember {
            {
                demoViewModel.zoomToAllMarkers()
                demoViewModel.toggleBottomSheet(false)
            }
        }
        val onSearchFailure = remember {
            { _: SearchFailure ->
                demoViewModel.updateErrorState { SearchError }
            }
        }

        BottomPanel(
            isBottomPanelExpanded = isBottomPanelExpanded,
            inputQueryFlow = viewModel.inputQuery,
            autocompleteResultsFlow = viewModel.autocompleteResults,
            searchResults = searchResults,
            sheetPeekHeight = sheetPeekHeight,
            onClearAutocompleteResults = { viewModel.clearAutocompleteResults() },
            onToggleBottomSheet = { demoViewModel.toggleBottomSheet(it) },
            onSetInputQuery = { viewModel.setInputQuery(it) },
            onSelectAutocompleteOption = { segment ->
                viewModel.selectAutocompleteOption(
                    segment = segment,
                    onSearchSuccess = onSearchSuccess,
                    onSearchFailure = onSearchFailure,
                )
            },
            onSearch = { query ->
                viewModel.performSearch(
                    query = query,
                    onSearchSuccess = onSearchSuccess,
                    onSearchFailure = onSearchFailure,
                )
            },
            isDeviceInLandscape = isDeviceInLandscape,
            modifier = Modifier
                .fillMaxWidthByOrientation(isDeviceInLandscape)
                .align(Alignment.BottomStart),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomPanel(
    isBottomPanelExpanded: Boolean,
    inputQueryFlow: StateFlow<String>,
    autocompleteResultsFlow: StateFlow<List<AutocompleteResultItemContent>>,
    searchResults: List<SearchResultItemContent>,
    sheetPeekHeight: Dp,
    onClearAutocompleteResults: () -> Unit,
    onToggleBottomSheet: (Boolean?) -> Unit,
    onSetInputQuery: (String) -> Unit,
    onSelectAutocompleteOption: (AutocompleteSegment) -> Unit,
    onSearch: (String) -> Unit,
    isDeviceInLandscape: Boolean,
    modifier: Modifier = Modifier,
) {
    val searchFocusRequester = remember { FocusRequester() }
    BottomSheet(
        isExpanded = isBottomPanelExpanded,
        sheetPeekHeight = sheetPeekHeight,
        onBottomSheetExpand = {
            onToggleBottomSheet(true)
            searchFocusRequester.requestFocus()
        },
        onBottomSheetPartialExpand = {
            onToggleBottomSheet(false)
            onClearAutocompleteResults()
        },
        modifier = modifier,
        isDeviceInLandscape = isDeviceInLandscape,
    ) {
        Column {
            AutocompleteSearchField(
                isBottomPanelExpanded = isBottomPanelExpanded,
                inputQueryFlow = inputQueryFlow,
                searchFocusRequester = searchFocusRequester,
                autocompleteResultsFlow = autocompleteResultsFlow,
                onClearAutocompleteResults = onClearAutocompleteResults,
                onToggleBottomSheet = onToggleBottomSheet,
                onSetInputQuery = onSetInputQuery,
                onSelectAutocompleteOption = onSelectAutocompleteOption,
                onSearch = onSearch,
            )

            LazyColumn(modifier = Modifier.fillMaxHeight()) {
                items(items = searchResults) { searchResult ->
                    SearchResultItem(item = searchResult, onSearchResultClick = {})
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AutocompleteSearchField(
    isBottomPanelExpanded: Boolean,
    inputQueryFlow: StateFlow<String>,
    searchFocusRequester: FocusRequester,
    autocompleteResultsFlow: StateFlow<List<AutocompleteResultItemContent>>,
    onClearAutocompleteResults: () -> Unit,
    onToggleBottomSheet: (Boolean?) -> Unit,
    onSetInputQuery: (String) -> Unit,
    onSelectAutocompleteOption: (AutocompleteSegment) -> Unit,
    onSearch: (String) -> Unit,
) {
    val inputQuery by inputQueryFlow.collectAsStateWithLifecycle()
    val autocompleteResults by autocompleteResultsFlow.collectAsStateWithLifecycle()

    ExposedDropdownMenuBox(
        expanded = autocompleteResults.isNotEmpty(),
        onExpandedChange = { },
    ) {
        TextField(
            value = inputQuery,
            onValueChange = { onSetInputQuery(it) },
            leadingIcon = { SearchIcon() },
            enabled = isBottomPanelExpanded,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryEditable)
                .fillMaxWidth()
                .focusRequester(searchFocusRequester)
                .clickable {
                    onToggleBottomSheet(null)
                }
                .onKeyEvent {
                    return@onKeyEvent if (it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                        onSearch(inputQuery)
                        true
                    } else {
                        false
                    }
                },
        )
        ExposedDropdownMenu(
            expanded = autocompleteResults.isNotEmpty(),
            onDismissRequest = { onClearAutocompleteResults() },
        ) {
            autocompleteResults.forEach { selectionOption ->
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            imageVector = selectionOption.imageVector,
                            contentDescription = "",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    },
                    text = { Text(text = selectionOption.text) },
                    onClick = {
                        onSelectAutocompleteOption(selectionOption.segment)
                        onSetInputQuery(selectionOption.text)
                    },
                )
            }
        }
    }
}
