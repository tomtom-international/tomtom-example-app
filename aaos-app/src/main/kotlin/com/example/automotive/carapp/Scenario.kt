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

package com.example.automotive.carapp

import com.example.automotive.common.PlaceDetails
import com.example.automotive.map.camera.CameraController.Companion.DEFAULT_CAMERA_ZOOM
import com.example.automotive.map.camera.CameraController.Companion.DEFAULT_ROTATION
import com.example.automotive.map.camera.CameraController.Companion.DEFAULT_TILT
import com.example.automotive.map.camera.CameraController.Companion.POI_CAMERA_ZOOM
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.camera.CameraTrackingMode

/**
 * High-level modes for the MainScreenUIState.
 */
enum class Scenario {
    HOME,
    POI_FOCUS,
    ROUTE_PREVIEW,
    GUIDANCE,
    DESTINATION_ARRIVAL,
    ;

    fun defaultTrackingMode(): CameraTrackingMode = when (this) {
        HOME -> CameraTrackingMode.FollowNorthUp
        POI_FOCUS, DESTINATION_ARRIVAL -> CameraTrackingMode.None
        ROUTE_PREVIEW -> CameraTrackingMode.RouteOverview
        GUIDANCE -> CameraTrackingMode.FollowRouteDirection
    }

    fun defaultCameraOptions(
        place: PlaceDetails?,
        locationProvider: LocationProvider?,
    ): CameraOptions? = when (this) {
        HOME -> CameraOptions(
            zoom = DEFAULT_CAMERA_ZOOM,
            tilt = DEFAULT_TILT,
            rotation = DEFAULT_ROTATION,
            position = locationProvider?.lastKnownLocation?.position,
        )

        POI_FOCUS, DESTINATION_ARRIVAL -> CameraOptions(
            zoom = POI_CAMERA_ZOOM,
            tilt = DEFAULT_TILT,
            position = place?.place?.coordinate,
        )

        ROUTE_PREVIEW, GUIDANCE -> null
    }
}
