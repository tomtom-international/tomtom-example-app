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

package com.example.application.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.map.display.visualization.navigation.NavigationVisualizationDataProvider
import com.tomtom.sdk.map.display.visualization.navigation.compose.model.NavigationVisualizationInfrastructure
import com.tomtom.sdk.map.display.visualization.routing.RoutingVisualizationDataProvider
import com.tomtom.sdk.navigation.TomTomNavigation
import com.tomtom.sdk.routing.RoutePlanner
import com.tomtom.sdk.routing.RoutePlanningCallback
import com.tomtom.sdk.routing.RoutePlanningResponse
import com.tomtom.sdk.routing.RoutingFailure
import com.tomtom.sdk.routing.buildRoutePlanningOptions
import com.tomtom.sdk.routing.options.Itinerary
import com.tomtom.sdk.routing.options.RoutePlanningOptions
import com.tomtom.sdk.routing.route.Route
import com.tomtom.sdk.routing.route.RouteStop
import com.tomtom.sdk.routing.route.RouteStopId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.util.Locale

/**
 * Manages route planning, selection, and navigation visualization state for the map screen.
 */
class RoutesViewModel(
    private val routePlanner: RoutePlanner,
    private val navigation: TomTomNavigation,
) : ViewModel() {
    lateinit var routePlanningOptions: RoutePlanningOptions

    private val _routes = MutableStateFlow<List<Route>>(emptyList())
    val routes: StateFlow<List<Route>> = _routes

    private val _selectedRoute = MutableStateFlow<Route?>(null)
    val selectedRoute: StateFlow<Route?> = _selectedRoute

    private val _navigationInfrastructure = MutableStateFlow(
        NavigationVisualizationInfrastructure(
            routingVisualizationDataProvider = flowOf(
                RoutingVisualizationDataProvider(
                    routes = routes,
                    selectedRouteId = selectedRoute.map { it?.id },
                ),
            ),
            navigationVisualizationDataProvider = flowOf(
                NavigationVisualizationDataProvider(
                    tomtomNavigation = navigation,
                ),
            ),
        ),
    )
    val navigationInfrastructure: StateFlow<NavigationVisualizationInfrastructure> = _navigationInfrastructure

    fun getRouteStop(routeStopId: RouteStopId): RouteStop? {
        return selectedRoute.value?.routeStops?.firstOrNull { it.id == routeStopId }
    }

    fun clearRoutes() {
        _routes.update { emptyList() }
        _selectedRoute.update { null }
    }

    fun planRoute(
        origin: GeoPoint,
        destination: GeoPoint,
        onRoutePlanningSuccess: () -> Unit,
        onRoutePlanningFailure: (RoutingFailure) -> Unit,
        waypoints: List<GeoPoint> = emptyList(),
    ) {
        planRoute(
            newRoutePlanningOptions = buildRoutePlanningOptions(
                itinerary = Itinerary(origin, destination, waypoints),
                language = Locale.getDefault(),
            ),
            onRoutePlanningSuccess = onRoutePlanningSuccess,
            onRoutePlanningFailure = onRoutePlanningFailure,
        )
    }

    private fun planRoute(
        newRoutePlanningOptions: RoutePlanningOptions,
        onRoutePlanningSuccess: () -> Unit,
        onRoutePlanningFailure: (RoutingFailure) -> Unit,
    ) {
        routePlanningOptions = newRoutePlanningOptions
        routePlanner.planRoute(
            routePlanningOptions = routePlanningOptions,
            object : RoutePlanningCallback {
                override fun onSuccess(result: RoutePlanningResponse) {
                    _routes.update { result.routes }
                    _selectedRoute.update { result.routes.firstOrNull() }
                    onRoutePlanningSuccess()
                }

                override fun onFailure(failure: RoutingFailure) {
                    onRoutePlanningFailure(failure)
                }
            },
        )
    }

    companion object {
        val ROUTE_PLANNER_KEY = object : CreationExtras.Key<RoutePlanner> {}
        val NAVIGATION_KEY = object : CreationExtras.Key<TomTomNavigation> {}

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                RoutesViewModel(
                    routePlanner = this[ROUTE_PLANNER_KEY] as RoutePlanner,
                    navigation = this[NAVIGATION_KEY] as TomTomNavigation,
                )
            }
        }
    }
}
