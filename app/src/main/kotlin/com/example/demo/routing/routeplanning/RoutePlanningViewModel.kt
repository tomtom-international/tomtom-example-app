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

package com.example.demo.routing.routeplanning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.application.common.LONDON
import com.example.application.common.PARIS
import com.tomtom.sdk.common.Cancellable
import com.tomtom.sdk.routing.RoutePlanner
import com.tomtom.sdk.routing.RoutePlanningCallback
import com.tomtom.sdk.routing.RoutePlanningResponse
import com.tomtom.sdk.routing.RoutingFailure
import com.tomtom.sdk.routing.buildRoutePlanningOptions
import com.tomtom.sdk.routing.options.Itinerary
import com.tomtom.sdk.routing.options.calculation.AvoidType
import com.tomtom.sdk.routing.options.calculation.RouteType
import com.tomtom.sdk.routing.route.RouteId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel for the route planning demo.
 * Holds route type/avoid toggles and triggers route planning; notifies UI on success/failure.
 */
class RoutePlanningViewModel(
    private val routePlanner: RoutePlanner,
    val onSetIsLoading: (Boolean) -> Unit,
    val onRoutePlanningSuccess: (RoutePlanningResponse) -> Unit,
    val onRoutePlanningFailure: (RoutingFailure) -> Unit,
    val onSelectRoute: (RouteId) -> Unit,
) : ViewModel() {
    private val _routeType = MutableStateFlow<RouteType>(RouteType.Fast)
    val routeType: StateFlow<RouteType> = _routeType

    private val _avoidMotorways = MutableStateFlow(false)
    val avoidMotorways: StateFlow<Boolean> = _avoidMotorways

    private val _avoidTolls = MutableStateFlow(false)
    val avoidTolls: StateFlow<Boolean> = _avoidTolls

    private val _avoidFerries = MutableStateFlow(false)
    val avoidFerries: StateFlow<Boolean> = _avoidFerries

    private var routePlanJob: Cancellable? = null

    init {
        planRoute()
    }

    fun setRouteType(routeType: RouteType) {
        if (_routeType.value != routeType) {
            _routeType.value = routeType
            planRoute()
        }
    }

    fun onRouteClick(routeId: RouteId) {
        onSelectRoute(routeId)
    }

    fun setAvoidMotorways(value: Boolean) {
        _avoidMotorways.update { value }
        planRoute()
    }

    fun setAvoidTolls(value: Boolean) {
        _avoidTolls.update { value }
        planRoute()
    }

    fun setAvoidFerries(value: Boolean) {
        _avoidFerries.update { value }
        planRoute()
    }

    private fun planRoute() {
        onSetIsLoading(true)
        routePlanJob?.cancel()

        val routePlanningOptions = buildRoutePlanningOptions(
            itinerary = Itinerary(PARIS, LONDON),
            maxAlternatives = 2,
            routeType = routeType.value,
            avoids = getAvoids(),
        )
        routePlanJob = routePlanner.planRoute(
            routePlanningOptions = routePlanningOptions,
            object : RoutePlanningCallback {
                override fun onSuccess(result: RoutePlanningResponse) {
                    onRoutePlanningSuccess(result)
                }

                override fun onFailure(failure: RoutingFailure) {
                    onRoutePlanningFailure(failure)
                }
            },
        )
    }

    private fun getAvoids(): Set<AvoidType> {
        val avoidTypes = mutableSetOf<AvoidType>()
        if (avoidMotorways.value) {
            avoidTypes.add(AvoidType.Motorways)
        }

        if (avoidTolls.value) {
            avoidTypes.add(AvoidType.TollRoads)
        }

        if (avoidFerries.value) {
            avoidTypes.add(AvoidType.Ferries)
        }
        return avoidTypes
    }

    companion object {
        val ROUTE_PLANNER_KEY = object : CreationExtras.Key<RoutePlanner> {}
        val ON_SET_IS_LOADING_KEY = object : CreationExtras.Key<(Boolean) -> Unit> {}
        val ROUTE_PLANNING_SUCCESS_KEY =
            object : CreationExtras.Key<(RoutePlanningResponse) -> Unit> {}
        val ROUTE_PLANNING_FAILURE_KEY = object : CreationExtras.Key<(RoutingFailure) -> Unit> {}
        val SELECT_ROUTE_KEY = object : CreationExtras.Key<(RouteId) -> Unit> {}

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                RoutePlanningViewModel(
                    routePlanner = this[ROUTE_PLANNER_KEY] as RoutePlanner,
                    onSetIsLoading = this[ON_SET_IS_LOADING_KEY] as (Boolean) -> Unit,
                    onRoutePlanningSuccess = this[ROUTE_PLANNING_SUCCESS_KEY] as (RoutePlanningResponse) -> Unit,
                    onRoutePlanningFailure = this[ROUTE_PLANNING_FAILURE_KEY] as (RoutingFailure) -> Unit,
                    onSelectRoute = this[SELECT_ROUTE_KEY] as (RouteId) -> Unit,
                )
            }
        }
    }
}
