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
import com.example.application.common.extension.formattedArrivalTime
import com.example.application.common.formatDistance
import com.example.application.common.ui.MapModeToggleButton
import com.example.application.common.ui.RecenterMapButton
import com.example.application.common.ui.Speedometer
import com.example.application.common.ui.fillMaxWidthByOrientation
import com.example.application.horizon.HorizonCard
import com.example.application.map.scenarios.guidance.ManeuverType.ARRIVAL
import com.example.application.map.scenarios.guidance.ManeuverType.BEAR_LEFT
import com.example.application.map.scenarios.guidance.ManeuverType.BEAR_RIGHT
import com.example.application.map.scenarios.guidance.ManeuverType.EXIT_HIGHWAY_LEFT
import com.example.application.map.scenarios.guidance.ManeuverType.EXIT_HIGHWAY_RIGHT
import com.example.application.map.scenarios.guidance.ManeuverType.MERGE_TO_LEFT
import com.example.application.map.scenarios.guidance.ManeuverType.MERGE_TO_RIGHT
import com.example.application.map.scenarios.guidance.ManeuverType.ROUNDABOUT_BACK
import com.example.application.map.scenarios.guidance.ManeuverType.ROUNDABOUT_CROSS
import com.example.application.map.scenarios.guidance.ManeuverType.ROUNDABOUT_LEFT
import com.example.application.map.scenarios.guidance.ManeuverType.ROUNDABOUT_RIGHT
import com.example.application.map.scenarios.guidance.ManeuverType.SHARP_LEFT
import com.example.application.map.scenarios.guidance.ManeuverType.SHARP_RIGHT
import com.example.application.map.scenarios.guidance.ManeuverType.STRAIGHT
import com.example.application.map.scenarios.guidance.ManeuverType.TOLLGATE
import com.example.application.map.scenarios.guidance.ManeuverType.TURN_LEFT
import com.example.application.map.scenarios.guidance.ManeuverType.TURN_RIGHT
import com.example.application.map.scenarios.guidance.ManeuverType.UTURN
import com.tomtom.sdk.map.display.camera.CameraTrackingMode

/**
 * Composes the Guidance UI including top next-instruction panel, bottom ETA/controls panel,
 * and overlay with recenter and map-mode toggle, arranged for portrait or landscape.
 */
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
                    drawable = maneuverType.drawable(),
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

data class ManeuverDrawable(
    @DrawableRes val resId: Int,
    val shouldInvert: Boolean,
)

@Suppress("CyclomaticComplexMethod")
private fun ManeuverType.drawable(): ManeuverDrawable = when (this) {
    STRAIGHT -> ManeuverDrawable(R.drawable.tt_asset_graphic_continue_64, false)
    TURN_LEFT -> ManeuverDrawable(R.drawable.tt_asset_graphic_turn_64, false)
    TURN_RIGHT -> ManeuverDrawable(R.drawable.tt_asset_graphic_turn_64, true)
    SHARP_LEFT -> ManeuverDrawable(R.drawable.tt_asset_graphic_sharpturn_64, false)
    SHARP_RIGHT -> ManeuverDrawable(R.drawable.tt_asset_graphic_sharpturn_64, true)
    BEAR_LEFT -> ManeuverDrawable(R.drawable.tt_asset_graphic_bear_64, false)
    BEAR_RIGHT -> ManeuverDrawable(R.drawable.tt_asset_graphic_bear_64, true)
    MERGE_TO_LEFT -> ManeuverDrawable(R.drawable.tt_asset_graphic_merge_64, false)
    MERGE_TO_RIGHT -> ManeuverDrawable(R.drawable.tt_asset_graphic_merge_64, true)
    ROUNDABOUT_CROSS -> ManeuverDrawable(R.drawable.tt_asset_graphic_roundaboutstraight_64, true)
    ROUNDABOUT_RIGHT -> ManeuverDrawable(R.drawable.tt_asset_graphic_roundaboutexitright_64, true)
    ROUNDABOUT_LEFT -> ManeuverDrawable(R.drawable.tt_asset_graphic_roundaboutexitleft_64, false)
    ROUNDABOUT_BACK -> ManeuverDrawable(R.drawable.tt_asset_graphic_roundaboutback_64, false)
    ARRIVAL -> ManeuverDrawable(R.drawable.tt_asset_graphic_finish_64, false)
    EXIT_HIGHWAY_LEFT -> ManeuverDrawable(R.drawable.tt_asset_icon_exit_fill_24, true)
    EXIT_HIGHWAY_RIGHT -> ManeuverDrawable(R.drawable.tt_asset_icon_exit_fill_24, false)
    TOLLGATE -> ManeuverDrawable(R.drawable.tt_asset_graphic_tollgate_64, false)
    UTURN -> ManeuverDrawable(R.drawable.tt_asset_graphic_uturn_64, false)
}
