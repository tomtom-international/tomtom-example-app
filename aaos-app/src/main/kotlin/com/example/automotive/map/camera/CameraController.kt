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

package com.example.automotive.map.camera

import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.camera.CameraTrackingMode
import com.tomtom.sdk.map.display.compose.state.MapViewState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Manages map camera state, animations, and synchronization with MapViewState.
 */
class CameraController(initialCenter: GeoPoint) {
    private var mapViewState: MapViewState? = null
    private var cameraControllerScope: CoroutineScope? = null

    var currentCameraOptions: CameraOptions = CameraOptions(initialCenter, DEFAULT_CAMERA_ZOOM)

    fun initialize(
        state: MapViewState,
        parentScope: CoroutineScope,
    ) {
        mapViewState = state
        mapViewState?.cameraState?.trackingMode = CameraTrackingMode.FollowNorthUp
        syncWithMapState()
        cameraControllerScope = parentScope
    }

    /**
     * Sets the camera tracking mode.
     */
    fun setCameraTracking(mode: CameraTrackingMode) {
        mapViewState?.cameraState?.trackingMode = mode
    }

    /**
     * Synchronizes the internal camera model with the actual MapViewState.
     * Should be called before camera movements to ensure consistency.
     */
    fun syncWithMapState() {
        mapViewState?.cameraState?.data?.let { cameraData ->
            val position = cameraData.position.position
            val zoom = if (cameraData.position.zoom > 0) cameraData.position.zoom else currentCameraOptions.zoom
            val tilt = cameraData.position.tilt
            val rotation = cameraData.position.rotation
            currentCameraOptions = CameraOptions(position, zoom, tilt, rotation)
        }
    }

    /**
     * Moves the camera to a new position without animation.
     *
     * @param cameraOptions New camera options
     */
    fun moveCamera(cameraOptions: CameraOptions) {
        val state = mapViewState ?: return

        currentCameraOptions = cameraOptions

        cameraControllerScope?.launch {
            state.cameraState.moveCamera(currentCameraOptions)
        }
    }

    /**
     * Animates the camera to a new position with smooth transition.
     *
     * @param cameraOptions New camera options
     */
    fun animateCamera(cameraOptions: CameraOptions) {
        val state = mapViewState ?: return

        currentCameraOptions = cameraOptions

        cameraControllerScope?.launch {
            state.cameraState.animateCamera(currentCameraOptions)
        }
    }

    /**
     * Applies a zoom delta to the current zoom level.
     *
     * @param delta Zoom level change (positive = zoom in, negative = zoom out)
     * @param animate Whether to animate the camera movement
     */
    fun applyZoomDelta(
        delta: Double,
        animate: Boolean = true,
    ) {
        val currentZoom =
            currentCameraOptions.zoom ?: mapViewState?.cameraState?.data?.position?.zoom ?: DEFAULT_CAMERA_ZOOM
        val newZoom = (currentZoom + delta).coerceIn(MIN_ZOOM, MAX_ZOOM)
        if (animate) {
            animateCamera(currentCameraOptions.deepCopy(zoom = newZoom))
        } else {
            moveCamera(currentCameraOptions.deepCopy(zoom = newZoom))
        }
    }

    companion object {
        val DEFAULT_POSITION = GeoPoint(52.3772449, 4.9097159)
        const val DEFAULT_CAMERA_ZOOM = 12.0
        const val POI_CAMERA_ZOOM = 14.0
        const val DEFAULT_TILT = 0.0
        const val DEFAULT_ROTATION = 0.0

        private const val MIN_ZOOM = 2.0
        private const val MAX_ZOOM = 20.0
    }
}
