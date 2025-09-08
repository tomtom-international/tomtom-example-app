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
import com.example.application.common.NavigationBottomPanel
import com.example.application.extension.formattedDuration
import com.example.application.extension.formattedRemainingDistance
import com.example.application.extension.formattedRemainingTime
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
                        Distance.Companion.kilometers(1),
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
