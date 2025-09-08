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

@file:Suppress("detekt:TooManyFunctions")

package com.example.application.search

import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.application.common.PlaceDetails
import com.example.application.common.SEARCH_BOTTOMSHEET_PEEK_HEIGHT
import com.example.application.common.locationDetails
import com.example.application.common.name
import com.example.application.common.ui.ArrowDownIconButton
import com.example.application.common.ui.BottomSheet
import com.example.application.common.ui.ClearSearchIconButton
import com.example.application.common.ui.PoiIconButton
import com.example.application.common.ui.SearchIcon
import com.example.application.search.SearchPanelState.Error
import com.example.application.search.SearchPanelState.Idle
import com.example.application.search.SearchPanelState.Loading
import com.example.application.search.SearchPanelState.NoResults
import com.example.application.search.SearchPanelState.ResultsFetched
import com.example.application.ui.theme.NavSdkExampleTheme
import com.tomtom.sdk.location.Address
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.Place
import com.tomtom.sdk.location.poi.StandardCategoryId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

private val searchSheetHeight
    @Composable
    get() = LocalDensity.current.run { LocalWindowInfo.current.containerSize.height.dp } -
        WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBottomSheet(searchStateHolder: SearchStateHolder) {
    SearchBottomSheet(
        isBottomPanelExpandedFlow = searchStateHolder.isBottomPanelExpandedFlow,
        searchPanelStateFlow = searchStateHolder.searchPanelStateFlow,
        inputQueryFlow = searchStateHolder.inputQueryFlow,
        searchResultsFlow = searchStateHolder.searchResultsFlow,
        scaffoldState = searchStateHolder.scaffoldState,
        onClearSearch = searchStateHolder.onClearSearch,
        onResetSearch = searchStateHolder.onResetSearch,
        onUpdateSearch = searchStateHolder.onUpdateSearch,
        onToggleBottomSheet = searchStateHolder.onToggleBottomSheet,
        onSearchResultClick = searchStateHolder.onSearchResultClick,
        onSearchPoiClick = searchStateHolder.onSearchPoiClick,
        isDeviceInLandscape = searchStateHolder.isDeviceInLandscape,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBottomSheet(
    isBottomPanelExpandedFlow: StateFlow<Boolean>,
    searchPanelStateFlow: StateFlow<SearchPanelState>,
    inputQueryFlow: StateFlow<String>,
    searchResultsFlow: StateFlow<List<SearchResultItemContent>>,
    scaffoldState: BottomSheetScaffoldState,
    onResetSearch: () -> Unit,
    onClearSearch: () -> Unit,
    onUpdateSearch: (String) -> Unit,
    onToggleBottomSheet: (Boolean?) -> Unit,
    onSearchResultClick: (PlaceDetails) -> Unit,
    onSearchPoiClick: (StandardCategoryId, String) -> Unit,
    isDeviceInLandscape: Boolean,
    modifier: Modifier = Modifier,
) {
    val searchPanelState by searchPanelStateFlow.collectAsStateWithLifecycle()
    val inputQuery by inputQueryFlow.collectAsStateWithLifecycle()
    val searchResults by searchResultsFlow.collectAsStateWithLifecycle()
    val isBottomPanelExpanded by isBottomPanelExpandedFlow.collectAsStateWithLifecycle()

    val focusRequester = remember { FocusRequester() }

    val lazyListState = rememberLazyListState()
    val isAtTop = remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex == 0 &&
                lazyListState.firstVisibleItemScrollOffset == 0
        }
    }

    BottomSheet(
        isExpanded = isBottomPanelExpanded,
        sheetPeekHeight = (SEARCH_BOTTOMSHEET_PEEK_HEIGHT).dp,
        onBottomSheetExpand = {
            onToggleBottomSheet(true)
            focusRequester.requestFocus()
        },
        onBottomSheetPartialExpand = {
            onToggleBottomSheet(false)
        },
        sheetSwipeEnabled = isAtTop.value,
        scaffoldState = scaffoldState,
        modifier = modifier,
        isDeviceInLandscape = isDeviceInLandscape,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .heightIn(
                    min = searchSheetHeight,
                    max = searchSheetHeight,
                ),
        ) {
            SearchField(
                query = inputQuery,
                isBottomPanelExpanded = isBottomPanelExpanded,
                focusRequester = focusRequester,
                onQueryTyped = { onUpdateSearch(it) },
                onClearSearchIconClicked = { onClearSearch() },
                onArrowDownIconClicked = { onResetSearch() },
                onToggleBottomSheet = { onToggleBottomSheet(it) },
                onSearch = { query -> onUpdateSearch(query) },
            )

            when (searchPanelState) {
                Loading -> SearchLoadingPanel(isExpanded = isBottomPanelExpanded)
                NoResults -> SearchStatusMessagePanel(
                    statusMessage = stringResource(id = R.string.search_error_empty_results),
                )

                Error -> SearchStatusMessagePanel(
                    statusMessage = stringResource(id = R.string.search_error_message),
                )

                ResultsFetched -> {
                    SearchResultList(
                        searchResults = searchResults,
                        onSearchResultClick = onSearchResultClick,
                        lazyListState = lazyListState,
                    )
                }

                Idle -> SearchPoiRow(
                    onSearchPoiClick = onSearchPoiClick,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
        }
    }
}

@Suppress("detekt:MagicNumber")
@PreviewLightDark
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBottomSheetPreview() {
    val searchResultItemContentList = List(12) {
        SearchResultItemContent(
            iconId = R.drawable.tt_asset_icon_fuel_fill_32,
            placeDetails = PlaceDetails(
                place = Place(
                    coordinate = GeoPoint(
                        latitude = 37.7749,
                        longitude = -122.4194,
                    ),
                    name = "Gas Station",
                    address = Address(
                        freeformAddress = "123 Main St, Springfield, USA",
                        countrySecondarySubdivision = "Springfield",
                        municipality = "Springfield",
                        countryCodeIso3 = "USA",
                    ),
                ),
            ),
            distance = "2.5 km",
        )
    }

    NavSdkExampleTheme {
        SearchBottomSheet(
            isBottomPanelExpandedFlow = MutableStateFlow(true),
            searchPanelStateFlow = MutableStateFlow(ResultsFetched),
            inputQueryFlow = MutableStateFlow("Search input"),
            searchResultsFlow = MutableStateFlow(searchResultItemContentList),
            scaffoldState = rememberBottomSheetScaffoldState(),
            onToggleBottomSheet = {},
            onClearSearch = {},
            onResetSearch = {},
            onUpdateSearch = {},
            onSearchResultClick = {},
            onSearchPoiClick = { _, _ -> },
            isDeviceInLandscape = false,
            modifier = Modifier,
        )
    }
}

@Composable
private fun SearchLoadingPanel(
    isExpanded: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.padding(
                top = if (isExpanded) 100.dp else 20.dp,
            ),
        )
    }
}

