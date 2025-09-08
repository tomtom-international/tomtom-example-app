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

package com.example.application.map.scenarios.routepreview

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.application.common.extension.formattedArrivalTime
import com.example.application.common.extension.formattedDistance
import com.example.application.common.extension.formattedDuration
import com.example.application.common.ui.CloseButton
import com.example.application.common.ui.NavigationBottomPanel
import com.example.application.common.ui.RecenterMapButton
import com.example.application.ui.theme.NavSdkExampleTheme

/**
 * Displays the Route Preview UI with close action, route summary panel, and recenter control,
 * arranging components differently for landscape and portrait orientations.
 */
@Composable
fun BoxScope.RoutePreviewUiComponents(routePreviewStateHolder: RoutePreviewStateHolder) {
    BackHandler { routePreviewStateHolder.onBackClick() }

    LaunchedEffect(Unit) {
        routePreviewStateHolder.onSafeAreaTopPaddingUpdate(0)
    }

    CloseButton(
        onClick = routePreviewStateHolder.onClearClick,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                end = 16.dp,
            ),
    )

    if (routePreviewStateHolder.isDeviceInLandscape) {
        Row(modifier = Modifier.align(Alignment.BottomStart)) {
            RoutePreviewPanel(routePreviewStateHolder = routePreviewStateHolder)

            RecenterMapButton(
                stateHolder = routePreviewStateHolder.recenterMapStateHolder,
                modifier = Modifier
                    .align(Alignment.Bottom)
                    .padding(16.dp),
            )
        }
    } else {
        Column(modifier = Modifier.align(Alignment.BottomStart)) {
            RecenterMapButton(
                stateHolder = routePreviewStateHolder.recenterMapStateHolder,
                modifier = Modifier.padding(16.dp),
            )

            RoutePreviewPanel(routePreviewStateHolder = routePreviewStateHolder)
        }
    }
}

@Composable
private fun RoutePreviewPanel(routePreviewStateHolder: RoutePreviewStateHolder) {
    val routes by routePreviewStateHolder.routesFlow.collectAsStateWithLifecycle(emptyList())

    routes.firstOrNull()?.let {
        RoutePreviewPanel(
            eta = it.formattedArrivalTime(),
            remainingDistance = it.formattedDistance(),
            remainingDuration = it.formattedDuration(),
            onDriveButtonClick = routePreviewStateHolder.onDriveButtonClick,
            isDeviceInLandscape = routePreviewStateHolder.isDeviceInLandscape,
            onSafeAreaBottomPaddingUpdate = routePreviewStateHolder.onSafeAreaBottomPaddingUpdate,
        )
    }
}

@Composable
private fun RoutePreviewPanel(
    eta: String,
    onDriveButtonClick: () -> Unit,
    isDeviceInLandscape: Boolean,
    onSafeAreaBottomPaddingUpdate: (Int) -> Unit,
    modifier: Modifier = Modifier,
    remainingDistance: String? = null,
    remainingDuration: String? = null,
) {
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
                    text = eta,
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
            Column(
                modifier = Modifier.fillMaxHeight(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center,
            ) {
                Button(onClick = onDriveButtonClick) {
                    Text(
                        text = stringResource(R.string.route_preview_button_drive),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        },
    )
}

@PreviewLightDark
@Composable
private fun RoutePreviewPanelPreview() {
    NavSdkExampleTheme {
        RoutePreviewPanel(
            eta = "16:58",
            remainingDistance = "650 yd",
            remainingDuration = "3 min",
            onDriveButtonClick = { },
            isDeviceInLandscape = false,
            onSafeAreaBottomPaddingUpdate = { _ -> },
            modifier = Modifier,
        )
    }
}
