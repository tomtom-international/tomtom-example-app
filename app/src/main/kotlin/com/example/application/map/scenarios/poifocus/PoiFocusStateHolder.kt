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

package com.example.application.map.scenarios.poifocus

import androidx.compose.runtime.Stable
import com.example.application.common.PlaceDetails
import com.example.application.common.ui.RecenterMapStateHolder
import com.tomtom.sdk.map.display.camera.CameraOptions

/**
 * UI state and callbacks for the POI Focus scenario.
 *
 * @param recenterMapStateHolder: holder controlling the recenter button visibility and action.
 * @param placeDetails: details of the place currently focused.
 * @param onAnimateCamera: callback to animate camera to the POI.
 * @param onClearClick: callback invoked to clear focus and close the panel.
 * @param onRouteButtonClick: callback invoked to start route planning to the POI.
 * @param isDeviceInLandscape: whether the device is currently in landscape orientation.
 * @param onSafeAreaTopPaddingUpdate: reports top inset used by overlays.
 * @param onSafeAreaBottomPaddingUpdate: reports bottom inset used by overlays.
 */
@Stable
data class PoiFocusStateHolder(
    val recenterMapStateHolder: RecenterMapStateHolder,
    val placeDetails: PlaceDetails?,
    val onAnimateCamera: (CameraOptions) -> Unit,
    val onClearClick: () -> Unit,
    val onRouteButtonClick: () -> Unit,
    val isDeviceInLandscape: Boolean,
    val onSafeAreaTopPaddingUpdate: (Int) -> Unit,
    val onSafeAreaBottomPaddingUpdate: (Int) -> Unit,
)
