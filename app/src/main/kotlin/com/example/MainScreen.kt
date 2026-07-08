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

package com.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.example.Destination.ChildActivityDestination.NavigationActivityDestination
import com.example.Destination.DemoListScreenDestination
import com.example.application.ui.theme.NavSdkExampleTheme

/**
 * This is the home screen of the application showing navigation options.
 */
@Composable
fun MainScreen(
    onNavigateToDestination: (Destination) -> Unit,
    modifier: Modifier = Modifier,
) {
    PaddedLazyColumn(modifier = modifier) {
        item {
            DestinationCard(
                onClick = { onNavigateToDestination(NavigationActivityDestination) },
                title = stringResource(R.string.navsdk_application_title),
                subtitle = stringResource(R.string.navsdk_application_subtitle),
            )
        }

        item {
            DestinationCard(
                onClick = { onNavigateToDestination(DemoListScreenDestination) },
                title = stringResource(R.string.demo_examples_title),
                subtitle = stringResource(R.string.demo_examples_subtitle),
            )
        }
    }
}

@Composable
fun PaddedLazyColumn(
    modifier: Modifier,
    content: LazyListScope.() -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(8.dp),
        content = content,
    )
}

@PreviewLightDark
@Composable
private fun PaddedLazyColumnPreview() {
    NavSdkExampleTheme {
        PaddedLazyColumn(modifier = Modifier) {
            item {
                DestinationCard(
                    onClick = { },
                    title = stringResource(R.string.navsdk_application_title),
                    subtitle = stringResource(R.string.navsdk_application_subtitle),
                )
            }
            item {
                DestinationCard(
                    onClick = { },
                    title = stringResource(R.string.demo_examples_title),
                    subtitle = stringResource(R.string.demo_examples_subtitle),
                )
            }
        }
    }
}

@Composable
fun DestinationCard(
    onClick: () -> Unit,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val contentColor = if (enabled) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    }
    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                modifier = Modifier.padding(16.dp),
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.titleMedium,
                color = contentColor,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun DestinationCardPreview() {
    NavSdkExampleTheme {
        Column {
            DestinationCard(
                onClick = { },
                title = stringResource(R.string.demo_examples_title),
                subtitle = stringResource(R.string.demo_examples_subtitle),
            )
        }
    }
}
