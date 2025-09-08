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

@file:Suppress("detekt:TooManyFunctions")

package com.example.demo.search.ev

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.application.common.BOTTOMSHEET_PADDING_TOP
import com.example.application.common.BottomSheet
import com.example.application.common.SEARCH_BOTTOMSHEET_PEEK_HEIGHT
import com.example.application.common.fillMaxWidthByOrientation
import com.example.application.search.EvFilter
import com.example.application.search.EvFilterCategory
import com.example.application.search.EvFilterOption
import com.example.application.search.SearchResultItemContent
import com.example.application.search.evFilterCategories
import com.example.application.ui.CustomIconButton
import com.example.application.ui.theme.NavSdkExampleTheme

private val evSearchSheetHeight
    @Composable
    get() = LocalDensity.current.run { LocalWindowInfo.current.containerSize.height.dp } -
        WindowInsets.statusBars.asPaddingValues().calculateTopPadding() -
        (BOTTOMSHEET_PADDING_TOP).dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvSearchBottomSheet(
    isExpanded: Boolean,
    activeFilters: SnapshotStateMap<EvFilterCategory, Set<EvFilterOption>>,
    searchResults: List<SearchResultItemContent>,
    onBottomSheetExpand: () -> Unit,
    onBottomSheetPartialExpand: () -> Unit,
    onBackArrowClick: () -> Unit,
    onResetClick: () -> Unit,
    onSearchResultClick: (SearchResultItemContent) -> Unit,
    onEvFilterCategoryClick: (EvFilterCategory) -> Unit,
    onEvFilterOptionClick: (EvFilterCategory, EvFilterOption) -> Unit,
    modifier: Modifier = Modifier,
    selectedFilterCategory: EvFilterCategory? = null,
    isDeviceInLandscape: Boolean,
) {
    val lazyListState = rememberLazyListState()
    val isAtTop = remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex == 0 &&
                lazyListState.firstVisibleItemScrollOffset == 0
        }
    }

    BottomSheet(
        isExpanded = isExpanded,
        sheetSwipeEnabled = isAtTop.value,
        sheetPeekHeight = (SEARCH_BOTTOMSHEET_PEEK_HEIGHT).dp,
        onBottomSheetExpand = onBottomSheetExpand,
        onBottomSheetPartialExpand = onBottomSheetPartialExpand,
        modifier = modifier.fillMaxWidthByOrientation(isDeviceInLandscape),
        isDeviceInLandscape = isDeviceInLandscape,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .heightIn(
                    min = evSearchSheetHeight,
                    max = evSearchSheetHeight,
                ),
        ) {
            if (isExpanded) {
                EvSearchBottomSheetExpandedHeader(
                    onBackArrowClick = onBackArrowClick,
                    onResetClick = onResetClick,
                )
            } else {
                EvSearchBottomSheetCollapsedHeader(
                    onFilterIconClick = onBottomSheetExpand,
                )
            }

            EvFilterCategoryRow(
                activeFilters = activeFilters,
                categorySelected = selectedFilterCategory,
                onEvFilterCategoryClick = { onEvFilterCategoryClick(it) },
                modifier = Modifier.padding(top = 8.dp),
            )

            if (isExpanded) {
                if (selectedFilterCategory != null) {
                    EvFilterOptionPanel(
                        activeFilters = activeFilters,
                        selectedFilterCategory = selectedFilterCategory,
                        onEvFilterOptionClick = { evFilterCategory, evFilterOption ->
                            onEvFilterOptionClick(evFilterCategory, evFilterOption)
                        },
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }

                EvSearchResultList(
                    searchResults = searchResults,
                    onSearchResultClick = onSearchResultClick,
                    lazyListState = lazyListState,
                )
            }
        }
    }
}

@Composable
private fun EvSearchBottomSheetCollapsedHeader(
    onFilterIconClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f),
        ) {
            Text(
                text = stringResource(R.string.demo_search_ev_title_bottom_panel),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(R.string.demo_search_ev_subtitle_bottom_panel),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
        }

        CustomIconButton(
            imageVector = ImageVector.vectorResource(id = R.drawable.outline_filter_alt_24px),
            modifier = Modifier.padding(end = 16.dp),
            onIconClick = onFilterIconClick,
        )
    }
}

