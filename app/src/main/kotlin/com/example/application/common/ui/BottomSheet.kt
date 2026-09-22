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

package com.example.application.common.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp

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
fun FixedHeightDemoBottomSheet(
    isDeviceInLandscape: Boolean,
    modifier: Modifier = Modifier,
    sheetPeekHeight: Dp = BottomSheetDefaults.SheetPeekHeight,
    contentPadding: PaddingValues = PaddingValues(top = 32.dp, start = 16.dp, end = 16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    FixedHeightBottomSheet(
        sheetPeekHeight = sheetPeekHeight,
        modifier = modifier,
        isDeviceInLandscape = isDeviceInLandscape,
    ) {
        Column(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
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
            snackbarHost = {},
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
