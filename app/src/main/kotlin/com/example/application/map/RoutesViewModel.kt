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

package com.example.application.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.Place
import com.tomtom.sdk.map.display.compose.BetaMapComposableApi
import com.tomtom.sdk.map.display.visualization.navigation.NavigationVisualizationDataProvider
import com.tomtom.sdk.map.display.visualization.navigation.annotations.BetaNavigationVisualizationDataProviderApi
import com.tomtom.sdk.map.display.visualization.navigation.compose.model.NavigationVisualizationInfrastructure
import com.tomtom.sdk.map.display.visualization.routing.RoutingVisualizationDataProvider
import com.tomtom.sdk.map.display.visualization.routing.annotations.BetaRoutingVisualizationDataProviderApi
import com.tomtom.sdk.navigation.TomTomNavigation
import com.tomtom.sdk.navigation.addItineraryPoint
import com.tomtom.sdk.navigation.skipRouteStop
import com.tomtom.sdk.routing.RoutePlanner
import com.tomtom.sdk.routing.RoutePlanningCallback
import com.tomtom.sdk.routing.RoutePlanningResponse
import com.tomtom.sdk.routing.RoutingFailure
import com.tomtom.sdk.routing.common.BetaRoutePlanningOptionsApi
import com.tomtom.sdk.routing.createRoutePlanningOptions
import com.tomtom.sdk.routing.options.Itinerary
import com.tomtom.sdk.routing.options.ItineraryPoint
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

@OptIn(
    BetaMapComposableApi::class,
    BetaNavigationVisualizationDataProviderApi::class,
    BetaRoutingVisualizationDataProviderApi::class,
)
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

    fun addWayPoint(
        waypoint: Place,
        onRoutePlanningSuccess: () -> Unit,
        onRoutePlanningFailure: (RoutingFailure) -> Unit,
    ) {
        planRoute(
            newRoutePlanningOptions = routePlanningOptions.addItineraryPoint(ItineraryPoint(waypoint)),
            onRoutePlanningSuccess = onRoutePlanningSuccess,
            onRoutePlanningFailure = onRoutePlanningFailure,
        )
    }

    fun removeWayPoint(
        routeStop: RouteStop,
        onRoutePlanningSuccess: () -> Unit,
        onRoutePlanningFailure: (RoutingFailure) -> Unit,
    ) {
        planRoute(
            newRoutePlanningOptions = routePlanningOptions.skipRouteStop(routeStop),
            onRoutePlanningSuccess = onRoutePlanningSuccess,
            onRoutePlanningFailure = onRoutePlanningFailure,
        )
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
        @OptIn(BetaRoutePlanningOptionsApi::class)
        planRoute(
            newRoutePlanningOptions = createRoutePlanningOptions(
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
                    _selectedRoute.update { routes.value.firstOrNull() }
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
