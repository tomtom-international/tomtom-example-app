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

package com.example.application.map.scenarios.arrival

import androidx.compose.runtime.Stable
import com.example.application.common.PlaceDetails
import com.example.application.common.ui.RecenterMapStateHolder

/**
 * UI state and callbacks for the Destination Arrival scenario.
 *
 * @param recenterMapStateHolder: holder controlling the recenter button visibility and action.
 * @param destinationDetails: details of the destination place to display.
 * @param onArrivalButtonClick: callback invoked when the Finish/Arrived button is clicked.
 * @param isDeviceInLandscape: whether the device is currently in landscape orientation.
 * @param onSafeAreaTopPaddingUpdate: reports top inset used by overlays.
 * @param onSafeAreaBottomPaddingUpdate: reports bottom inset used by overlays.
 */
@Stable
data class ArrivalStateHolder(
    val recenterMapStateHolder: RecenterMapStateHolder,
    val destinationDetails: PlaceDetails?,
    val onArrivalButtonClick: () -> Unit,
    val isDeviceInLandscape: Boolean,
    val onSafeAreaTopPaddingUpdate: (Int) -> Unit,
    val onSafeAreaBottomPaddingUpdate: (Int) -> Unit,
)
