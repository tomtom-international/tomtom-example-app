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

package com.example

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.example.Destination.ChildActivityDestination.NavigationActivityDestination
import com.example.Destination.DemoListScreenDestination
import com.example.application.ui.theme.NavSdkExampleTheme

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
                    title = "Destination Card Title",
                    subtitle = "Destination Card Subtitle. Subtitle description.",
                )
            }
            item {
                DestinationCard(
                    onClick = { },
                    title = "Destination Card Title",
                    subtitle = "Destination Card Subtitle. Subtitle description.",
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
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row {
            Image(
                painter = painterResource(id = R.drawable.placeholder_demo_card),
                contentDescription = stringResource(id = R.string.placeholder_demo_image),
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterVertically)
                    .width(100.dp),
            )
            Column(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .fillMaxWidth(),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(16.dp),
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(16.dp),
                )
            }
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
                title = "Destination Card Title",
                subtitle = "Destination Card Subtitle. Subtitle description.",
            )
        }
    }
}