@Composable
private fun EvSearchBottomSheetExpandedHeader(
    onBackArrowClick: () -> Unit,
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        CustomIconButton(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            modifier = Modifier.padding(start = 16.dp),
            onIconClick = onBackArrowClick,
        )
        Text(
            text = stringResource(R.string.demo_search_ev_title_bottom_panel_expanded),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(R.string.demo_search_ev_button_reset_filters),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .clickable { onResetClick() }
                .padding(end = 24.dp),
        )
    }
}

@Composable
private fun EvFilterCategoryRow(
    activeFilters: SnapshotStateMap<EvFilterCategory, Set<EvFilterOption>>,
    categorySelected: EvFilterCategory?,
    onEvFilterCategoryClick: (EvFilterCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .horizontalScroll(scrollState),
    ) {
        evFilterCategories.forEach { (_, it) ->
            EvFilterCategory(
                imageVector = ImageVector.vectorResource(id = it.imageVector),
                isCategorySelected = it == categorySelected,
                isCategoryActive = it.isCategoryActive(activeFilters),
                onEvFilterCategoryClick = { onEvFilterCategoryClick(it) },
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
    }
}

@Composable
private fun EvFilterCategory(
    imageVector: ImageVector,
    isCategorySelected: Boolean,
    isCategoryActive: Boolean,
    onEvFilterCategoryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tint = if (isCategorySelected) {
        MaterialTheme.colorScheme.inversePrimary
    } else {
        MaterialTheme.colorScheme.primary
    }

    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(shape = RoundedCornerShape(5.dp))
            .clickable { onEvFilterCategoryClick() }
            .border(
                width = 2.dp,
                color = tint,
                shape = RoundedCornerShape(5.dp),
            )
            .background(
                if (isCategorySelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else if (isCategoryActive) {
                    MaterialTheme.colorScheme.inversePrimary
                } else {
                    MaterialTheme.colorScheme.background
                },
            )
            .padding(vertical = 8.dp, horizontal = 24.dp),
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = stringResource(R.string.demo_search_ev_content_description_category_icon),
            tint = tint,
            modifier = Modifier.padding(end = 8.dp),
        )

        Icon(
            imageVector = if (isCategorySelected) {
                Icons.Default.KeyboardArrowUp
            } else {
                Icons.Default.KeyboardArrowDown
            },
            contentDescription = stringResource(R.string.demo_search_ev_content_description_category_arrow),
            tint = tint,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EvFilterOptionPanel(
    selectedFilterCategory: EvFilterCategory,
    activeFilters: SnapshotStateMap<EvFilterCategory, Set<EvFilterOption>>,
    onEvFilterOptionClick: (EvFilterCategory, EvFilterOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .wrapContentHeight()
                .padding(start = 16.dp),
        ) {
            Text(
                text = stringResource(selectedFilterCategory.filterDescription),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary,
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.secondary,
            )
        }

        val scrollState = rememberScrollState()

        FlowRow(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .verticalScroll(scrollState),
        ) {
            selectedFilterCategory.filterOptions.forEach { filterOption ->
                EvFilterOption(
                    imageVector = ImageVector.vectorResource(id = filterOption.imageVector),
                    optionDescription = stringResource(filterOption.filterOptionDescription),
                    isOptionSelected = filterOption.isOptionSelected(selectedFilterCategory, activeFilters),
                    onEvFilterOptionClick = { onEvFilterOptionClick(selectedFilterCategory, filterOption) },
                )
            }
        }
    }
}

@Composable
private fun EvFilterOption(
    imageVector: ImageVector,
    optionDescription: String,
    isOptionSelected: Boolean,
    onEvFilterOptionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(8.dp)
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable { onEvFilterOptionClick() }
            .background(
                if (isOptionSelected) {
                    MaterialTheme.colorScheme.inversePrimary
                } else {
                    MaterialTheme.colorScheme.background
                },
            )
            .padding(vertical = 8.dp, horizontal = 16.dp),
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = "Charging speed",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 8.dp),
        )

        Text(
            text = optionDescription,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Suppress("detekt:MagicNumber")
@PreviewLightDark
@Composable
private fun EvSearchBottomSheetPreview() {
    NavSdkExampleTheme {
        EvSearchBottomSheet(
            activeFilters = SnapshotStateMap(),
            searchResults = emptyList(),
            selectedFilterCategory = null,
            isExpanded = false,
            onSearchResultClick = {},
            onBottomSheetExpand = {},
            onBottomSheetPartialExpand = {},
            onEvFilterCategoryClick = {},
            onEvFilterOptionClick = { _, _ -> },
            onBackArrowClick = {},
            onResetClick = {},
            modifier = Modifier,
            isDeviceInLandscape = false,
        )
    }
}

@PreviewLightDark
@Composable
private fun EvSearchBottomSheetCollapsedHeaderPreview() {
    NavSdkExampleTheme {
        EvSearchBottomSheetCollapsedHeader(
            onFilterIconClick = {},
            modifier = Modifier,
        )
    }
}

@PreviewLightDark
@Composable
private fun EvSearchBottomSheetExpandedHeaderPreview() {
    NavSdkExampleTheme {
        EvSearchBottomSheetExpandedHeader(
            onBackArrowClick = {},
            onResetClick = {},
            modifier = Modifier,
        )
    }
}

@PreviewLightDark
@Composable
private fun EvFilterCategoryRowPreview() {
    NavSdkExampleTheme {
        EvFilterCategoryRow(
            onEvFilterCategoryClick = { },
            activeFilters = SnapshotStateMap(),
            categorySelected = null,
            modifier = Modifier,
        )
    }
}

@PreviewLightDark
@Composable
private fun EvFilterCategoryRowCategoryActivePreview() {
    val testMap = SnapshotStateMap<EvFilterCategory, Set<EvFilterOption>>()
    val evFilterCategory = evFilterCategories[EvFilter.CHARGING_SPEED] ?: return
    val evFilterOption = evFilterCategory.filterOptions[0]
    testMap[evFilterCategory] = setOf(evFilterOption)

    NavSdkExampleTheme {
        EvFilterCategoryRow(
            onEvFilterCategoryClick = { },
            activeFilters = testMap,
            categorySelected = null,
            modifier = Modifier,
        )
    }
}

@PreviewLightDark
@Composable
private fun EvFilterCategoryRowCategorySelectedPreview() {
    val testMap = SnapshotStateMap<EvFilterCategory, Set<EvFilterOption>>()
    val evFilterCategory = evFilterCategories[EvFilter.CHARGING_SPEED] ?: return
    val evFilterOption = evFilterCategory.filterOptions[0]
    testMap[evFilterCategory] = setOf(evFilterOption)

    NavSdkExampleTheme {
        EvFilterCategoryRow(
            onEvFilterCategoryClick = { },
            activeFilters = testMap,
            categorySelected = evFilterCategory,
            modifier = Modifier,
        )
    }
}

@PreviewLightDark
@Composable
private fun EvFilterCategoryPreview() {
    NavSdkExampleTheme {
        EvFilterCategory(
            isCategorySelected = false,
            isCategoryActive = false,
            imageVector = ImageVector.vectorResource(id = R.drawable.bolt_24px),
            onEvFilterCategoryClick = { },
        )
    }
}

@PreviewLightDark
@Composable
private fun EvFilterCategoryActivePreview() {
    NavSdkExampleTheme {
        EvFilterCategory(
            isCategorySelected = false,
            isCategoryActive = true,
            imageVector = ImageVector.vectorResource(id = R.drawable.bolt_24px),
            onEvFilterCategoryClick = { },
        )
    }
}

@PreviewLightDark
@Composable
private fun EvFilterCategorySelectedPreview() {
    NavSdkExampleTheme {
        EvFilterCategory(
            isCategorySelected = true,
            isCategoryActive = true,
            imageVector = ImageVector.vectorResource(id = R.drawable.bolt_24px),
            onEvFilterCategoryClick = { },
        )
    }
}

@PreviewLightDark
@Composable
private fun EvFilterOptionPanelPreview() {
    val evFilterCategory = evFilterCategories[EvFilter.CHARGING_SPEED] ?: return
    NavSdkExampleTheme {
        EvFilterOptionPanel(
            selectedFilterCategory = evFilterCategory,
            activeFilters = SnapshotStateMap(),
            onEvFilterOptionClick = { _, _ -> },
        )
    }
}

@PreviewLightDark
@Composable
private fun EvFilterOptionPreview() {
    NavSdkExampleTheme {
        EvFilterOption(
            imageVector = ImageVector.vectorResource(id = R.drawable.bolt_24px),
            optionDescription = "< 11 kW",
            isOptionSelected = false,
            onEvFilterOptionClick = { },
        )
    }
}

@PreviewLightDark
@Composable
private fun EvFilterOptionSelectedPreview() {
    NavSdkExampleTheme {
        EvFilterOption(
            imageVector = ImageVector.vectorResource(id = R.drawable.bolt_24px),
            optionDescription = "< 11 kW",
            isOptionSelected = true,
            onEvFilterOptionClick = { },
        )
    }
}
