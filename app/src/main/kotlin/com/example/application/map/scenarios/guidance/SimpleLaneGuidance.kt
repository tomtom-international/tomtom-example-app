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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.application.common.extension.fadingEdge
import com.tomtom.sdk.routing.route.section.lane.Direction
import com.tomtom.sdk.routing.route.section.lane.Direction.LEFT
import com.tomtom.sdk.routing.route.section.lane.Direction.LEFT_U_TURN
import com.tomtom.sdk.routing.route.section.lane.Direction.RIGHT
import com.tomtom.sdk.routing.route.section.lane.Direction.RIGHT_U_TURN
import com.tomtom.sdk.routing.route.section.lane.Direction.SHARP_LEFT
import com.tomtom.sdk.routing.route.section.lane.Direction.SHARP_RIGHT
import com.tomtom.sdk.routing.route.section.lane.Direction.SLIGHT_LEFT
import com.tomtom.sdk.routing.route.section.lane.Direction.SLIGHT_RIGHT
import com.tomtom.sdk.routing.route.section.lane.Direction.STRAIGHT
import com.tomtom.sdk.routing.route.section.lane.Lane

@Composable
fun SimpleLaneGuidancePanel(
    slgPanelWidth: Int,
    lanes: List<Lane>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(simpleLaneGuidancePanelBackground),
    ) {
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            color = lowlightedDividerColor,
            thickness = 2.dp,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(17.dp),
            modifier = Modifier
                .padding(vertical = 6.dp)
                .align(Alignment.CenterHorizontally),
        ) {
            val slgConfiguration = createSlgConfiguration(lanes, calculateMaxNumberOfLanes(slgPanelWidth))

            slgConfiguration.lanesToDraw.forEachIndexed { index, lane ->
                Box(
                    modifier = Modifier
                        .height(LANE_SIZE)
                        .width(LANE_SIZE),
                ) {
                    val fadeLeftLane = if (index == 0) slgConfiguration.fadeLeft else false
                    val fadeRightLane = if (index == slgConfiguration.lanesToDraw.lastIndex) {
                        slgConfiguration.fadeRight
                    } else {
                        false
                    }

                    for (laneDirection in lane.directions) {
                        lane.follow?.let { followDirection ->
                            if (followDirection != laneDirection) {
                                SimpleLaneGuidanceIcon(
                                    direction = laneDirection,
                                    iconTint = lowlightedArrowColor,
                                    fadeLeft = fadeLeftLane,
                                    fadeRight = fadeRightLane,
                                    modifier = Modifier.align(Alignment.Center),
                                )
                            }
                        } ?: SimpleLaneGuidanceIcon(
                            direction = laneDirection,
                            iconTint = lowlightedArrowColor,
                            fadeLeft = fadeLeftLane,
                            fadeRight = fadeRightLane,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }

                    lane.follow?.let {
                        SimpleLaneGuidanceIcon(
                            direction = it,
                            iconTint = Color.White,
                            fadeLeft = fadeLeftLane,
                            fadeRight = fadeRightLane,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                }
            }
        }
    }
}

private data class SimpleLaneGuidanceConfiguration(
    val fadeLeft: Boolean = false,
    val fadeRight: Boolean = false,
    val lanesToDraw: List<Lane> = emptyList(),
)

private fun createSlgConfiguration(
    lanes: List<Lane>,
    maxNumberOfLanes: Int,
): SimpleLaneGuidanceConfiguration {
    var fadeLeft = false
    var fadeRight = false
    val numberOfLanes = lanes.size

    fun allLanesFitInScreen(
        numberOfLanes: Int,
        maxNumberOfLanes: Int,
    ) = numberOfLanes <= maxNumberOfLanes

    val leftLanesBeforeFollow = lanes.indexOfFirst { it.follow != null }
    val finalFollowLane = lanes.indexOfLast { it.follow != null }
    val rightLanesAfterFollow = if (finalFollowLane != -1) {
        numberOfLanes - finalFollowLane - 1
    } else {
        -1
    }

    val startingDrawingPosition: Int
    if (leftLanesBeforeFollow > rightLanesAfterFollow) {
        fadeLeft = !allLanesFitInScreen(numberOfLanes, maxNumberOfLanes)
        if (rightLanesAfterFollow >= maxNumberOfLanes) {
            fadeRight = true
            startingDrawingPosition = leftLanesBeforeFollow
        } else {
            startingDrawingPosition = Math.max(numberOfLanes - maxNumberOfLanes, 0)
        }
    } else {
        fadeRight = !allLanesFitInScreen(numberOfLanes, maxNumberOfLanes)
        if (leftLanesBeforeFollow >= maxNumberOfLanes) {
            fadeLeft = true
            startingDrawingPosition = leftLanesBeforeFollow
        } else {
            startingDrawingPosition = 0
        }
    }

    val finalDrawingPosition = if (!allLanesFitInScreen(numberOfLanes, maxNumberOfLanes)) {
        startingDrawingPosition + maxNumberOfLanes - 1
    } else {
        numberOfLanes - 1
    }

    return SimpleLaneGuidanceConfiguration(
        fadeLeft = fadeLeft,
        fadeRight = fadeRight,
        lanesToDraw = lanes.slice(startingDrawingPosition..finalDrawingPosition),
    )
}

private fun Direction.drawable(): ManeuverDrawable = when (this) {
    // Consolidated LEFT/RIGHT pairs use a single left-based asset; RIGHT variants are mirrored
    LEFT -> ManeuverDrawable(R.drawable.tt_asset_graphic_turn_64, false)
    RIGHT -> ManeuverDrawable(R.drawable.tt_asset_graphic_turn_64, true)

    STRAIGHT -> ManeuverDrawable(R.drawable.tt_asset_graphic_continue_64, false)

    SLIGHT_LEFT -> ManeuverDrawable(R.drawable.tt_asset_graphic_bear_64, false)
    SLIGHT_RIGHT -> ManeuverDrawable(R.drawable.tt_asset_graphic_bear_64, true)

    SHARP_LEFT -> ManeuverDrawable(R.drawable.tt_asset_graphic_sharpturn_64, false)
    SHARP_RIGHT -> ManeuverDrawable(R.drawable.tt_asset_graphic_sharpturn_64, true)

    LEFT_U_TURN -> ManeuverDrawable(R.drawable.tt_asset_graphic_uturn_64, false)
    RIGHT_U_TURN -> ManeuverDrawable(R.drawable.tt_asset_graphic_uturn_64, true)
}

private val Direction.xOffset: Dp
    @Composable
    get() = when (this) {
        LEFT, RIGHT -> -(12.dp)
        LEFT_U_TURN, RIGHT_U_TURN -> -(10.dp)
        SHARP_LEFT, SHARP_RIGHT -> -(8.dp)
        SLIGHT_LEFT, SLIGHT_RIGHT -> -(7.dp)
        else -> 0.dp
    }

@Suppress("detekt:MagicNumber")
private val simpleLaneGuidancePanelBackground = Color(0xFF2F598E)
private val lowlightedArrowColor = Color.White.copy(alpha = 0.4f)
private val lowlightedDividerColor = Color.White.copy(alpha = 0.4f)

private val LANE_SIZE = 34.dp

@Suppress("detekt:MagicNumber")
@Composable
private fun SimpleLaneGuidanceIcon(
    direction: Direction,
    iconTint: Color,
    modifier: Modifier = Modifier,
    fadeLeft: Boolean = false,
    fadeRight: Boolean = false,
) {
    val drawable = direction.drawable()
    Icon(
        painter = painterResource(id = drawable.resId),
        contentDescription = stringResource(R.string.guidance_content_description_next_instruction_panel_poi),
        modifier = modifier
            .then(if (drawable.shouldInvert) Modifier.graphicsLayer(scaleX = -1f) else Modifier)
            .then(
                if (fadeLeft) {
                    Modifier.fadingEdge(
                        Brush.horizontalGradient(
                            0f to Color.Transparent,
                            0.9f to Color.Red,
                        ),
                    )
                } else if (fadeRight) {
                    Modifier.fadingEdge(
                        Brush.horizontalGradient(
                            0.1f to Color.Red,
                            1f to Color.Transparent,
                        ),
                    )
                } else {
                    Modifier
                },
            )
            .size(40.dp)
            .offset(x = direction.xOffset),
        tint = iconTint,
    )
}

@Composable
private fun calculateMaxNumberOfLanes(slgPanelWidth: Int): Int {
    return slgPanelWidth / LANE_SIZE.value.toInt()
}

@Preview(showBackground = true)
@PreviewLightDark
@Composable
private fun LanesAlignmentPreview() {
    Box(modifier = Modifier) {
        SimpleLaneGuidancePanel(
            slgPanelWidth = LocalDensity.current.run { LocalWindowInfo.current.containerSize.width },
            lanes = listOf(
                Lane(
                    directions = listOf(STRAIGHT, RIGHT_U_TURN),
                    follow = null,
                ),
                Lane(
                    directions = listOf(STRAIGHT, LEFT_U_TURN),
                    follow = null,
                ),
                Lane(
                    directions = listOf(STRAIGHT, SHARP_RIGHT),
                    follow = null,
                ),
                Lane(
                    directions = listOf(STRAIGHT, SHARP_LEFT),
                    follow = null,
                ),
                Lane(
                    directions = listOf(STRAIGHT, RIGHT),
                    follow = null,
                ),
                Lane(
                    directions = listOf(STRAIGHT, LEFT),
                    follow = null,
                ),
                Lane(
                    directions = listOf(STRAIGHT, SLIGHT_RIGHT),
                    follow = null,
                ),
                Lane(
                    directions = listOf(STRAIGHT, SLIGHT_LEFT),
                    follow = null,
                ),
            ),
            modifier = Modifier,
        )
    }
}

@Suppress("detekt:MagicNumber")
@Preview(showBackground = true)
@PreviewLightDark
@Composable
private fun SimpleLaneGuidancePanelPreview() {
    val testListLength = 9
    Box(modifier = Modifier) {
        SimpleLaneGuidancePanel(
            slgPanelWidth = LocalDensity.current.run { LocalWindowInfo.current.containerSize.width },
            lanes = listOf(
                List(testListLength) { Lane(directions = listOf(SLIGHT_LEFT), follow = null) },
                listOf(
                    Lane(directions = listOf(STRAIGHT), follow = STRAIGHT),
                    Lane(directions = listOf(STRAIGHT), follow = STRAIGHT),
                    Lane(directions = listOf(STRAIGHT), follow = STRAIGHT),
                ),
                List(testListLength) { Lane(directions = listOf(RIGHT), follow = null) },
            ).flatten(),
            modifier = Modifier,
        )
    }
}

@Preview(showBackground = true)
@PreviewLightDark
@Composable
private fun LeftTurnGuidanceIconPreview() {
    Box(modifier = Modifier) {
        SimpleLaneGuidanceIcon(
            direction = LEFT,
            iconTint = Color.Black,
            fadeLeft = true,
        )
    }
}

@Preview(showBackground = true)
@PreviewLightDark
@Composable
private fun StraightfadeRightGuidanceIconPreview() {
    Box(modifier = Modifier) {
        SimpleLaneGuidanceIcon(
            direction = STRAIGHT,
            iconTint = Color.Black,
            fadeRight = true,
        )
    }
}
