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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.application.common.extension.formattedDuration
import com.example.application.common.extension.formattedRemainingDistance
import com.example.application.common.extension.formattedRemainingTime
import com.example.application.common.ui.NavigationBottomPanel
import com.example.application.ui.theme.NavSdkExampleTheme
import com.tomtom.quantity.Distance
import com.tomtom.sdk.navigation.progress.RouteProgress
import com.tomtom.sdk.navigation.progress.RouteStopProgress
import com.tomtom.sdk.routing.route.RouteStopId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

@Composable
fun GuidancePanel(
    routeProgressFlow: StateFlow<RouteProgress?>,
    routeFormatedArrivalTime: String,
    onStopGuidance: () -> Unit,
    onSafeAreaBottomPaddingUpdate: (Int) -> Unit,
    isDeviceInLandscape: Boolean,
    modifier: Modifier = Modifier,
) {
    val routeProgress by routeProgressFlow.collectAsStateWithLifecycle()
    val remainingDistance = routeProgress?.formattedRemainingDistance()
    val remainingDuration = routeProgress?.formattedDuration()

    NavigationBottomPanel(
        isDeviceInLandscape = isDeviceInLandscape,
        modifier = modifier
            .onGloballyPositioned { onSafeAreaBottomPaddingUpdate(if (isDeviceInLandscape) 0 else it.size.height) },
        header = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.chequered_flag_24px),
                    contentDescription = stringResource(id = R.string.common_content_description_arrival_time),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(40.dp)
                        .padding(4.dp),
                )
                Text(
                    text = routeProgress?.formattedRemainingTime() ?: routeFormatedArrivalTime,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
        subheader = {
            Row(
                modifier = Modifier.padding(start = 8.dp),
            ) {
                Text(
                    text = remainingDistance ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                )
                if (remainingDistance != null && remainingDuration != null) {
                    VerticalDivider(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .height(MaterialTheme.typography.titleMedium.fontSize.value.dp)
                            .padding(start = 4.dp, end = 4.dp),
                        thickness = 2.dp,
                    )
                }
                Text(
                    text = remainingDuration ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                )
            }
        },
        rightSideColumn = {
            Button(onClick = onStopGuidance) {
                Text(
                    text = stringResource(R.string.guidance_button_exit),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        },
    )
}

@Suppress("detekt:MagicNumber")
@PreviewLightDark
@Composable
private fun GuidancePanelPreview() {
    NavSdkExampleTheme {
        Column(modifier = Modifier) {
            GuidancePanel(
                routeProgressFlow = MutableStateFlow(
                    RouteProgress(
                        Distance.kilometers(1),
                        listOf(
                            RouteStopProgress(RouteStopId(), Duration.ZERO, Distance.kilometers(10)),
                        ),
                    ),
                ),
                routeFormatedArrivalTime = "",
                onStopGuidance = {},
                onSafeAreaBottomPaddingUpdate = { _ -> },
                isDeviceInLandscape = false,
                modifier = Modifier,
            )
        }
    }
}
