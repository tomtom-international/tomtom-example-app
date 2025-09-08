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

package com.example.automotive.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.automotive.carapp.MainScreenUIState
import com.example.automotive.vehicle.VehicleRepository
import com.tomtom.quantity.Energy
import com.tomtom.sdk.init.TomTomSdk
import com.tomtom.sdk.init.createRoutePlanner
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.routing.RoutePlanner
import com.tomtom.sdk.routing.RoutePlanningCallback
import com.tomtom.sdk.routing.RoutePlanningResponse
import com.tomtom.sdk.routing.RoutingFailure
import com.tomtom.sdk.routing.buildEvRoutePlanningOptions
import com.tomtom.sdk.routing.common.BetaEvRoutePlanningOptionsApi
import com.tomtom.sdk.routing.options.ChargingOptions
import com.tomtom.sdk.routing.options.Itinerary
import com.tomtom.sdk.routing.route.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel that manages EV route planning state and operations.
 *
 * @param routePlanner The route planner instance (injectable for testing)
 * @param vehicleRepository Repository for refreshing vehicle data before route planning
 */
class RoutesViewModel(
    private val routePlanner: RoutePlanner = TomTomSdk.createRoutePlanner(),
    private val vehicleRepository: VehicleRepository,
) : ViewModel() {
    private val _routes: MutableStateFlow<List<Route>> = MutableStateFlow(emptyList())
    val routes: StateFlow<List<Route>> = _routes.asStateFlow()

    private val _selectedRoute = MutableStateFlow<Route?>(null)
    val selectedRoute: StateFlow<Route?> = _selectedRoute.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * Combined UI state that atomically captures loading and routes state.
     * This prevents race conditions when reading state in template building.
     */
    val uiState: StateFlow<MainScreenUIState> = combine(
        _isLoading,
        _routes,
    ) { loading, routes ->
        MainScreenUIState(
            isLoading = loading,
            hasRoutes = routes.isNotEmpty(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = MainScreenUIState(),
    )

    private var currentRequestCallback: RoutePlanningCallback? = null

    /**
     * Plans a sample EV route from Amsterdam to Paris.
     * Refreshes vehicle data before planning to ensure the latest car information is used.
     * Sets loading state while route planning is in progress.
     */
    fun planSampleEvRoute() {
        _isLoading.value = true

        // Refresh vehicle data to get the latest battery state before planning
        vehicleRepository.configureVehicle()

        val routePlanningOptions = buildSampleEvRouteOptions()

        val callback = object : RoutePlanningCallback {
            override fun onSuccess(result: RoutePlanningResponse) {
                if (currentRequestCallback == this) {
                    val firstRoute = result.routes.firstOrNull()
                    _routes.value = result.routes
                    _selectedRoute.value = firstRoute
                    _isLoading.value = false
                    currentRequestCallback = null
                }
            }

            override fun onFailure(failure: RoutingFailure) {
                if (currentRequestCallback == this) {
                    Log.e(TAG, "Route planning failed: $failure")
                    _routes.value = emptyList()
                    _selectedRoute.value = null
                    _isLoading.value = false
                    currentRequestCallback = null
                }
            }
        }

        currentRequestCallback = callback
        routePlanner.planRoute(routePlanningOptions, callback)
    }

    fun clearRoutes() {
        _routes.value = emptyList()
        _selectedRoute.value = null
    }

    override fun onCleared() {
        super.onCleared()
        currentRequestCallback = null
    }

    @Suppress("detekt:OptInAnnotationDetected")
    @OptIn(BetaEvRoutePlanningOptionsApi::class)
    private fun buildSampleEvRouteOptions() = buildEvRoutePlanningOptions(
        Itinerary(TOMTOM_AMSTERDAM_OFFICE, PARIS),
        chargingOptions = ChargingOptions(
            minChargeAtDestination = INITIAL_MIN_CHARGE,
            minChargeAtChargingStops = INITIAL_MIN_CHARGE,
        ),
    )

    companion object {
        private const val TAG = "RoutesViewModel"

        /**
         * Stop timeout for the UI state Flow when there are no active collectors.
         *
         * The 5-second delay prevents unnecessary Flow restarts when collectors briefly disconnect
         * and reconnect during configuration changes (e.g., screen rotation) or rapid navigation
         * transitions. If all collectors unsubscribe, the Flow waits 5 seconds before stopping.
         * If a new collector subscribes within that window, the Flow continues without restart.
         */
        private const val STOP_TIMEOUT_MILLIS = 5000L

        private const val INITIAL_MIN_CHARGE_KWH = 5.0
        private val INITIAL_MIN_CHARGE: Energy = Energy.kilowattHours(INITIAL_MIN_CHARGE_KWH)
        private val PARIS = GeoPoint(48.8566, 2.3522)
        private val TOMTOM_AMSTERDAM_OFFICE = GeoPoint(52.3766, 4.9082)
    }
}
