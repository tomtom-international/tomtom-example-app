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

import androidx.car.app.CarContext
import androidx.car.app.CarToast
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.automotive.R
import com.example.automotive.carapp.MainViewModel
import com.example.automotive.carapp.Scenario
import com.example.automotive.carapp.Scenario.DESTINATION_ARRIVAL
import com.example.automotive.carapp.Scenario.GUIDANCE
import com.example.automotive.carapp.Scenario.HOME
import com.example.automotive.carapp.Scenario.POI_FOCUS
import com.example.automotive.carapp.Scenario.ROUTE_PREVIEW
import com.example.automotive.map.camera.CameraController.Companion.DEFAULT_CAMERA_ZOOM
import com.example.automotive.map.camera.CameraController.Companion.DEFAULT_POSITION
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.map.display.camera.InitialCameraOptions
import com.tomtom.sdk.map.display.compose.TomTomMap
import com.tomtom.sdk.map.display.compose.model.MapDisplayInfrastructure
import com.tomtom.sdk.map.display.compose.model.MarkerData
import com.tomtom.sdk.map.display.compose.nodes.CurrentLocationMarker
import com.tomtom.sdk.map.display.compose.nodes.Marker
import com.tomtom.sdk.map.display.compose.nodes.Traffic
import com.tomtom.sdk.map.display.compose.properties.CurrentLocationMarkerProperties
import com.tomtom.sdk.map.display.compose.properties.MarkerProperties
import com.tomtom.sdk.map.display.compose.state.MapViewState
import com.tomtom.sdk.map.display.compose.state.rememberCurrentLocationMarkerState
import com.tomtom.sdk.map.display.compose.state.rememberMapViewState
import com.tomtom.sdk.map.display.compose.state.rememberMarkerState
import com.tomtom.sdk.map.display.compose.state.rememberTrafficState
import com.tomtom.sdk.map.display.image.ImageFactory
import com.tomtom.sdk.map.display.location.LocationMarkerOptions
import com.tomtom.sdk.map.display.style.StyleMode
import com.tomtom.sdk.map.display.visualization.navigation.compose.NavigationVisualization
import com.tomtom.sdk.map.display.visualization.navigation.compose.model.NavigationVisualizationInfrastructure
import kotlinx.coroutines.flow.StateFlow

/**
 * Map screen composable for AAOS application.
 *
 * Renders the TomTom map with navigation visualization and forwards the MapViewState
 * back to the host for gesture handling.
 *
 * @param carContext CarContext from Android Automotive framework
 * @param mapDisplayInfrastructure Infrastructure for map display
 * @param navigationInfrastructure Flow of navigation visualization infrastructure
 * @param mainViewModel ViewModel providing UI state and handling map interactions
 * @param onMapViewStateReady Callback invoked when MapViewState is ready
 * @param modifier The [Modifier] to be applied to this [MapScreen].
 * @param initialCenter Initial camera center position; defaults to TomTom Amsterdam office
 */
@Composable
fun MapScreen(
    carContext: CarContext,
    mapDisplayInfrastructure: MapDisplayInfrastructure,
    navigationInfrastructure: StateFlow<NavigationVisualizationInfrastructure>,
    mainViewModel: MainViewModel,
    onMapViewStateReady: (MapViewState) -> Unit,
    modifier: Modifier = Modifier,
    initialCenter: GeoPoint = DEFAULT_POSITION,
) {
    val uiState by mainViewModel.uiState.collectAsState()

    LaunchedEffect(uiState.failureMessage) {
        uiState.failureMessage?.let { error ->
            CarToast.makeText(carContext, error, CarToast.LENGTH_LONG).show()
            mainViewModel.dismissFailureMessage()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(getMapBackgroundColor()),
        contentAlignment = Alignment.Center,
    ) {
        val mapViewState = rememberStyledMapViewState(
            initialCameraOptions = InitialCameraOptions.LocationBased(
                position = initialCenter,
                zoom = DEFAULT_CAMERA_ZOOM,
            ),
        )

        mapViewState.safeArea = PaddingValues(
            start = safeAreaStartPadding(uiState.scenario),
        )

        // Expose MapViewState back to host so gestures from SurfaceCallback can control the camera
        LaunchedEffect(mapViewState) {
            onMapViewStateReady(mapViewState)
        }

        TomTomMap(
            infrastructure = mapDisplayInfrastructure,
            state = mapViewState,
            onMapClick = { mainViewModel.onMapClick(it) },
        ) {
            Traffic(
                state = rememberTrafficState(
                    showTrafficFlow = false,
                    showTrafficIncidents = true,
                ),
            )

            CurrentLocationMarker(
                properties = CurrentLocationMarkerProperties {
                    type = LocationMarkerOptions.Type.Chevron
                },
                state = rememberCurrentLocationMarkerState(),
            )

            NavigationVisualization(infrastructure = navigationInfrastructure.collectAsState().value)

            if (uiState.scenario == POI_FOCUS) {
                uiState.placeDetails?.let { placeDetails ->
                    Marker(
                        data = MarkerData(geoPoint = placeDetails.place.coordinate),
                        properties = MarkerProperties(pinImage = ImageFactory.fromResource(R.drawable.tt_pin)),
                        state = rememberMarkerState(),
                    )
                }
            }
        }

        ScenarioUiComponents(uiState, mainViewModel.routeProgress)
    }
}

@Composable
@ReadOnlyComposable
fun safeAreaStartPadding(scenario: Scenario): Dp = when (scenario) {
    HOME, POI_FOCUS, ROUTE_PREVIEW, DESTINATION_ARRIVAL -> 0.dp
    GUIDANCE -> LocalDensity.current.run { (LocalWindowInfo.current.containerSize.width.toDp() / 2) }
}

@Composable
private fun getMapBackgroundColor() = if (isSystemInDarkTheme()) {
    colorResource(R.color.map_background_dark)
} else {
    colorResource(R.color.map_background_light)
}

@Composable
private fun rememberStyledMapViewState(initialCameraOptions: InitialCameraOptions): MapViewState {
    val styleMode = if (isSystemInDarkTheme()) StyleMode.DARK else StyleMode.MAIN

    val mapViewState = rememberMapViewState(
        initialCameraOptions = initialCameraOptions,
    ) {
        styleState.styleMode = styleMode
    }

    SideEffect {
        mapViewState.styleState.styleMode = styleMode
    }

    return mapViewState
}
