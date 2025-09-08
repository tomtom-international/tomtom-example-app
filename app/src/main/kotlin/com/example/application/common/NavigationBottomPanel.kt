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

package com.example.application.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.example.application.ui.theme.NavSdkExampleTheme

@Composable
fun NavigationBottomPanel(
    isDeviceInLandscape: Boolean,
    header: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    subheader: @Composable ColumnScope.() -> Unit = { },
    body: @Composable (ColumnScope.() -> Unit) = { },
    rightSideColumn: @Composable (RowScope.() -> Unit) = { },
) {
    Row(
        modifier = modifier
            .fillMaxWidthByOrientation(isDeviceInLandscape)
            .height(IntrinsicSize.Min)
            .clip(
                shape = RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 20.dp,
                ),
            )
            .background(color = MaterialTheme.colorScheme.background)
            .padding(vertical = 24.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            header()
            subheader()
            body()
        }
        rightSideColumn()
    }
}

@PreviewLightDark
@Composable
private fun NavigationBottomPanelPreviewAllSlots() {
    NavSdkExampleTheme {
        NavigationBottomPanel(
            isDeviceInLandscape = false,
            header = {
                Text(
                    text = "Header Title",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            },
            subheader = {
                Text(
                    text = "Subheader description text",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
            },
            body = {
                Text(
                    text = "Body content goes here with additional information",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 8.dp),
                )
            },
            rightSideColumn = {
                Button(onClick = { }) {
                    Text(
                        text = "Action",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            },
        )
    }
}
