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

package com.example.application.guidance

import com.example.application.common.PlaceDetails
import com.example.application.horizon.element.UpcomingHorizonElements
import com.example.application.ui.RecenterMapStateHolder
import com.tomtom.quantity.Distance
import com.tomtom.sdk.map.display.camera.CameraTrackingMode
import com.tomtom.sdk.navigation.guidance.LaneGuidance
import com.tomtom.sdk.navigation.locationcontext.LocationContext
import com.tomtom.sdk.navigation.progress.RouteProgress
import com.tomtom.sdk.routing.route.Route
import com.tomtom.sdk.routing.route.RouteStop
import kotlinx.coroutines.flow.StateFlow

@Suppress("detekt:LongParameterList")
class GuidanceStateHolder(
    val recenterMapStateHolder: RecenterMapStateHolder,
    val selectedRouteFlow: StateFlow<Route?>,
    val locationContext: StateFlow<LocationContext?>,
    val placeDetails: PlaceDetails?,
    val routeStop: RouteStop?,
    val routeProgress: StateFlow<RouteProgress?>,
    val onAddStopButtonClick: () -> Unit,
    val onRemoveStopButtonClick: () -> Unit,
    val onCloseWaypointPanelButtonClick: () -> Unit,
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
