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

import androidx.compose.runtime.Stable
import com.example.application.common.PlaceDetails
import com.example.application.map.model.Scenario.FREE_DRIVING
import com.example.application.map.model.Scenario.GUIDANCE
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.camera.CameraTrackingMode

/**
 * UI state for MapScreen; produced by the ViewModel and rendered by the UI.
 */
@Stable
data class MapScreenUiState(
    val scenario: Scenario,
    val cameraTrackingMode: CameraTrackingMode = CameraTrackingMode.None,
    val zoomToAllMarkers: Boolean = false,
    val isInteractiveMode: Boolean = false,
    val destinationDetails: PlaceDetails? = null,
    val placeDetails: PlaceDetails? = null,
    val poiPlaces: List<PlaceDetails> = emptyList(),
    val destinationMarker: GeoPoint? = null,
    val safeAreaTopPadding: Int = 0,
    val safeAreaBottomPadding: Int = 0,
) {
    sealed class ErrorState {
        object SearchError : ErrorState()

        object RoutingError : ErrorState()
    }

    fun isDrivingScenario(): Boolean = scenario == GUIDANCE || scenario == FREE_DRIVING

    fun getRecenterCameraTrackingMode(): CameraTrackingMode = scenario.defaultTrackingMode()

    fun getRecenterCameraOptions(locationProvider: LocationProvider): CameraOptions? =
        scenario.defaultCameraOptions(placeDetails, locationProvider)

    companion object {
        const val HOME_CAMERA_ZOOM = 12.0
        const val POI_CAMERA_ZOOM = 14.0
        const val FREE_DRIVING_CAMERA_ZOOM = 16.0
        const val DEFAULT_TILT = 0.0
        const val DEFAULT_ROTATION = 0.0
    }
}
