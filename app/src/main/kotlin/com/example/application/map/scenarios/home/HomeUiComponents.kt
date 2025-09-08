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

package com.example.application.map.scenarios.home

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
import com.example.application.common.ui.RecenterMapButton
import com.example.application.common.ui.SettingsButton
import com.example.application.map.model.MapScreenUiState.Companion.DEFAULT_ROTATION
import com.example.application.map.model.MapScreenUiState.Companion.DEFAULT_TILT
import com.example.application.map.model.MapScreenUiState.Companion.HOME_CAMERA_ZOOM
import com.example.application.search.SearchBottomSheet
import com.tomtom.sdk.map.display.camera.CameraOptions
import kotlinx.coroutines.flow.distinctUntilChangedBy

/**
 * Displays the Home scenario UI with a search panel and recenter control, adapting layout for
 * orientation and updating safe‑area insets as needed.
 */
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
