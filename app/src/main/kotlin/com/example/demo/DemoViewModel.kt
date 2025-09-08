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

package com.example.demo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.BuildConfig
import com.example.application.map.MapScreenUiState.ErrorState
import com.example.application.settings.data.SettingsRepository
import com.tomtom.sdk.annotations.AlphaSdkInitializationApi
import com.tomtom.sdk.entrypoint.TomTomSdk
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.map.display.MapDataInfrastructure
import com.tomtom.sdk.map.display.MapLocationInfrastructure
import com.tomtom.sdk.map.display.annotation.BetaMapInfrastructureApi
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.compose.BetaMapComposableApi
import com.tomtom.sdk.map.display.compose.model.MapInfrastructure
import com.tomtom.sdk.map.display.mapdatastore.common.ExperimentalMapDataStoreApi
import com.tomtom.sdk.map.display.visualization.navigation.NavigationVisualizationDataProvider
import com.tomtom.sdk.map.display.visualization.navigation.annotations.BetaNavigationVisualizationDataProviderApi
import com.tomtom.sdk.map.display.visualization.navigation.compose.model.NavigationVisualizationInfrastructure
import com.tomtom.sdk.map.display.visualization.routing.RoutingVisualizationDataProvider
import com.tomtom.sdk.map.display.visualization.routing.annotations.BetaRoutingVisualizationDataProviderApi
import com.tomtom.sdk.navigation.TomTomNavigation
import com.tomtom.sdk.routing.route.Route
import com.tomtom.sdk.routing.route.RouteId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Suppress("detekt:TooManyFunctions")
@OptIn(
    BetaMapComposableApi::class,
    BetaMapInfrastructureApi::class,
    AlphaSdkInitializationApi::class,
    ExperimentalMapDataStoreApi::class,
    BetaRoutingVisualizationDataProviderApi::class,
    BetaNavigationVisualizationDataProviderApi::class,
)
class DemoViewModel(
    settingsRepository: SettingsRepository,
    val navigation: TomTomNavigation,
) : ViewModel() {
    private val _mapUiState: MutableStateFlow<DemoMapUiState> = MutableStateFlow(DemoMapUiState())
    val mapUiState: StateFlow<DemoMapUiState> = _mapUiState.asStateFlow()

    private val _errorState: MutableStateFlow<ErrorState?> = MutableStateFlow(null)
    val errorState: StateFlow<ErrorState?> = _errorState.asStateFlow()

    private val _zoomToAllMarkers = MutableStateFlow(false)
    val zoomToAllMarkers: StateFlow<Boolean> = _zoomToAllMarkers.asStateFlow()

    val isTtpLogsEnabled: StateFlow<Boolean> = settingsRepository.settings.map { it.isNavSdkTtpLogsEnabled }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = false,
        )

    private val _cameraOptions = MutableStateFlow<CameraOptions?>(null)
    val cameraOptions: StateFlow<CameraOptions?> = _cameraOptions.asStateFlow()

    private val _routes = MutableStateFlow<List<Route>>(emptyList())
    val routes: StateFlow<List<Route>> = _routes

    private val _selectedRoute = MutableStateFlow<Route?>(null)
    val selectedRoute: StateFlow<Route?> = _selectedRoute

    private val _mapInfrastructure = MutableStateFlow(
        MapInfrastructure(
            dataInfrastructure = MapDataInfrastructure(
                apiKey = BuildConfig.TOMTOM_API_KEY,
                dataStore = TomTomSdk.sdkContext.mapDataStore,
            ),
        ) {
            locationInfrastructure = MapLocationInfrastructure {
                locationProvider = TomTomSdk.locationProvider
            }
        },
    )
    val mapInfrastructure: StateFlow<MapInfrastructure> = _mapInfrastructure

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
        _routes.update { routes }
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
        val SETTINGS_REPOSITORY_KEY = object : CreationExtras.Key<SettingsRepository> {}
        val NAVIGATION_KEY = object : CreationExtras.Key<TomTomNavigation> {}

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                DemoViewModel(
                    settingsRepository = this[SETTINGS_REPOSITORY_KEY] as SettingsRepository,
                    navigation = this[NAVIGATION_KEY] as TomTomNavigation,
                )
            }
        }
    }
}
