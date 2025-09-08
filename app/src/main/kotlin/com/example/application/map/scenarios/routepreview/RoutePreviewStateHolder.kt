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

package com.example.application.map.scenarios.routepreview

import androidx.compose.runtime.Stable
import com.example.application.common.ui.RecenterMapStateHolder
import com.tomtom.sdk.routing.route.Route
import kotlinx.coroutines.flow.StateFlow

/**
 * UI state and callbacks for the Route Preview scenario.
 *
 * @param recenterMapStateHolder: holder controlling the recenter button visibility and action.
 * @param routesFlow: stream of available routes to preview.
 * @param onClearClick: callback invoked to clear the preview and close the panel.
 * @param onDriveButtonClick: callback invoked to start guidance on the selected route.
 * @param onSimulateButtonClick: callback invoked to start simulated guidance.
 * @param isDeviceInLandscape: whether the device is currently in landscape orientation.
 * @param onSafeAreaTopPaddingUpdate: reports top inset used by overlays.
 * @param onSafeAreaBottomPaddingUpdate: reports bottom inset used by overlays.
 * @param onBackClick: callback invoked when the system back is pressed in this scenario.
 */
@Stable
data class RoutePreviewStateHolder(
    val recenterMapStateHolder: RecenterMapStateHolder,
    val routesFlow: StateFlow<List<Route>>,
    val onClearClick: () -> Unit,
    val onDriveButtonClick: () -> Unit,
    val onSimulateButtonClick: () -> Unit,
    val isDeviceInLandscape: Boolean,
    val onSafeAreaTopPaddingUpdate: (Int) -> Unit,
    val onSafeAreaBottomPaddingUpdate: (Int) -> Unit,
    val onBackClick: () -> Unit,
)
