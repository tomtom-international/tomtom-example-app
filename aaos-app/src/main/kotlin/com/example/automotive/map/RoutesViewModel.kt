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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Manages EV route planning state and operations, and acts as the single source of truth
 * for all state needed to render MainScreen.
 *
 * @param routePlanner The instance used for route planning operations.
 * @param vehicleRepository Repository for vehicle data.
 * @param sdkInitialized StateFlow indicating SDK initialization status.
 * @param initializationError StateFlow containing SDK initialization error, null if no error.
 */
class RoutesViewModel(
    private var routePlanner: RoutePlanner?,
    private val vehicleRepository: VehicleRepository,
    private val sdkInitialized: StateFlow<Boolean>,
    private val initializationError: StateFlow<String?>,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainScreenUIState())
    val uiState: StateFlow<MainScreenUIState> = _uiState.asStateFlow()

    private var currentRequestCallback: RoutePlanningCallback? = null

    init {
        viewModelScope.launch {
            merge(sdkInitialized, initializationError).collect {
                _uiState.update {
                    it.copy(
                        sdkInitialized = sdkInitialized.value,
                        initializationError = initializationError.value,
                    )
                }
            }
        }
    }

    /** Sets permissions granted state from the UI layer. */
    fun setPermissionsGranted(granted: Boolean) {
        _uiState.update { it.copy(permissionsGranted = granted) }
    }

    /** Sets location enabled state from the UI layer. */
    fun setLocationEnabled(enabled: Boolean) {
        _uiState.update { it.copy(locationEnabled = enabled) }
    }

    /** Sets the route planner after SDK initialization. */
    fun setRoutePlanner(planner: RoutePlanner) {
        routePlanner = planner
    }

    /** Plans a sample EV route from Amsterdam to Paris. Does nothing if route planner unavailable. */
    fun planSampleEvRoute() {
        val planner = routePlanner ?: return
        _uiState.update { it.copy(isLoading = true) }

        // Refresh vehicle data to get the latest battery state before planning
        vehicleRepository.configureVehicle()

        val routePlanningOptions = buildSampleEvRouteOptions()

        val callback = object : RoutePlanningCallback {
            override fun onSuccess(result: RoutePlanningResponse) {
                if (currentRequestCallback == this) {
                    _uiState.update {
                        it.copy(
                            routes = result.routes,
                            selectedRoute = result.routes.firstOrNull(),
                            isLoading = false,
                        )
                    }
                    currentRequestCallback = null
                }
            }

            override fun onFailure(failure: RoutingFailure) {
                if (currentRequestCallback == this) {
                    Log.e(TAG, "Route planning failed: $failure")
                    _uiState.update {
                        it.copy(
                            routes = emptyList(),
                            selectedRoute = null,
                            isLoading = false,
                        )
                    }
                    currentRequestCallback = null
                }
            }
        }

        currentRequestCallback = callback
        planner.planRoute(routePlanningOptions, callback)
    }

    fun clearRoutes() {
        _uiState.update { it.copy(routes = emptyList(), selectedRoute = null) }
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
