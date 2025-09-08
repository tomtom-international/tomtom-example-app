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

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import com.example.automotive.R
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.map.display.camera.InitialCameraOptions
import com.tomtom.sdk.map.display.compose.TomTomMap
import com.tomtom.sdk.map.display.compose.model.MapDisplayInfrastructure
import com.tomtom.sdk.map.display.compose.nodes.Traffic
import com.tomtom.sdk.map.display.compose.state.MapViewState
import com.tomtom.sdk.map.display.compose.state.rememberMapViewState
import com.tomtom.sdk.map.display.compose.state.rememberTrafficState
import com.tomtom.sdk.map.display.style.StyleMode
import com.tomtom.sdk.map.display.visualization.navigation.compose.NavigationVisualization
import com.tomtom.sdk.map.display.visualization.navigation.compose.model.NavigationVisualizationInfrastructure
import kotlinx.coroutines.flow.StateFlow

private const val TOMTOM_AMSTERDAM_LATITUDE = 52.3772449
private const val TOMTOM_AMSTERDAM_LONGITUDE = 4.9097159
private const val INITIAL_ZOOM = 14.0

val TOMTOM_AMSTERDAM_OFFICE = GeoPoint(TOMTOM_AMSTERDAM_LATITUDE, TOMTOM_AMSTERDAM_LONGITUDE)

/**
 * Map screen composable for AAOS application.
 *
 * Renders the TomTom map with navigation visualization and forwards the MapViewState
 * back to the host for gesture handling.
 *
 * @param mapDisplayInfrastructure Infrastructure for map display
 * @param navigationInfrastructure Flow of navigation visualization infrastructure
 * @param onMapViewStateReady Callback invoked when MapViewState is ready
 */
@Composable
fun MapScreen(
    mapDisplayInfrastructure: MapDisplayInfrastructure,
    navigationInfrastructure: StateFlow<NavigationVisualizationInfrastructure>,
    onMapViewStateReady: (MapViewState) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(getMapBackgroundColor()),
        contentAlignment = Alignment.Center,
    ) {
        val mapViewState = rememberStyledMapViewState(
            initialCameraOptions = InitialCameraOptions.LocationBased(
                position = TOMTOM_AMSTERDAM_OFFICE,
                zoom = INITIAL_ZOOM,
            ),
        )

        // Expose MapViewState back to host so gestures from SurfaceCallback can control the camera
        LaunchedEffect(mapViewState) {
            onMapViewStateReady(mapViewState)
        }

        TomTomMap(
            infrastructure = mapDisplayInfrastructure,
            state = mapViewState,
        ) {
            Traffic(
                state = rememberTrafficState(
                    showTrafficFlow = false,
                    showTrafficIncidents = true,
                ),
            )

            val infra = navigationInfrastructure.collectAsState().value
            NavigationVisualization(
                infrastructure = infra,
            )
        }
    }
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
