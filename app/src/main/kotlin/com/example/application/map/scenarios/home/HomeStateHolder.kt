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

import androidx.compose.runtime.Stable
import com.example.application.common.PlaceDetails
import com.example.application.common.ui.RecenterMapStateHolder
import com.example.application.search.SearchStateHolder
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.map.display.camera.CameraOptions
import kotlinx.coroutines.flow.StateFlow

/**
 * UI state and callbacks for the Home scenario.
 *
 * @param locationUpdates: stream of user location updates used to animate camera.
 * @param poiPlaces: list of nearby or searched POI details shown on the map.
 * @param recenterMapStateHolder: holder controlling the recenter button visibility and action.
 * @param onAnimateCamera: callback to animate the map camera with provided options.
 * @param searchStateHolder: holder for search panel state and actions.
 * @param isDeviceInLandscape: whether the device is currently in landscape orientation.
 * @param onSafeAreaTopPaddingUpdate: reports top inset used by overlays.
 * @param onSafeAreaBottomPaddingUpdate: reports bottom inset used by overlays.
 * @param onSettingsClick: callback invoked when the Settings button is clicked.
 */
@Stable
data class HomeStateHolder(
    val locationUpdates: StateFlow<GeoPoint?>,
    val poiPlaces: List<PlaceDetails>,
    val recenterMapStateHolder: RecenterMapStateHolder,
    val onAnimateCamera: (CameraOptions) -> Unit,
    val searchStateHolder: SearchStateHolder,
    val isDeviceInLandscape: Boolean,
    val onSafeAreaTopPaddingUpdate: (Int) -> Unit,
    val onSafeAreaBottomPaddingUpdate: (Int) -> Unit,
    val onSettingsClick: () -> Unit,
)
