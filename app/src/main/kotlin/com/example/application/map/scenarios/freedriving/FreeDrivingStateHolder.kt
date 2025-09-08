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

package com.example.application.map.scenarios.freedriving

import com.example.application.common.ui.RecenterMapStateHolder
import com.example.application.horizon.element.UpcomingHorizonElements
import com.example.application.search.SearchStateHolder
import com.tomtom.sdk.navigation.locationcontext.LocationContext
import kotlinx.coroutines.flow.StateFlow

/**
 * UI state and callbacks for the Free Driving scenario.
 *
 * @param recenterMapStateHolder: holder controlling the recenter button visibility and action.
 * @param searchStateHolder: holder for search panel state and actions.
 * @param locationContext: stream of location context updates for speed and other info.
 * @param isDeviceInLandscape: whether the device is currently in landscape orientation.
 * @param onSafeAreaTopPaddingUpdate: reports top inset used by overlays.
 * @param onSafeAreaBottomPaddingUpdate: reports bottom inset used by overlays.
 * @param horizonElementsFlow: stream of upcoming horizon alerts to render.
 */
@Suppress("detekt:LongParameterList")
class FreeDrivingStateHolder(
    val recenterMapStateHolder: RecenterMapStateHolder,
    val searchStateHolder: SearchStateHolder,
    val locationContext: StateFlow<LocationContext?>,
    val isDeviceInLandscape: Boolean,
    val onSafeAreaTopPaddingUpdate: (Int) -> Unit,
    val onSafeAreaBottomPaddingUpdate: (Int) -> Unit,
    val horizonElementsFlow: StateFlow<UpcomingHorizonElements?>,
)
