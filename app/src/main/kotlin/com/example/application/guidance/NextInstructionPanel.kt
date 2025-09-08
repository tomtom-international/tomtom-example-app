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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.application.ui.theme.NavSdkExampleTheme
import com.tomtom.quantity.Distance
import com.tomtom.sdk.navigation.guidance.LaneGuidance
import com.tomtom.sdk.routing.route.section.lane.Direction.LEFT
import com.tomtom.sdk.routing.route.section.lane.Direction.LEFT_U_TURN
import com.tomtom.sdk.routing.route.section.lane.Direction.RIGHT
import com.tomtom.sdk.routing.route.section.lane.Direction.RIGHT_U_TURN
import com.tomtom.sdk.routing.route.section.lane.Direction.SHARP_LEFT
import com.tomtom.sdk.routing.route.section.lane.Direction.SHARP_RIGHT
import com.tomtom.sdk.routing.route.section.lane.Direction.STRAIGHT
import com.tomtom.sdk.routing.route.section.lane.Lane

@Suppress("detekt:MagicNumber")
val nextInstructionPanelBackground = Color(0xFF2F598E)

@Composable
fun NextInstructionPanel(
    icon: Int,
    distanceToNextInstruction: String?,
    roadname: String?,
    towards: String?,
    isDeviceInLandscape: Boolean,
    modifier: Modifier = Modifier,
    laneGuidance: LaneGuidance? = null,
) {
    Column {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(nextInstructionPanelBackground)
                .padding(
                    start = 32.dp,
                    end = 32.dp,
                    bottom = 16.dp,
                ),
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = stringResource(R.string.guidance_content_description_next_instruction_panel_poi),
                modifier = Modifier
                    .size(64.dp)
                    .padding(end = 8.dp),
                tint = Color.White,
            )
            Column {
                Text(
                    text = distanceToNextInstruction.orEmpty(),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                if (!roadname.isNullOrEmpty()) {
                    Text(
                        text = roadname,
                        fontSize = 16.sp,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
                if (!towards.isNullOrEmpty()) {
                    Text(
                        text = towards,
                        fontSize = 16.sp,
                        color = Color.White,
                    )
                }
            }
        }
        laneGuidance?.let {
            SimpleLaneGuidancePanel(
                slgPanelWidth = if (isDeviceInLandscape) {
                    LocalConfiguration.current.screenWidthDp / 2
                } else {
                    LocalConfiguration.current.screenWidthDp
                },
                lanes = it.lanes,
                modifier = modifier,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun NextInstructionPanelPreview() {
    NavSdkExampleTheme {
        Box(modifier = Modifier) {
            NextInstructionPanel(
                icon = R.drawable.turn_left_24px,
                distanceToNextInstruction = "430m",
                roadname = "Via della Cappella",
                towards = "Towards",
                laneGuidance = null,
                isDeviceInLandscape = false,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun NextInstructionPanelSlgPreview() {
    Box(modifier = Modifier) {
        NextInstructionPanel(
            icon = R.drawable.turn_left_24px,
            distanceToNextInstruction = "430m",
            roadname = "Via della Cappella",
            towards = "Towards",
            laneGuidance = LaneGuidance(
                lanes = listOf(
                    Lane(
                        directions = listOf(STRAIGHT, LEFT, SHARP_RIGHT),
                        follow = STRAIGHT,
                    ),
                    Lane(
                        directions = listOf(LEFT_U_TURN, STRAIGHT, RIGHT_U_TURN),
                        follow = STRAIGHT,
                    ),
                    Lane(
                        directions = listOf(STRAIGHT, RIGHT, LEFT),
                        follow = null,
                    ),
                    Lane(
                        directions = listOf(SHARP_LEFT, STRAIGHT),
                        follow = STRAIGHT,
                    ),
                ),
                laneSeparators = emptyList(),
                routeOffset = Distance.ZERO,
                length = Distance.ZERO,
            ),
            modifier = Modifier,
            isDeviceInLandscape = false,
        )
    }
}
