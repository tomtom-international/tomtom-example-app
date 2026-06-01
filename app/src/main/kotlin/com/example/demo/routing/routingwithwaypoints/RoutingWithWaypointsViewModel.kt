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

package com.example.demo.routing.routingwithwaypoints

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.application.common.DORDRECHT
import com.example.application.common.HAGUE
import com.example.application.common.ROTTERDAM
import com.example.application.common.TOMTOM_AMSTERDAM_OFFICE
import com.example.application.common.UTRECHT
import com.tomtom.sdk.common.Cancellable
import com.tomtom.sdk.location.Place
import com.tomtom.sdk.routing.RoutePlanner
import com.tomtom.sdk.routing.RoutePlanningCallback
import com.tomtom.sdk.routing.RoutePlanningResponse
import com.tomtom.sdk.routing.RoutingFailure
import com.tomtom.sdk.routing.buildRoutePlanningOptions
import com.tomtom.sdk.routing.options.Itinerary
import com.tomtom.sdk.routing.options.ItineraryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class RoutingWithWaypointsViewModel(
    private val routePlanner: RoutePlanner,
    val onSetIsLoading: (Boolean) -> Unit,
    val onRoutePlanningSuccess: (RoutePlanningResponse) -> Unit,
    val onRoutePlanningFailure: (RoutingFailure) -> Unit,
) : ViewModel() {
    private val amsterdam = ItineraryPoint(Place(TOMTOM_AMSTERDAM_OFFICE))
    private val rotterdam = ItineraryPoint(Place(ROTTERDAM))
    private val hague = ItineraryPoint(Place(HAGUE))
    private val utrecht = ItineraryPoint(Place(UTRECHT))
    private val dordrecht = ItineraryPoint(Place(DORDRECHT))

    private val _isHagueSelected = MutableStateFlow(false)
    val isHagueSelected: StateFlow<Boolean> = _isHagueSelected

    private val _isUtrechtSelected = MutableStateFlow(false)
    val isUtrechtSelected: StateFlow<Boolean> = _isUtrechtSelected

    private val _isDordrechtSelected = MutableStateFlow(false)
    val isDordrechtSelected: StateFlow<Boolean> = _isDordrechtSelected

    private var routePlanJob: Cancellable? = null

    init {
        planRoute()
    }

    fun setHagueSelected(value: Boolean) {
        _isHagueSelected.update { value }
        planRoute()
    }

    fun setUtrechtSelected(value: Boolean) {
        _isUtrechtSelected.update { value }
        planRoute()
    }

    fun setDordrechtSelected(value: Boolean) {
        _isDordrechtSelected.update { value }
        planRoute()
    }

    private fun planRoute() {
        onSetIsLoading(true)
        routePlanJob?.cancel()

        val waypoints = mutableListOf<ItineraryPoint>()
        if (isHagueSelected.value) {
            waypoints.add(hague)
        }
        if (isUtrechtSelected.value) {
            waypoints.add(utrecht)
        }
        if (isDordrechtSelected.value) {
            waypoints.add(dordrecht)
        }

        val itinerary = Itinerary(
            origin = amsterdam,
            destination = rotterdam,
            waypoints = waypoints,
        )

        val routePlanningOptions = buildRoutePlanningOptions(
            itinerary = itinerary,
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

    companion object {
        val ROUTE_PLANNER_KEY = object : CreationExtras.Key<RoutePlanner> {}
        val ON_SET_IS_LOADING_KEY = object : CreationExtras.Key<(Boolean) -> Unit> {}
        val ROUTE_PLANNING_SUCCESS_KEY =
            object : CreationExtras.Key<(RoutePlanningResponse) -> Unit> {}
        val ROUTE_PLANNING_FAILURE_KEY = object : CreationExtras.Key<(RoutingFailure) -> Unit> {}

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                RoutingWithWaypointsViewModel(
                    routePlanner = this[ROUTE_PLANNER_KEY] as RoutePlanner,
                    onSetIsLoading = this[ON_SET_IS_LOADING_KEY] as (Boolean) -> Unit,
                    onRoutePlanningSuccess = this[ROUTE_PLANNING_SUCCESS_KEY] as (RoutePlanningResponse) -> Unit,
                    onRoutePlanningFailure = this[ROUTE_PLANNING_FAILURE_KEY] as (RoutingFailure) -> Unit,
                )
            }
        }
    }
}