@PreviewLightDark
@Composable
private fun SearchLoadingPanelPreview() {
    NavSdkExampleTheme {
        SearchLoadingPanel(
            isExpanded = false,
            modifier = Modifier,
        )
    }
}

@Composable
private fun SearchStatusMessagePanel(
    statusMessage: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = statusMessage,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(top = 100.dp),
        )
    }
}

@PreviewLightDark
@Composable
private fun SearchStatusMessagePanelPreview() {
    NavSdkExampleTheme {
        SearchStatusMessagePanel(
            statusMessage = "This is the status message",
            modifier = Modifier,
        )
    }
}

@Composable
private fun SearchPoiRow(
    onSearchPoiClick: (StandardCategoryId, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth(),
    ) {
        poiOptions.forEach { poiOption ->
            val poiDescription = stringResource(getPoiDescriptionString(poiOption.key))
            PoiIconButton(
                imageVector = ImageVector.vectorResource(poiOption.value.imageVector),
                contentDescription = stringResource(poiOption.value.contentDescription),
                onClick = { onSearchPoiClick(poiOption.key, poiDescription) },
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun SearchPoiRowPreview() {
    NavSdkExampleTheme {
        SearchPoiRow(
            onSearchPoiClick = { _, _ -> },
        )
    }
}

@Composable
private fun SearchField(
    query: String,
    isBottomPanelExpanded: Boolean,
    onArrowDownIconClicked: () -> Unit,
    onClearSearchIconClicked: () -> Unit,
    onToggleBottomSheet: (Boolean?) -> Unit,
    onQueryTyped: (String) -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = FocusRequester(),
) {
    TextField(
        value = query,
        onValueChange = onQueryTyped,
        enabled = isBottomPanelExpanded,
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        placeholder = {
            Text(
                text = stringResource(id = R.string.search_content_description_search),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 18.sp,
            )
        },
        leadingIcon = {
            if (isBottomPanelExpanded || query.isNotEmpty()) {
                ArrowDownIconButton(
                    modifier = modifier,
                    onArrowDownIconClicked = onArrowDownIconClicked,
                )
            } else {
                SearchIcon()
            }
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                ClearSearchIconButton(
                    modifier = modifier,
                    onClearSearchIconClicked = onClearSearchIconClicked,
                )
            } else {
                Unit
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .focusRequester(focusRequester)
            .clickable { onToggleBottomSheet(null) }
            .onKeyEvent {
                return@onKeyEvent if (it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                    onSearch(query)
                    true
                } else {
                    false
                }
            },
    )
}

@PreviewLightDark
@Composable
private fun SearchFieldPreview() {
    NavSdkExampleTheme {
        SearchField(
            query = "the search query",
            isBottomPanelExpanded = true,
            modifier = Modifier,
            onArrowDownIconClicked = { },
            onClearSearchIconClicked = { },
            onToggleBottomSheet = { },
            onQueryTyped = { },
            onSearch = { },
        )
    }
}

@Composable
private fun SearchResultList(
    searchResults: List<SearchResultItemContent>,
    onSearchResultClick: (PlaceDetails) -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
) {
    LazyColumn(
        state = lazyListState,
        modifier = modifier,
    ) {
        items(items = searchResults) { searchResult ->
            SearchResultItem(
                item = searchResult,
                onSearchResultClick = onSearchResultClick,
            )
        }
    }
}

@Suppress("detekt:MagicNumber")
@PreviewLightDark
@Composable
private fun SearchResultListPreview() {
    val searchResultItemContentList = List(12) {
        SearchResultItemContent(
            iconId = R.drawable.tt_asset_icon_fuel_fill_32,
            placeDetails = PlaceDetails(
                place = Place(
                    coordinate = GeoPoint(
                        latitude = 37.7749,
                        longitude = -122.4194,
                    ),
                    name = "Gas Station",
                    address = Address(
                        freeformAddress = "123 Main St, Springfield, USA",
                        countrySecondarySubdivision = "Springfield",
                        municipality = "Springfield",
                        countryCodeIso3 = "USA",
                    ),
                ),
            ),
            distance = "2.5 km",
        )
    }

    NavSdkExampleTheme {
        SearchResultList(
            searchResults = searchResultItemContentList,
            onSearchResultClick = {},
            modifier = Modifier,
            lazyListState = rememberLazyListState(),
        )
    }
}

@Composable
fun SearchResultItem(
    item: SearchResultItemContent,
    onSearchResultClick: (PlaceDetails) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = modifier
            .background(color = MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .clickable(
                onClick = { onSearchResultClick(item.placeDetails) },
            ),
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(item.iconId),
            contentDescription = stringResource(id = R.string.search_content_description_search_result),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(32.dp)
                .padding(4.dp)
                .align(Alignment.CenterVertically),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(LOCATION_DESCRIPTION_COLUMN_WEIGHT)) {
            Text(
                text = item.placeDetails.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = item.placeDetails.locationDetails,
                modifier = modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            textAlign = TextAlign.Right,
            text = item.distance,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = modifier.padding(top = 2.dp),
        )
    }
}

private const val LOCATION_DESCRIPTION_COLUMN_WEIGHT = 0.8f

@PreviewLightDark
@Composable
private fun SearchResultItemPreview() {
    NavSdkExampleTheme {
        SearchResultItem(
            item = SearchResultItemContent(
                iconId = R.drawable.tt_asset_icon_fuel_fill_32,
                placeDetails = PlaceDetails(
                    place = Place(
                        coordinate = GeoPoint(
                            latitude = 37.7749,
                            longitude = -122.4194,
                        ),
                        name = "Gas Station",
                        address = Address(
                            freeformAddress = "123 Main St, Springfield, USA",
                            countrySecondarySubdivision = "Springfield",
                            municipality = "Springfield",
                            countryCodeIso3 = "USA",
                        ),
                    ),
                ),
                distance = "2.5 km",
            ),
            onSearchResultClick = {},
            modifier = Modifier,
        )
    }
}
