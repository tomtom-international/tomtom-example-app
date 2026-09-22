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

import android.util.Log
import androidx.car.app.CarContext
import androidx.car.app.SurfaceCallback
import androidx.car.app.SurfaceContainer
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.automotive.map.ComposePresentationManager
import com.example.automotive.map.camera.CameraController.Companion.DEFAULT_POSITION
import com.tomtom.sdk.init.TomTomSdk
import com.tomtom.sdk.map.display.MapLocationInfrastructure
import com.tomtom.sdk.map.display.compose.model.MapDisplayInfrastructure
import com.tomtom.sdk.map.display.visualization.navigation.NavigationVisualizationDataProvider
import com.tomtom.sdk.map.display.visualization.navigation.compose.model.NavigationVisualizationInfrastructure
import com.tomtom.sdk.map.display.visualization.routing.RoutingVisualizationDataProvider
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * SurfaceCallback for rendering a minimal TomTom map on the AAOS instrument cluster.
 *
 * Displays the active navigation route and current location marker with automatic
 * camera tracking. The map is non-interactive.
 *
 * @param carContext CarContext for accessing car services.
 * @param lifecycleOwner Lifecycle owner for Compose integration.
 * @param savedStateRegistryOwner Saved state registry owner for Compose.
 * @param viewModelStoreOwner ViewModel store owner for Compose.
 * @param clusterData Container for cluster map state flows.
 */
class ClusterMapSurfaceCallback(
    private val carContext: CarContext,
    private val lifecycleOwner: LifecycleOwner,
    private val savedStateRegistryOwner: SavedStateRegistryOwner,
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val clusterData: ClusterData,
) : SurfaceCallback {
    private val presentationManager = ComposePresentationManager(carContext)

    private val initialCenter =
        TomTomSdk.locationProvider.lastKnownLocation?.position ?: DEFAULT_POSITION

    private val navigationInfrastructure = NavigationVisualizationInfrastructure(
        routingVisualizationDataProvider = flowOf(
            RoutingVisualizationDataProvider(
                routes = clusterData.clusterRoutes,
                selectedRouteId = clusterData.clusterSelectedRoute.map { it?.id },
            ),
        ),
        navigationVisualizationDataProvider = flowOf(
            NavigationVisualizationDataProvider(
                tomtomNavigation = TomTomSdk.navigation,
            ),
        ),
    )

    override fun onSurfaceAvailable(surfaceContainer: SurfaceContainer) {
        if (!surfaceContainer.isValid()) {
            Log.w(TAG, "Surface not ready or has zero size; skipping VirtualDisplay creation.")
            return
        }

        presentationManager.destroy()

        val composeView = createComposeView()

        presentationManager.create(
            surfaceContainer = surfaceContainer,
            composeView = composeView,
        )
    }

    override fun onSurfaceDestroyed(surfaceContainer: SurfaceContainer) {
        presentationManager.destroy()
    }

    private fun SurfaceContainer.isValid(): Boolean = surface != null && width > 0 && height > 0

    private fun createComposeView(): ComposeView = ComposeView(carContext).apply {
        setViewTreeLifecycleOwner(lifecycleOwner)
        setViewTreeSavedStateRegistryOwner(savedStateRegistryOwner)
        setViewTreeViewModelStoreOwner(viewModelStoreOwner)

        setContent {
            ClusterMapContent(
                mapDisplayInfrastructure = MapDisplayInfrastructure(sdkContext = TomTomSdk.sdkContext) {
                    locationInfrastructure = MapLocationInfrastructure {
                        locationProvider = TomTomSdk.locationProvider
                    }
                },
                navigationInfrastructure = navigationInfrastructure,
                isActiveGuidance = clusterData.isActiveGuidance,
                initialCenter = initialCenter,
            )
        }
    }

    private companion object {
        const val TAG = "ClusterMapSurfaceCallback"
    }
}
