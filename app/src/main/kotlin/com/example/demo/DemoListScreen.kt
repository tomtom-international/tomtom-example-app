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

package com.example.demo

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.Destination
import com.example.Destination.RoutingListScreenDestination
import com.example.Destination.SearchListScreenDestination
import com.example.DestinationCard
import com.example.PaddedLazyColumn
import com.example.R

/**
 * Top-level catalog of demos.
 * Lists demos by feature area (Routing, Search) and navigates to their lists.
 */
@Composable
fun DemoListScreen(
    onNavigateToDestination: (Destination) -> Unit,
    modifier: Modifier = Modifier,
) {
    PaddedLazyColumn(modifier = modifier) {
        item {
            DestinationCard(
                onClick = { onNavigateToDestination(RoutingListScreenDestination) },
                title = stringResource(R.string.demo_routing_title),
                subtitle = stringResource(R.string.demo_routing_subtitle),
            )
        }

        item {
            DestinationCard(
                onClick = { onNavigateToDestination(SearchListScreenDestination) },
                title = stringResource(R.string.demo_search_title),
                subtitle = stringResource(R.string.demo_search_subtitle),
            )
        }
    }
}
