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

package com.example.application.map.model

import com.example.application.common.PlaceDetails
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.camera.CameraTrackingMode
import com.tomtom.sdk.map.display.camera.FollowCameraOperatorConfig

/**
 * High-level modes for the Map screen UI.
 */
enum class Scenario {
    HOME,
    POI_FOCUS,
    ROUTE_PREVIEW,
    GUIDANCE,
    FREE_DRIVING,
    DESTINATION_ARRIVAL,
    ;

    fun defaultTrackingMode(): CameraTrackingMode = when (this) {
        HOME, POI_FOCUS, DESTINATION_ARRIVAL -> CameraTrackingMode.None
        ROUTE_PREVIEW -> CameraTrackingMode.RouteOverview
        GUIDANCE -> CameraTrackingMode.FollowRouteDirection
        FREE_DRIVING -> CameraTrackingMode.FollowDirection(
            FollowCameraOperatorConfig(defaultZoom = MapScreenUiState.FREE_DRIVING_CAMERA_ZOOM),
        )
    }

    fun defaultCameraOptions(
        place: PlaceDetails?,
        locationProvider: LocationProvider,
    ): CameraOptions? = when (this) {
        HOME -> CameraOptions(
            zoom = MapScreenUiState.HOME_CAMERA_ZOOM,
            tilt = MapScreenUiState.DEFAULT_TILT,
            rotation = MapScreenUiState.DEFAULT_ROTATION,
            position = locationProvider.lastKnownLocation?.position,
        )
        POI_FOCUS, DESTINATION_ARRIVAL -> CameraOptions(
            zoom = MapScreenUiState.POI_CAMERA_ZOOM,
            tilt = MapScreenUiState.DEFAULT_TILT,
            position = place?.place?.coordinate,
        )
        ROUTE_PREVIEW, FREE_DRIVING, GUIDANCE -> null
    }
}
