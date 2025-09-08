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

package com.example.application.routepreview

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
import com.example.application.common.NavigationBottomPanel
import com.example.application.extension.formattedArrivalTime
import com.example.application.extension.formattedDistance
import com.example.application.extension.formattedDuration
import com.example.application.ui.CloseButton
import com.example.application.ui.RecenterMapButton
import com.example.application.ui.theme.NavSdkExampleTheme

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
