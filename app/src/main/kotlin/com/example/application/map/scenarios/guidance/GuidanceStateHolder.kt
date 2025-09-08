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

package com.example.application.map.scenarios.guidance

import com.example.application.common.ui.RecenterMapStateHolder
import com.example.application.horizon.element.UpcomingHorizonElements
import com.tomtom.quantity.Distance
import com.tomtom.sdk.map.display.camera.CameraTrackingMode
import com.tomtom.sdk.navigation.guidance.LaneGuidance
import com.tomtom.sdk.navigation.locationcontext.LocationContext
import com.tomtom.sdk.navigation.progress.RouteProgress
import com.tomtom.sdk.routing.route.Route
import kotlinx.coroutines.flow.StateFlow

/**
 * UI state and callbacks for the Guidance scenario.
 *
 * @param recenterMapStateHolder: holder controlling the recenter button visibility and action.
 * @param selectedRouteFlow: stream of the currently selected route.
 * @param locationContext: stream of location context updates for speed and environment.
 * @param routeProgress: stream of route progress updates.
 * @param onExitButtonClick: callback to stop guidance and exit the scenario.
 * @param nextInstruction: stream of next-instruction data for the top panel.
 * @param horizonElementsFlow: stream of upcoming horizon alerts to render.
 * @param laneGuidance: stream of lane guidance updates.
 * @param onMapModeToggleClick: callback toggling camera tracking mode.
 * @param cameraTrackingMode: current camera tracking mode.
 * @param isDeviceInLandscape: whether the device is currently in landscape orientation.
 * @param onSafeAreaTopPaddingUpdate: reports top inset used by overlays.
 * @param onSafeAreaBottomPaddingUpdate: reports bottom inset used by overlays.
 */
@Suppress("detekt:LongParameterList")
class GuidanceStateHolder(
    val recenterMapStateHolder: RecenterMapStateHolder,
    val selectedRouteFlow: StateFlow<Route?>,
    val locationContext: StateFlow<LocationContext?>,
    val routeProgress: StateFlow<RouteProgress?>,
    val onExitButtonClick: () -> Unit,
    val nextInstruction: StateFlow<NextInstruction?>,
    val horizonElementsFlow: StateFlow<UpcomingHorizonElements?>,
    val laneGuidance: StateFlow<LaneGuidance?>,
    val onMapModeToggleClick: (Boolean) -> Unit,
    val cameraTrackingMode: CameraTrackingMode,
    val isDeviceInLandscape: Boolean,
    val onSafeAreaTopPaddingUpdate: (Int) -> Unit,
    val onSafeAreaBottomPaddingUpdate: (Int) -> Unit,
)

data class NextInstruction(
    val maneuverType: ManeuverType?,
    val distanceToManeuver: Distance,
    val roadName: String?,
    val towardName: String?,
    val exitNumber: String?,
    val exitName: String?,
)

enum class ManeuverType {
    STRAIGHT,
    TURN_LEFT,
    TURN_RIGHT,
    SHARP_LEFT,
    SHARP_RIGHT,
    BEAR_LEFT,
    BEAR_RIGHT,
    MERGE_TO_LEFT,
    MERGE_TO_RIGHT,
    ROUNDABOUT_CROSS,
    ROUNDABOUT_RIGHT,
    ROUNDABOUT_LEFT,
    ROUNDABOUT_BACK,
    ARRIVAL,
    EXIT_HIGHWAY_LEFT,
    EXIT_HIGHWAY_RIGHT,
    TOLLGATE,
    UTURN,
}
