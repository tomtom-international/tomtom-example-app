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

package com.example.demo.search

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.Destination
import com.example.Destination.ChildActivityDestination.AutocompleteDestination
import com.example.Destination.ChildActivityDestination.EvSearchDestination
import com.example.Destination.ChildActivityDestination.PoiAlongRouteDestination
import com.example.Destination.ChildActivityDestination.PoiSearchAreaDestination
import com.example.DestinationCard
import com.example.PaddedLazyColumn
import com.example.R

/**
 * Search demos list.
 * Shows available search examples (EV, along route, in area, autocomplete).
 */
@Composable
fun SearchListScreen(
    onNavigateToDestination: (Destination) -> Unit,
    modifier: Modifier = Modifier,
) {
    PaddedLazyColumn(modifier = modifier) {
        item {
            DestinationCard(
                onClick = { onNavigateToDestination(EvSearchDestination) },
                title = stringResource(R.string.demo_search_ev_title),
                subtitle = stringResource(R.string.demo_search_ev_subtitle),
            )
        }

        item {
            DestinationCard(
                onClick = { onNavigateToDestination(PoiAlongRouteDestination) },
                title = stringResource(R.string.demo_search_poi_along_route_title),
                subtitle = stringResource(R.string.demo_search_poi_along_route_subtitle),
            )
        }

        item {
            DestinationCard(
                onClick = { onNavigateToDestination(PoiSearchAreaDestination) },
                title = stringResource(R.string.demo_search_poi_in_area_title),
                subtitle = stringResource(R.string.demo_search_poi_in_area_subtitle),
            )
        }

        item {
            DestinationCard(
                onClick = { onNavigateToDestination(AutocompleteDestination) },
                title = stringResource(R.string.demo_search_autocomplete_title),
                subtitle = stringResource(R.string.demo_search_autocomplete_subtitle),
            )
        }
    }
}
