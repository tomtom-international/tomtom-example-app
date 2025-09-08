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

import androidx.annotation.DrawableRes
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.application.extension.fadingEdge
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
            horizontalArrangement = Arrangement.Center,
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

private val Direction.icon: Int
    @DrawableRes
    get() = when (this) {
        LEFT -> R.drawable.turn_left_24px
        RIGHT -> R.drawable.turn_right_24px
        STRAIGHT -> R.drawable.straight_24px
        SLIGHT_RIGHT -> R.drawable.bear_right_24px
        SHARP_RIGHT -> R.drawable.turn_sharp_right_24px
        RIGHT_U_TURN -> R.drawable.u_turn_right_24px
        SLIGHT_LEFT -> R.drawable.bear_left_24px
        SHARP_LEFT -> R.drawable.turn_sharp_left_24px
        LEFT_U_TURN -> R.drawable.u_turn_left_24px
    }

private val Direction.xOffset: Dp
    @Composable
    get() = when (this) {
        LEFT -> -(7.dp)
        RIGHT -> 7.dp
        LEFT_U_TURN -> -(8.dp)
        RIGHT_U_TURN -> 8.dp
        SHARP_LEFT -> -(8.dp)
        SHARP_RIGHT -> 8.dp
        SLIGHT_LEFT -> -(3.dp)
        SLIGHT_RIGHT -> 3.dp
        else -> 0.dp
    }

private val Direction.yOffset: Dp
    @Composable
    get() = when (this) {
        STRAIGHT, SHARP_LEFT, SHARP_RIGHT, RIGHT_U_TURN, LEFT_U_TURN -> -(2.dp)
        else -> 0.dp
    }

@Suppress("detekt:MagicNumber")
private val simpleLaneGuidancePanelBackground = Color(0xFF2F598E)
private val lowlightedArrowColor = Color.White.copy(alpha = 0.4f)
private val lowlightedDividerColor = Color.White.copy(alpha = 0.4f)

private val LANE_SIZE = 48.dp

@Suppress("detekt:MagicNumber")
@Composable
private fun SimpleLaneGuidanceIcon(
    direction: Direction,
    iconTint: Color,
    modifier: Modifier = Modifier,
    fadeLeft: Boolean = false,
    fadeRight: Boolean = false,
) {
    Icon(
        painter = painterResource(id = direction.icon),
        contentDescription = stringResource(R.string.guidance_content_description_next_instruction_panel_poi),
        modifier = modifier
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
            .offset(x = direction.xOffset, y = direction.yOffset),
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
