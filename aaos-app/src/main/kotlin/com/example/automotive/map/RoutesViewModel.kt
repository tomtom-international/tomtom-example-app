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
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.routing.RoutePlanner
import com.tomtom.sdk.routing.RoutePlanningCallback
import com.tomtom.sdk.routing.RoutePlanningResponse
import com.tomtom.sdk.routing.RoutingFailure
import com.tomtom.sdk.routing.buildEvRoutePlanningOptions
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
 * Manages EV route planning state and operations.
 *
 * @param routePlanner The instance used for route planning operations.
 * @param vehicleRepository Repository for vehicle data.
 * @param sdkInitialized StateFlow indicating SDK initialization status.
 * @param initializationError StateFlow containing SDK initialization error, null if no error.
 */
class RoutesViewModel(
    private var routePlanner: RoutePlanner?,
    private val vehicleRepository: VehicleRepository,
    sdkInitialized: StateFlow<Boolean>,
    initializationError: StateFlow<String?>,
) : ViewModel() {
    private val permissionsGranted = MutableStateFlow(false)
    private val _routes: MutableStateFlow<List<Route>> = MutableStateFlow(emptyList())
    val routes: StateFlow<List<Route>> = _routes.asStateFlow()

    private val _selectedRoute = MutableStateFlow<Route?>(null)
    val selectedRoute: StateFlow<Route?> = _selectedRoute.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /** Single source of truth for all state needed to render MainScreen. */
    val uiState: StateFlow<MainScreenUIState> = combine(
        sdkInitialized,
        initializationError,
        permissionsGranted,
        _isLoading,
        _routes,
    ) { sdk, error, perms, loading, routes ->
        MainScreenUIState(
            sdkInitialized = sdk,
            initializationError = error,
            permissionsGranted = perms,
            isLoading = loading,
            hasRoutes = routes.isNotEmpty(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = MainScreenUIState(),
    )

    private var currentRequestCallback: RoutePlanningCallback? = null

    /** Sets permissions granted state from the UI layer. */
    fun setPermissionsGranted(granted: Boolean) {
        permissionsGranted.value = granted
    }

    /** Sets the route planner after SDK initialization. */
    fun setRoutePlanner(planner: RoutePlanner) {
        routePlanner = planner
    }

    /** Plans a sample EV route from Amsterdam to Paris. Does nothing if route planner unavailable. */
    fun planSampleEvRoute() {
        val planner = routePlanner ?: return
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
        planner.planRoute(routePlanningOptions, callback)
    }

    fun clearRoutes() {
        _routes.value = emptyList()
        _selectedRoute.value = null
    }

    override fun onCleared() {
        super.onCleared()
        currentRequestCallback = null
    }

    private fun buildSampleEvRouteOptions() = buildEvRoutePlanningOptions(
        Itinerary(TOMTOM_AMSTERDAM_OFFICE, PARIS),
        chargingOptions = ChargingOptions(
            minChargeAtDestination = INITIAL_MIN_CHARGE,
            minChargeAtChargingStops = INITIAL_MIN_CHARGE,
        ),
    )

    companion object {
        private const val TAG = "RoutesViewModel"

        private const val INITIAL_MIN_CHARGE_KWH = 5.0
        private val INITIAL_MIN_CHARGE: Energy = Energy.kilowattHours(INITIAL_MIN_CHARGE_KWH)
        private val PARIS = GeoPoint(48.8566, 2.3522)
        private val TOMTOM_AMSTERDAM_OFFICE = GeoPoint(52.3766, 4.9082)
    }
}
