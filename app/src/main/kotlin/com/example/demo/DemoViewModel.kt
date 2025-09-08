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

package com.example.demo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.application.map.model.MapScreenUiState.ErrorState
import com.tomtom.sdk.init.TomTomSdk
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.map.display.MapLocationInfrastructure
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.compose.model.MapDisplayInfrastructure
import com.tomtom.sdk.map.display.visualization.navigation.NavigationVisualizationDataProvider
import com.tomtom.sdk.map.display.visualization.navigation.compose.model.NavigationVisualizationInfrastructure
import com.tomtom.sdk.map.display.visualization.routing.RoutingVisualizationDataProvider
import com.tomtom.sdk.navigation.TomTomNavigation
import com.tomtom.sdk.routing.route.Route
import com.tomtom.sdk.routing.route.RouteId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the demos screen.
 * Manages demo map/navigation infrastructure, UI state, and interactions like bottom sheet and routes.
 */
@Suppress("detekt:TooManyFunctions")
class DemoViewModel(
    val navigation: TomTomNavigation,
) : ViewModel() {
    private val _mapUiState: MutableStateFlow<DemoMapUiState> = MutableStateFlow(DemoMapUiState())
    val mapUiState: StateFlow<DemoMapUiState> = _mapUiState.asStateFlow()

    private val _errorState: MutableStateFlow<ErrorState?> = MutableStateFlow(null)
    val errorState: StateFlow<ErrorState?> = _errorState.asStateFlow()

    private val _zoomToAllMarkers = MutableStateFlow(false)
    val zoomToAllMarkers: StateFlow<Boolean> = _zoomToAllMarkers.asStateFlow()

    private val _cameraOptions = MutableStateFlow<CameraOptions?>(null)
    val cameraOptions: StateFlow<CameraOptions?> = _cameraOptions.asStateFlow()

    private val routes = MutableStateFlow<List<Route>>(emptyList())

    private val _selectedRoute = MutableStateFlow<Route?>(null)
    val selectedRoute: StateFlow<Route?> = _selectedRoute

    private val _mapDisplayInfrastructure = MutableStateFlow(
        MapDisplayInfrastructure(
            sdkContext = TomTomSdk.sdkContext,
        ) {
            locationInfrastructure = MapLocationInfrastructure {
                locationProvider = TomTomSdk.locationProvider
            }
        },
    )

    val mapDisplayInfrastructure: StateFlow<MapDisplayInfrastructure> = _mapDisplayInfrastructure

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

    private var _isBottomSheetExpanded = MutableStateFlow(false)
    val isBottomSheetExpanded: StateFlow<Boolean> = _isBottomSheetExpanded.asStateFlow()

    fun toggleBottomSheet(isExpanded: Boolean? = null) {
        if (isExpanded == null) {
            _isBottomSheetExpanded.update { !it }
        } else {
            _isBottomSheetExpanded.update { isExpanded }
        }
    }

    fun zoomToAllMarkers() {
        _zoomToAllMarkers.update { !it }
    }

    fun updateCameraOptions(
        coordinate: GeoPoint,
        zoom: Double,
        tilt: Double,
    ) {
        _cameraOptions.tryEmit(
            CameraOptions(
                zoom = zoom,
                tilt = tilt,
                position = coordinate,
            ),
        )
    }

    fun setIsLoading(isLoading: Boolean) {
        _mapUiState.update { it.copy(isLoading = isLoading) }
    }

    fun updateErrorState(updateBlock: (ErrorState?) -> ErrorState?) {
        setIsLoading(false)
        _errorState.update(updateBlock)
    }

    fun updateRoutes(
        routes: List<Route>,
        selectedRoute: Route?,
    ) {
        setIsLoading(false)
        this.routes.update { routes }
        _selectedRoute.update { selectedRoute }
    }

    fun selectRoute(routeId: RouteId) {
        viewModelScope.launch {
            routes.value.firstOrNull { it.id == routeId }?.let { route ->
                _selectedRoute.update { route }
            }
        }
    }

    fun updateSafeAreaTopPadding(topPadding: Int) {
        _mapUiState.update { it.copy(safeAreaTopPadding = topPadding) }
    }

    fun updateSafeAreaBottomPadding(bottomPadding: Int) {
        _mapUiState.update { it.copy(safeAreaBottomPadding = bottomPadding) }
    }

    fun updateMapStyleUrl(mapStyleUrl: String) {
        _mapUiState.update { it.copy(mapStyleUrl = mapStyleUrl) }
    }

    fun clearErrorState() {
        _errorState.update { null }
    }

    companion object {
        val NAVIGATION_KEY = object : CreationExtras.Key<TomTomNavigation> {}

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                DemoViewModel(
                    navigation = this[NAVIGATION_KEY] as TomTomNavigation,
                )
            }
        }
    }
}
