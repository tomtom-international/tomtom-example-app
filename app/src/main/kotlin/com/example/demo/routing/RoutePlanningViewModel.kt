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

package com.example.demo.routing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.application.common.LONDON
import com.example.application.common.PARIS
import com.tomtom.sdk.routing.RoutePlanner
import com.tomtom.sdk.routing.RoutePlanningCallback
import com.tomtom.sdk.routing.RoutePlanningResponse
import com.tomtom.sdk.routing.RoutingFailure
import com.tomtom.sdk.routing.common.BetaRoutePlanningOptionsApi
import com.tomtom.sdk.routing.createRoutePlanningOptions
import com.tomtom.sdk.routing.options.Itinerary
import com.tomtom.sdk.routing.options.calculation.AvoidType
import com.tomtom.sdk.routing.options.calculation.RouteType
import com.tomtom.sdk.routing.route.RouteId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

@OptIn(BetaRoutePlanningOptionsApi::class)
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
        val routePlanningOptions = createRoutePlanningOptions(
            itinerary = Itinerary(PARIS, LONDON),
            maxAlternatives = 2,
            routeType = routeType.value,
            avoids = getAvoids(),
        )
        routePlanner.planRoute(
            routePlanningOptions = routePlanningOptions,
            object : RoutePlanningCallback {
                override fun onSuccess(result: RoutePlanningResponse) {
                    onSetIsLoading(false)
                    onRoutePlanningSuccess(result)
                }

                override fun onFailure(failure: RoutingFailure) {
                    onSetIsLoading(false)
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
