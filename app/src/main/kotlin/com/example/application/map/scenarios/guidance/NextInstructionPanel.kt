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
import androidx.compose.ui.graphics.graphicsLayer
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
    drawable: ManeuverDrawable,
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
                    start = 24.dp,
                    end = 32.dp,
                    bottom = 16.dp,
                ),
        ) {
            Icon(
                painter = painterResource(id = drawable.resId),
                contentDescription = stringResource(R.string.guidance_content_description_next_instruction_panel_poi),
                modifier = Modifier
                    .then(if (drawable.shouldInvert) Modifier.graphicsLayer(scaleX = -1f) else Modifier)
                    .size(64.dp)
                    .padding(end = 8.dp),
                tint = Color.White,
            )
            Column(modifier = Modifier.padding(start = 8.dp)) {
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
                drawable = ManeuverDrawable(R.drawable.tt_asset_graphic_turn_64, shouldInvert = false),
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
            drawable = ManeuverDrawable(R.drawable.tt_asset_graphic_turn_64, shouldInvert = true),
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

@PreviewLightDark
@Composable
private fun NextInstructionPanelRoundaboutCrossPreview() {
    NavSdkExampleTheme {
        Box(modifier = Modifier) {
            NextInstructionPanel(
                drawable = ManeuverDrawable(
                    R.drawable.tt_asset_graphic_roundaboutstraight_64,
                    shouldInvert = true,
                ),
                distanceToNextInstruction = "250m",
                roadname = "Piazza del Popolo",
                towards = "4th exit",
                laneGuidance = null,
                isDeviceInLandscape = false,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun NextInstructionPanelRoundaboutLeftPreview() {
    NavSdkExampleTheme {
        Box(modifier = Modifier) {
            NextInstructionPanel(
                drawable = ManeuverDrawable(
                    R.drawable.tt_asset_graphic_roundaboutexitright_64,
                    shouldInvert = true,
                ),
                distanceToNextInstruction = "120m",
                roadname = "Via Roma",
                towards = "1st exit",
                laneGuidance = null,
                isDeviceInLandscape = false,
            )
        }
    }
}
