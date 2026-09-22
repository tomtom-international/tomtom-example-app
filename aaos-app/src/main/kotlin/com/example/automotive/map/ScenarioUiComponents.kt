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

package com.example.automotive.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.automotive.R
import com.example.automotive.carapp.MainScreenUIState
import com.example.automotive.carapp.Scenario.DESTINATION_ARRIVAL
import com.example.automotive.carapp.Scenario.GUIDANCE
import com.example.automotive.carapp.Scenario.POI_FOCUS
import com.example.automotive.carapp.Scenario.ROUTE_PREVIEW
import com.example.automotive.common.PlaceDetails
import com.example.automotive.common.extension.formattedArrivalTime
import com.example.automotive.common.extension.formattedDistance
import com.example.automotive.common.extension.formattedDuration
import com.example.automotive.common.extension.formattedRemainingDistance
import com.example.automotive.common.locationDetails
import com.example.automotive.common.name
import com.tomtom.sdk.navigation.progress.RouteProgress
import kotlinx.coroutines.flow.StateFlow

@Composable
fun BoxScope.ScenarioUiComponents(
    uiState: MainScreenUIState,
    routeProgressFlow: StateFlow<RouteProgress?>,
) {
    when (uiState.scenario) {
        POI_FOCUS -> {
            uiState.placeDetails?.let { placeDetails ->
                PoiFocusPanel(
                    placeDetails = placeDetails,
                    modifier = Modifier.align(Alignment.BottomStart),
                )
            }
        }

        ROUTE_PREVIEW -> {
            uiState.selectedRoute?.let { route ->
                RoutePreviewPanel(
                    eta = route.formattedArrivalTime(),
                    remainingDistance = route.formattedDistance(),
                    remainingDuration = route.formattedDuration(),
                    modifier = Modifier.align(Alignment.BottomStart),
                )
            }
        }

        GUIDANCE -> {
            GuidancePanel(
                routeProgressFlow = routeProgressFlow,
                routeFormattedArrivalTime = uiState.selectedRoute?.formattedArrivalTime() ?: "",
                modifier = Modifier.align(Alignment.BottomStart),
            )
        }

        DESTINATION_ARRIVAL -> {
            DestinationArrivalPanel(
                modifier = Modifier.align(Alignment.BottomStart),
            )
        }

        else -> {}
    }
}

@Composable
private fun GuidancePanel(
    routeProgressFlow: StateFlow<RouteProgress?>,
    routeFormattedArrivalTime: String,
    modifier: Modifier = Modifier,
) {
    val routeProgress by routeProgressFlow.collectAsState()

    PanelCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            val remainingDistance = routeProgress?.formattedRemainingDistance()
            val remainingTime = routeProgress?.formattedDuration()
            Text(
                text = remainingTime ?: routeFormattedArrivalTime,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )

            Text(
                text = remainingDistance ?: "",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@Composable
private fun RoutePreviewPanel(
    eta: String,
    remainingDistance: String,
    modifier: Modifier = Modifier,
    remainingDuration: String? = null,
) {
    PanelCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = eta,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Row {
                Text(
                    text = remainingDistance,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
                if (remainingDuration != null) {
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
                )
            }
        }
    }
}

@Composable
private fun PoiFocusPanel(
    placeDetails: PlaceDetails,
    modifier: Modifier = Modifier,
) {
    PanelCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = placeDetails.name,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )

            val subheaderText = placeDetails.place.address?.freeformAddress ?: placeDetails.locationDetails
            if (subheaderText.isNotEmpty()) {
                Text(
                    text = subheaderText,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun DestinationArrivalPanel(modifier: Modifier = Modifier) {
    PanelCard(modifier = modifier) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = stringResource(R.string.map_title_destination_arrival),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun PanelCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth(fraction = 0.5f)
            .padding(16.dp)
            .background(color = MaterialTheme.colorScheme.background),
    ) {
        content()
    }
}
