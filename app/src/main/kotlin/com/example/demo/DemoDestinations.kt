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

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.Destination
import com.example.Destination.DemoListScreenDestination
import com.example.Destination.MapListScreenDestination
import com.example.Destination.RoutingListScreenDestination
import com.example.Destination.SearchListScreenDestination
import com.example.demo.map.MapListScreen
import com.example.demo.routing.RoutingListScreen
import com.example.demo.search.SearchListScreen

fun NavGraphBuilder.getDemoDestinations(
    navigateToDestination: (Destination) -> Unit,
    modifier: Modifier,
) {
    composable<DemoListScreenDestination> {
        DemoListScreen(
            onNavigateToDestination = { navigateToDestination(it) },
            modifier = modifier,
        )
    }

    composable<RoutingListScreenDestination> {
        RoutingListScreen(
            onNavigateToDestination = { navigateToDestination(it) },
            modifier = modifier,
        )
    }

    composable<SearchListScreenDestination> {
        SearchListScreen(
            onNavigateToDestination = { navigateToDestination(it) },
            modifier = modifier,
        )
    }

    composable<MapListScreenDestination> {
        MapListScreen(
            onNavigateToDestination = { navigateToDestination(it) },
            modifier = modifier,
        )
    }
}
