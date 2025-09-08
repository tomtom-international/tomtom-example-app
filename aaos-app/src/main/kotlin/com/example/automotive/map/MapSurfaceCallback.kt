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

import android.content.Context
import android.util.Log
import androidx.car.app.SurfaceCallback
import androidx.car.app.SurfaceContainer
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.automotive.map.camera.CameraController
import com.example.automotive.map.camera.CameraCoordinateCalculator
import com.tomtom.sdk.init.TomTomSdk
import com.tomtom.sdk.init.TomTomSdk.sdkContext
import com.tomtom.sdk.map.display.MapLocationInfrastructure
import com.tomtom.sdk.map.display.compose.model.MapDisplayInfrastructure
import com.tomtom.sdk.map.display.visualization.navigation.NavigationVisualizationDataProvider
import com.tomtom.sdk.map.display.visualization.navigation.compose.model.NavigationVisualizationInfrastructure
import com.tomtom.sdk.map.display.visualization.routing.RoutingVisualizationDataProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

private const val INITIAL_ZOOM = 14.0
private const val MIN_ZOOM = 2.0
private const val MAX_ZOOM = 20.0
private const val DEFAULT_GESTURE_SENSITIVITY = 2.0

/**
 * SurfaceCallback that coordinates map display, camera gestures, and navigation visualization
 * using VirtualDisplay and Compose presentation.
 *
 * @param context Android context
 * @param lifecycleOwner Lifecycle owner for Compose integration
 * @param savedStateRegistryOwner Saved state registry owner for Compose
 * @param viewModelStoreOwner ViewModel store owner for Compose
 * @param routesViewModel ViewModel providing route data for visualization
 */
class MapSurfaceCallback(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val savedStateRegistryOwner: SavedStateRegistryOwner,
    private val viewModelStoreOwner: ViewModelStoreOwner,
    routesViewModel: RoutesViewModel,
) : SurfaceCallback {
    private val presentationManager = ComposePresentationManager(context)
    private val cameraController = CameraController(
        initialCenter = TOMTOM_AMSTERDAM_OFFICE,
        initialZoom = INITIAL_ZOOM,
        minZoom = MIN_ZOOM,
        maxZoom = MAX_ZOOM,
    )

    private val navigationInfrastructure = MutableStateFlow(
        NavigationVisualizationInfrastructure(
            routingVisualizationDataProvider = flowOf(
                RoutingVisualizationDataProvider(
                    routes = routesViewModel.routes,
                    selectedRouteId = routesViewModel.selectedRoute.map { it?.id },
                ),
            ),
            navigationVisualizationDataProvider = flowOf(
                NavigationVisualizationDataProvider(
                    tomtomNavigation = TomTomSdk.navigation,
                ),
            ),
        ),
    )

    override fun onSurfaceAvailable(surfaceContainer: SurfaceContainer) {
        if (!surfaceContainer.isValid()) {
            Log.w(TAG, "Surface not ready or has zero size; skipping VirtualDisplay creation.")
            return
        }

        val mapDisplayInfrastructure = createMapDisplayInfrastructure()
        val composeView = createComposeView(mapDisplayInfrastructure)

        presentationManager.create(
            surfaceContainer = surfaceContainer,
            composeView = composeView,
        )
    }

    override fun onSurfaceDestroyed(surfaceContainer: SurfaceContainer) {
        presentationManager.destroy()
        cameraController.cleanup()
    }

    override fun onScroll(
        distanceX: Float,
        distanceY: Float,
    ) {
        cameraController.syncWithMapState()

        val newCenter = CameraCoordinateCalculator.computePannedCenter(
            currentCenter = cameraController.currentCenter,
            currentZoom = cameraController.currentZoom,
            distanceX = distanceX,
            distanceY = distanceY,
        )

        cameraController.moveCamera(newCenter, cameraController.currentZoom)
    }

    override fun onScale(
        focusX: Float,
        focusY: Float,
        scaleFactor: Float,
    ) {
        cameraController.syncWithMapState()

        val zoomDelta = CameraCoordinateCalculator.scaleFactorToZoomDelta(
            scaleFactor = scaleFactor,
            sensitivity = DEFAULT_GESTURE_SENSITIVITY,
        )

        cameraController.applyZoomDelta(zoomDelta, animate = true)
    }

    private fun SurfaceContainer.isValid(): Boolean = surface != null && width > 0 && height > 0

    private fun createMapDisplayInfrastructure(): MapDisplayInfrastructure =
        MapDisplayInfrastructure(sdkContext = sdkContext) {
            locationInfrastructure = MapLocationInfrastructure {
                locationProvider = TomTomSdk.locationProvider
            }
        }

    private fun createComposeView(mapDisplayInfrastructure: MapDisplayInfrastructure): ComposeView =
        ComposeView(context).apply {
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(savedStateRegistryOwner)
            setViewTreeViewModelStoreOwner(viewModelStoreOwner)

            setContent {
                MapScreen(
                    mapDisplayInfrastructure = mapDisplayInfrastructure,
                    navigationInfrastructure = navigationInfrastructure,
                    onMapViewStateReady = { state ->
                        cameraController.initialize(state)
                    },
                )
            }
        }

    private companion object {
        const val TAG = "MapSurfaceCallback"
    }
}
