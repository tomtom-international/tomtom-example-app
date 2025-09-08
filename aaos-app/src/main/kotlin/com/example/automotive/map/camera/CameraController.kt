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
import com.tomtom.sdk.map.display.compose.state.MapViewState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Manages map camera state, animations, and synchronization with MapViewState.
 */
class CameraController(
    initialCenter: GeoPoint,
    initialZoom: Double,
    private val minZoom: Double = 2.0,
    private val maxZoom: Double = 20.0,
) {
    private var mapViewState: MapViewState? = null
    private var animationScope: CoroutineScope? = null

    var currentCenter: GeoPoint = initialCenter
        private set

    var currentZoom: Double = initialZoom
        private set

    fun initialize(state: MapViewState) {
        mapViewState = state
        syncWithMapState()
        animationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    }

    fun cleanup() {
        animationScope?.cancel()
        animationScope = null
        mapViewState = null
    }

    /**
     * Synchronizes the internal camera model with the actual MapViewState.
     * Should be called before camera movements to ensure consistency.
     */
    fun syncWithMapState() {
        mapViewState?.cameraState?.data?.let { cameraData ->
            currentCenter = cameraData.position.position
            currentZoom = cameraData.position.zoom
        }
    }

    /**
     * Moves the camera to a new position without animation.
     *
     * @param newCenter New camera center position
     * @param newZoom New zoom level (will be clamped to min/max range)
     */
    fun moveCamera(
        newCenter: GeoPoint,
        newZoom: Double,
    ) {
        val state = mapViewState ?: return

        currentCenter = newCenter
        currentZoom = newZoom.coerceIn(minZoom, maxZoom)

        animationScope?.launch {
            state.cameraState.moveCamera(
                CameraOptions(
                    position = currentCenter,
                    zoom = currentZoom,
                ),
            )
        }
    }

    /**
     * Animates the camera to a new position with smooth transition.
     *
     * @param newCenter New camera center position
     * @param newZoom New zoom level (will be clamped to min/max range)
     */
    private fun animateCamera(
        newCenter: GeoPoint,
        newZoom: Double,
    ) {
        val state = mapViewState ?: return

        currentCenter = newCenter
        currentZoom = newZoom.coerceIn(minZoom, maxZoom)

        animationScope?.launch {
            state.cameraState.animateCamera(
                CameraOptions(
                    position = currentCenter,
                    zoom = currentZoom,
                ),
            )
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
        val newZoom = currentZoom + delta
        if (animate) {
            animateCamera(currentCenter, newZoom)
        } else {
            moveCamera(currentCenter, newZoom)
        }
    }
}
