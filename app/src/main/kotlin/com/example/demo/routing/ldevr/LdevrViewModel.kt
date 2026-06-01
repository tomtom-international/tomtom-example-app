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

package com.example.demo.routing.ldevr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.application.common.PARIS
import com.example.application.common.TOMTOM_AMSTERDAM_OFFICE
import com.tomtom.quantity.Energy
import com.tomtom.sdk.common.Cancellable
import com.tomtom.sdk.routing.RoutePlanner
import com.tomtom.sdk.routing.RoutePlanningCallback
import com.tomtom.sdk.routing.RoutePlanningResponse
import com.tomtom.sdk.routing.RoutingFailure
import com.tomtom.sdk.routing.buildEvRoutePlanningOptions
import com.tomtom.sdk.routing.options.ChargingOptions
import com.tomtom.sdk.routing.options.Itinerary
import com.tomtom.sdk.routing.route.RouteId
import com.tomtom.sdk.vehicle.Vehicle
import com.tomtom.sdk.vehicle.provider.VehicleProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

private const val MIN_CHARGE_AT_STOP = 5
private const val MIN_CHARGE_AT_DESTINATION = 10

/**
 * ViewModel for the Long Distance EV Routing demo.
 */
class LdevrViewModel(
    private val routePlanner: RoutePlanner,
    val onSetIsLoading: (Boolean) -> Unit,
    val onRoutePlanningSuccess: (RoutePlanningResponse) -> Unit,
    val onRoutePlanningFailure: (RoutingFailure) -> Unit,
    val onSelectRoute: (RouteId) -> Unit,
) : ViewModel() {
    private val _evCarRange = MutableStateFlow(EvCarRange.Large)
    val evCarRange: StateFlow<EvCarRange> = _evCarRange

    private var routePlanJob: Cancellable? = null

    private val chargingOptions = ChargingOptions(
        minChargeAtChargingStops = Energy.kilowattHours(MIN_CHARGE_AT_STOP),
        minChargeAtDestination = Energy.kilowattHours(MIN_CHARGE_AT_DESTINATION),
    )

    init {
        planRoute()
    }

    fun onRouteClick(routeId: RouteId) {
        onSelectRoute(routeId)
    }

    fun setEvCarRange(range: EvCarRange) {
        if (_evCarRange.value != range) {
            _evCarRange.update { range }
            planRoute()
        }
    }

    private fun planRoute() {
        onSetIsLoading(true)
        routePlanJob?.cancel()

        VehicleProvider.vehicle = when (_evCarRange.value) {
            EvCarRange.Short -> EvSampleCars.shortRangeCar
            EvCarRange.Medium -> EvSampleCars.mediumRangeCar
            EvCarRange.Large -> EvSampleCars.largeRangeCar
        }

        val routePlanningOptions = buildEvRoutePlanningOptions(
            itinerary = Itinerary(TOMTOM_AMSTERDAM_OFFICE, PARIS),
            chargingOptions = chargingOptions,
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

    override fun onCleared() {
        super.onCleared()
        VehicleProvider.vehicle = Vehicle.Car(electricEngine = null)
    }

    companion object {
        val ROUTE_PLANNER_KEY = object : CreationExtras.Key<RoutePlanner> {}
        val ON_SET_IS_LOADING_KEY = object : CreationExtras.Key<(Boolean) -> Unit> {}
        val ROUTE_PLANNING_SUCCESS_KEY = object : CreationExtras.Key<(RoutePlanningResponse) -> Unit> {}
        val ROUTE_PLANNING_FAILURE_KEY = object : CreationExtras.Key<(RoutingFailure) -> Unit> {}
        val SELECT_ROUTE_KEY = object : CreationExtras.Key<(RouteId) -> Unit> {}

        const val TAG = "LdevrViewModel"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                LdevrViewModel(
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
