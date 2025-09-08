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

package com.example.application.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixedHeightBottomSheet(
    modifier: Modifier = Modifier,
    sheetPeekHeight: Dp = BottomSheetDefaults.SheetPeekHeight,
    isDeviceInLandscape: Boolean,
    sheetContent: @Composable ColumnScope.() -> Unit,
) {
    BottomSheet(
        sheetPeekHeight = sheetPeekHeight,
        modifier = modifier,
        isDeviceInLandscape = isDeviceInLandscape,
        sheetContent = sheetContent,
        sheetSwipeEnabled = false,
        showDragHandle = false,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(
    modifier: Modifier = Modifier,
    sheetPeekHeight: Dp = BottomSheetDefaults.SheetPeekHeight,
    isExpanded: Boolean = false,
    showDragHandle: Boolean = true,
    onBottomSheetExpand: () -> Unit = {},
    onBottomSheetPartialExpand: () -> Unit = {},
    sheetSwipeEnabled: Boolean = true,
    scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
    isDeviceInLandscape: Boolean,
    sheetContent: @Composable ColumnScope.() -> Unit,
) {
    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            scaffoldState.bottomSheetState.expand()
        } else {
            scaffoldState.bottomSheetState.partialExpand()
        }
    }

    LaunchedEffect(scaffoldState.bottomSheetState.currentValue) {
        if (scaffoldState.bottomSheetState.currentValue == SheetValue.PartiallyExpanded) {
            onBottomSheetPartialExpand()
        } else {
            onBottomSheetExpand()
        }
    }

    Column(
        modifier = modifier.fillMaxWidthByOrientation(isDeviceInLandscape),
    ) {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = sheetPeekHeight,
            sheetSwipeEnabled = sheetSwipeEnabled,
            sheetDragHandle = {
                if (showDragHandle) {
                    BottomSheetDefaults.DragHandle(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
            },
            sheetContent = sheetContent,
        ) { }
    }
}
