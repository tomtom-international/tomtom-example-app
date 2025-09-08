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

package com.example.application.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.application.map.MapScreenUiState.Companion.DEFAULT_ROTATION
import com.example.application.map.MapScreenUiState.Companion.DEFAULT_TILT
import com.example.application.map.MapScreenUiState.Companion.HOME_CAMERA_ZOOM
import com.example.application.search.SearchBottomSheet
import com.example.application.ui.RecenterMapButton
import com.example.application.ui.SettingsButton
import com.tomtom.sdk.map.display.camera.CameraOptions
import kotlinx.coroutines.flow.distinctUntilChangedBy

@Composable
fun BoxScope.HomeUiComponents(homeStateHolder: HomeStateHolder) {
    val bottomSheetHeight = 170.dp

    BackHandler(homeStateHolder)

    LaunchedEffect(Unit) {
        homeStateHolder.onSafeAreaTopPaddingUpdate(0)
        homeStateHolder.onSafeAreaBottomPaddingUpdate(
            if (homeStateHolder.isDeviceInLandscape) 0 else bottomSheetHeight.value.toInt(),
        )

        homeStateHolder.locationUpdates
            .distinctUntilChangedBy { it }
            .collect {
                if (homeStateHolder.poiPlaces.isEmpty()) {
                    homeStateHolder.onAnimateCamera(
                        CameraOptions(
                            zoom = HOME_CAMERA_ZOOM,
                            tilt = DEFAULT_TILT,
                            rotation = DEFAULT_ROTATION,
                            position = it,
                        ),
                    )
                }
            }
    }

    SettingsButton(
        onClick = homeStateHolder.onSettingsClick,
        modifier = Modifier
            .align(Alignment.TopStart)
            .padding(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp,
                start = 16.dp,
            ),
    )

    if (homeStateHolder.isDeviceInLandscape) {
        Row(modifier = Modifier.align(Alignment.BottomStart)) {
            SearchBottomSheet(searchStateHolder = homeStateHolder.searchStateHolder)

            RecenterMapButton(
                stateHolder = homeStateHolder.recenterMapStateHolder,
                modifier = Modifier
                    .align(Alignment.Bottom)
                    .padding(16.dp),
            )
        }
    } else {
        RecenterMapButton(
            stateHolder = homeStateHolder.recenterMapStateHolder,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 186.dp),
        )

        SearchBottomSheet(searchStateHolder = homeStateHolder.searchStateHolder)
    }
}

@Composable
private fun BackHandler(homeStateHolder: HomeStateHolder) {
    val isBottomSheetExpanded by homeStateHolder.searchStateHolder
        .isBottomPanelExpandedFlow.collectAsStateWithLifecycle()
    val inputQuery by homeStateHolder.searchStateHolder.inputQueryFlow.collectAsStateWithLifecycle()

    BackHandler(inputQuery.isNotEmpty() || isBottomSheetExpanded) {
        if (inputQuery.isNotEmpty()) {
            homeStateHolder.searchStateHolder.onClearSearch()
        }

        homeStateHolder.searchStateHolder.onToggleBottomSheet(false)
    }
}
