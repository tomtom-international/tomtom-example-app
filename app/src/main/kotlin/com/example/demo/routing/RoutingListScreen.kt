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

package com.example.demo.routing

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.Destination
import com.example.Destination.ChildActivityDestination.RoutePlanningDestination
import com.example.DestinationCard
import com.example.PaddedLazyColumn
import com.example.R

/**
 * Routing demos list.
 * Shows available routing examples and navigates to the selected demo.
 */
@Composable
fun RoutingListScreen(
    onNavigateToDestination: (Destination) -> Unit,
    modifier: Modifier = Modifier,
) {
    PaddedLazyColumn(modifier = modifier) {
        item {
            DestinationCard(
                onClick = { onNavigateToDestination(RoutePlanningDestination) },
                title = stringResource(R.string.demo_route_planning_title),
                subtitle = stringResource(R.string.demo_route_planning_subtitle),
            )
        }
    }
}
