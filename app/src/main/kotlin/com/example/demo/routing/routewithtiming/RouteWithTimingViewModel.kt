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

package com.example.demo.routing.routewithtiming

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.application.common.PARIS
import com.example.application.common.TOMTOM_AMSTERDAM_OFFICE
import com.example.demo.routing.ldevr.EvSampleCars
import com.tomtom.quantity.Energy
import com.tomtom.sdk.common.Cancellable
import com.tomtom.sdk.location.Place
import com.tomtom.sdk.routing.RoutePlanner
import com.tomtom.sdk.routing.RoutePlanningCallback
import com.tomtom.sdk.routing.RoutePlanningResponse
import com.tomtom.sdk.routing.RoutingFailure
import com.tomtom.sdk.routing.buildEvRoutePlanningOptions
import com.tomtom.sdk.routing.buildRoutePlanningOptions
import com.tomtom.sdk.routing.options.ChargingOptions
import com.tomtom.sdk.routing.options.Itinerary
import com.tomtom.sdk.routing.options.ItineraryPoint
import com.tomtom.sdk.routing.options.calculation.PlanningTime
import com.tomtom.sdk.routing.route.RouteId
import com.tomtom.sdk.vehicle.Vehicle
import com.tomtom.sdk.vehicle.provider.VehicleProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.time.ZonedDateTime

private const val MIN_CHARGE_AT_STOP = 5
private const val MIN_CHARGE_AT_DESTINATION = 10

class RouteWithTimingViewModel(
    private val routePlanner: RoutePlanner,
    val onSetIsLoading: (Boolean) -> Unit,
    val onRoutePlanningSuccess: (RoutePlanningResponse) -> Unit,
    val onRoutePlanningFailure: (RoutingFailure) -> Unit,
    val onSelectRoute: (RouteId) -> Unit,
) : ViewModel() {
    private val _isEvCar = MutableStateFlow(false)
    val isEvCar: StateFlow<Boolean> = _isEvCar

    private val _isArrivalTimeSelected = MutableStateFlow(false)
    val isArrivalTimeSelected: StateFlow<Boolean> = _isArrivalTimeSelected

    private val _isDepartureTimeSelected = MutableStateFlow(false)
    val isDepartureTimeSelected: StateFlow<Boolean> = _isDepartureTimeSelected

    private val _planningDateTime = MutableStateFlow<ZonedDateTime?>(null)
    val planningDateTime: StateFlow<ZonedDateTime?> = _planningDateTime

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

    fun setCarType(isEv: Boolean) {
        if (_isEvCar.value != isEv) {
            _isEvCar.update { isEv }
            planRoute()
        }
    }

    fun setTimingPreference(
        isArrivalSelected: Boolean,
        isDepartureSelected: Boolean,
        planningDateTime: ZonedDateTime?,
    ) {
        if (_isArrivalTimeSelected.value != isArrivalSelected ||
            _isDepartureTimeSelected.value != isDepartureSelected ||
            _planningDateTime.value != planningDateTime
        ) {
            _isArrivalTimeSelected.update { isArrivalSelected }
            _isDepartureTimeSelected.update { isDepartureSelected }
            _planningDateTime.update { planningDateTime }
            if (planningDateTime != null) {
                planRoute()
            }
        }
    }

    private fun planRoute() {
        onSetIsLoading(true)
        routePlanJob?.cancel()

        val planningTime: PlanningTime? = _planningDateTime.value?.let { selectedDateTime ->
            when {
                _isArrivalTimeSelected.value -> PlanningTime.ArrivalTime(selectedDateTime)
                _isDepartureTimeSelected.value -> PlanningTime.DepartureTime(selectedDateTime)
                else -> null
            }
        }

        val itinerary = if (planningTime != null) {
            Itinerary(
                origin = ItineraryPoint(Place(TOMTOM_AMSTERDAM_OFFICE)),
                destination = ItineraryPoint(Place(PARIS)),
                planningTime = planningTime,
            )
        } else {
            Itinerary(
                origin = ItineraryPoint(Place(TOMTOM_AMSTERDAM_OFFICE)),
                destination = ItineraryPoint(Place(PARIS)),
            )
        }

        val callback = object : RoutePlanningCallback {
            override fun onSuccess(result: RoutePlanningResponse) {
                onRoutePlanningSuccess(result)
            }

            override fun onFailure(failure: RoutingFailure) {
                onRoutePlanningFailure(failure)
            }
        }

        if (_isEvCar.value) {
            VehicleProvider.vehicle = EvSampleCars.mediumRangeCar
            routePlanJob = routePlanner.planRoute(
                routePlanningOptions = buildEvRoutePlanningOptions(
                    itinerary = itinerary,
                    chargingOptions = chargingOptions,
                ),
                callback,
            )
        } else {
            VehicleProvider.vehicle = Vehicle.Car(electricEngine = null)
            routePlanJob = routePlanner.planRoute(
                routePlanningOptions = buildRoutePlanningOptions(
                    itinerary = itinerary,
                ),
                callback,
            )
        }
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

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                RouteWithTimingViewModel(
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
