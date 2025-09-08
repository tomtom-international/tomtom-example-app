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

package com.example.application.common.ui

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
