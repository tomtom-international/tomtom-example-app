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

package com.example.automotive.carapp.cluster

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.automotive.R
import com.example.automotive.map.camera.CameraController.Companion.DEFAULT_CAMERA_ZOOM
import com.example.automotive.map.camera.CameraController.Companion.DEFAULT_POSITION
import com.example.automotive.map.camera.CameraController.Companion.DEFAULT_ROTATION
import com.example.automotive.map.camera.CameraController.Companion.DEFAULT_TILT
import com.tomtom.sdk.init.TomTomSdk
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.camera.CameraTrackingMode
import com.tomtom.sdk.map.display.camera.InitialCameraOptions
import com.tomtom.sdk.map.display.compose.TomTomMap
import com.tomtom.sdk.map.display.compose.model.MapDisplayInfrastructure
import com.tomtom.sdk.map.display.compose.nodes.CurrentLocationMarker
import com.tomtom.sdk.map.display.compose.properties.CurrentLocationMarkerProperties
import com.tomtom.sdk.map.display.compose.state.MapViewState
import com.tomtom.sdk.map.display.compose.state.rememberMapViewState
import com.tomtom.sdk.map.display.location.LocationMarkerOptions
import com.tomtom.sdk.map.display.style.StyleMode
import com.tomtom.sdk.map.display.visualization.navigation.compose.NavigationVisualization
import com.tomtom.sdk.map.display.visualization.navigation.compose.model.NavigationVisualizationInfrastructure
import kotlinx.coroutines.flow.StateFlow

/**
 * Minimal map composable for the AAOS instrument cluster display.
 *
 * Renders a TomTom map with the active navigation route and current location marker.
 * The camera automatically follows the route when guidance is active, and centers on the current
 * location when guidance is not active. The map is non-interactive (no pan, zoom, or click).
 *
 * @param mapDisplayInfrastructure Infrastructure for map display.
 * @param navigationInfrastructure Navigation visualization infrastructure.
 * @param isActiveGuidance Flow indicating whether navigation guidance is currently active.
 * @param initialCenter Initial camera center position; defaults to TomTom Amsterdam office.
 * @param modifier Modifier for the composable.
 */
@Composable
fun ClusterMapContent(
    mapDisplayInfrastructure: MapDisplayInfrastructure,
    navigationInfrastructure: NavigationVisualizationInfrastructure,
    isActiveGuidance: StateFlow<Boolean>,
    modifier: Modifier = Modifier,
    initialCenter: GeoPoint = DEFAULT_POSITION,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(getClusterMapBackgroundColor()),
        contentAlignment = Alignment.Center,
    ) {
        val mapViewState = rememberStyledMapViewState(
            initialCameraOptions = InitialCameraOptions.LocationBased(
                position = initialCenter,
                zoom = DEFAULT_CAMERA_ZOOM,
                tilt = DEFAULT_TILT,
                rotation = DEFAULT_ROTATION,
            ),
        )

        val isGuiding by isActiveGuidance.collectAsStateWithLifecycle()
        LaunchedEffect(isGuiding) {
            if (isGuiding) {
                mapViewState.cameraState.trackingMode = CameraTrackingMode.FollowRouteDirection
            } else {
                mapViewState.cameraState.trackingMode = CameraTrackingMode.FollowNorthUp
                TomTomSdk.locationProvider.lastKnownLocation?.position?.let {
                    mapViewState.cameraState.animateCamera(
                        CameraOptions(
                            position = it,
                            zoom = DEFAULT_CAMERA_ZOOM,
                            tilt = DEFAULT_TILT,
                            rotation = DEFAULT_ROTATION,
                        ),
                    )
                }
            }
        }

        TomTomMap(
            infrastructure = mapDisplayInfrastructure,
            state = mapViewState,
        ) {
            CurrentLocationMarker(
                properties = CurrentLocationMarkerProperties {
                    type = LocationMarkerOptions.Type.Chevron
                },
            )

            NavigationVisualization(infrastructure = navigationInfrastructure)
        }
    }
}

@Composable
private fun getClusterMapBackgroundColor() = if (isSystemInDarkTheme()) {
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
