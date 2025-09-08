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

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.application.common.fillMaxWidthByOrientation
import com.example.application.common.formatDistance
import com.example.application.extension.formattedArrivalTime
import com.example.application.guidance.ManeuverType.ARRIVAL
import com.example.application.guidance.ManeuverType.BEAR_LEFT
import com.example.application.guidance.ManeuverType.BEAR_RIGHT
import com.example.application.guidance.ManeuverType.EXIT_HIGHWAY_LEFT
import com.example.application.guidance.ManeuverType.EXIT_HIGHWAY_RIGHT
import com.example.application.guidance.ManeuverType.MERGE_TO_LEFT
import com.example.application.guidance.ManeuverType.MERGE_TO_RIGHT
import com.example.application.guidance.ManeuverType.ROUNDABOUT_BACK
import com.example.application.guidance.ManeuverType.ROUNDABOUT_CROSS
import com.example.application.guidance.ManeuverType.ROUNDABOUT_LEFT
import com.example.application.guidance.ManeuverType.ROUNDABOUT_RIGHT
import com.example.application.guidance.ManeuverType.SHARP_LEFT
import com.example.application.guidance.ManeuverType.SHARP_RIGHT
import com.example.application.guidance.ManeuverType.STRAIGHT
import com.example.application.guidance.ManeuverType.TOLLGATE
import com.example.application.guidance.ManeuverType.TURN_LEFT
import com.example.application.guidance.ManeuverType.TURN_RIGHT
import com.example.application.guidance.ManeuverType.UTURN
import com.example.application.horizon.HorizonCard
import com.example.application.ui.MapModeToggleButton
import com.example.application.ui.RecenterMapButton
import com.example.application.ui.Speedometer
import com.tomtom.sdk.map.display.camera.CameraTrackingMode

@Composable
fun BoxScope.GuidanceUiComponents(stateHolder: GuidanceStateHolder) {
    BackHandler { stateHolder.onExitButtonClick() }

    TopPanel(stateHolder)

    if (stateHolder.isDeviceInLandscape) {
        Row(modifier = Modifier.align(Alignment.BottomStart)) {
            BottomPanel(stateHolder = stateHolder, modifier = Modifier.align(Alignment.Bottom))

            OverlayUi(stateHolder)
        }
    } else {
        Column(modifier = Modifier.align(Alignment.BottomStart)) {
            OverlayUi(stateHolder)

            BottomPanel(stateHolder = stateHolder)
        }
    }
}

@Composable
private fun OverlayUi(stateHolder: GuidanceStateHolder) {
    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.padding(16.dp)) {
        RecenterMapButton(stateHolder = stateHolder.recenterMapStateHolder)

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Speedometer(stateHolder.locationContext, stateHolder.isDeviceInLandscape)

            MapModeToggleButton(
                checked = stateHolder.cameraTrackingMode == CameraTrackingMode.FollowRouteDirection,
                onCheckedChange = stateHolder.onMapModeToggleClick,
            )
        }
    }
}

@Composable
private fun TopPanel(stateHolder: GuidanceStateHolder) {
    val nextInstruction by stateHolder.nextInstruction.collectAsStateWithLifecycle()
    val laneGuidance by stateHolder.laneGuidance.collectAsStateWithLifecycle()
    val horizonElements by stateHolder.horizonElementsFlow.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxWidthByOrientation(stateHolder.isDeviceInLandscape)
            .onGloballyPositioned {
                stateHolder.onSafeAreaTopPaddingUpdate(if (stateHolder.isDeviceInLandscape) 0 else it.size.height)
            }
            .then(
                when {
                    nextInstruction?.maneuverType != null ->
                        Modifier
                            .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                            .background(nextInstructionPanelBackground)

                    horizonElements?.trafficElement != null ||
                        horizonElements?.safetyLocationElement != null ->
                        Modifier
                            .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                            .background(MaterialTheme.colorScheme.surface)

                    else -> Modifier
                },
            )
            .padding(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
            ),
    ) {
        nextInstruction?.let { nextInstruction ->
            nextInstruction.maneuverType?.let { maneuverType ->
                NextInstructionPanel(
                    icon = maneuverType.icon,
                    distanceToNextInstruction = formatDistance(nextInstruction.distanceToManeuver),
                    roadname = nextInstruction.roadName,
                    towards = nextInstruction.towardName,
                    laneGuidance = laneGuidance,
                    isDeviceInLandscape = stateHolder.isDeviceInLandscape,
                )
            }
        }
        horizonElements?.let { horizonElements ->
            val element = horizonElements.safetyLocationElement
                ?: horizonElements.trafficElement
            element?.let {
                HorizonCard(
                    horizonElement = it,
                    isBottomCard = true,
                )
            }
        }
    }
}

@Composable
private fun BottomPanel(
    stateHolder: GuidanceStateHolder,
    modifier: Modifier = Modifier,
) {
    when {
        stateHolder.placeDetails != null -> {
            val (isAddingStop, onButtonClick) = if (stateHolder.routeStop == null) {
                (true to stateHolder.onAddStopButtonClick)
            } else {
                (false to stateHolder.onRemoveStopButtonClick)
            }
            WaypointPanel(
                isAddingStop = isAddingStop,
                onButtonClick = onButtonClick,
                onCloseWaypointPanelButtonClick = stateHolder.onCloseWaypointPanelButtonClick,
                placeDetails = stateHolder.placeDetails,
                onSafeAreaBottomPaddingUpdate = stateHolder.onSafeAreaBottomPaddingUpdate,
                modifier = modifier,
                isDeviceInLandscape = stateHolder.isDeviceInLandscape,
            )
        }

        else -> {
            val selectedRoute by stateHolder.selectedRouteFlow.collectAsStateWithLifecycle()

            selectedRoute?.let {
                GuidancePanel(
                    routeProgressFlow = stateHolder.routeProgress,
                    routeFormatedArrivalTime = it.formattedArrivalTime(),
                    onStopGuidance = stateHolder.onExitButtonClick,
                    onSafeAreaBottomPaddingUpdate = stateHolder.onSafeAreaBottomPaddingUpdate,
                    isDeviceInLandscape = stateHolder.isDeviceInLandscape,
                    modifier = modifier,
                )
            }
        }
    }
}

@get:DrawableRes
private val ManeuverType.icon: Int
    get() {
        return when (this) {
            STRAIGHT -> R.drawable.straight_24px
            TURN_LEFT -> R.drawable.turn_left_24px
            TURN_RIGHT -> R.drawable.turn_right_24px
            // TODO(GOSDK-183166) Change to use sharp turn icons from TTApp
            SHARP_LEFT -> R.drawable.turn_left_24px
            SHARP_RIGHT -> R.drawable.turn_right_24px
            BEAR_LEFT -> R.drawable.bear_left_24px
            BEAR_RIGHT -> R.drawable.bear_right_24px
            MERGE_TO_LEFT, MERGE_TO_RIGHT -> R.drawable.merge_24px
            // TODO(GOSDK-183166) Change to use roundabout icons from TTApp
            ROUNDABOUT_CROSS -> R.drawable.straight_24px
            ROUNDABOUT_RIGHT, ROUNDABOUT_LEFT, ROUNDABOUT_BACK -> R.drawable.roundabout_left_24px
            ARRIVAL -> R.drawable.chequered_flag_24px
            EXIT_HIGHWAY_LEFT -> R.drawable.bear_left_24px
            EXIT_HIGHWAY_RIGHT -> R.drawable.bear_right_24px
            TOLLGATE -> R.drawable.toll_24px
            UTURN -> R.drawable.u_turn_left_24px
        }
    }
