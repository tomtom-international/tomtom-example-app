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

import android.annotation.SuppressLint
import android.os.Build
import android.os.SystemClock
import android.util.Log
import android.view.MotionEvent
import android.view.WindowInsets
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.car.app.CarContext
import androidx.car.app.SurfaceCallback
import androidx.car.app.SurfaceContainer
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.automotive.carapp.MainViewModel
import com.example.automotive.map.camera.CameraController
import com.example.automotive.map.camera.CameraController.Companion.DEFAULT_POSITION
import com.example.automotive.map.camera.CameraCoordinateCalculator
import com.tomtom.sdk.init.TomTomSdk
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.map.display.MapLocationInfrastructure
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.camera.CameraTrackingMode
import com.tomtom.sdk.map.display.compose.model.MapDisplayInfrastructure
import com.tomtom.sdk.map.display.visualization.navigation.NavigationVisualizationDataProvider
import com.tomtom.sdk.map.display.visualization.navigation.compose.model.NavigationVisualizationInfrastructure
import com.tomtom.sdk.map.display.visualization.routing.RoutingVisualizationDataProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

private const val DEFAULT_GESTURE_SENSITIVITY = 2.0

/**
 * SurfaceCallback that coordinates map display, camera gestures, and navigation visualization
 * using VirtualDisplay and Compose presentation.
 *
 * @param carContext CarContext for accessing car services
 * @param lifecycleOwner Lifecycle owner for Compose integration
 * @param savedStateRegistryOwner Saved state registry owner for Compose
 * @param viewModelStoreOwner ViewModel store owner for Compose
 * @param mainViewModel ViewModel providing route data for visualization
 * @param isLocationPermissionGranted Lambda returning true if ACCESS_FINE_LOCATION is granted
 */
class MapSurfaceCallback(
    private val carContext: CarContext,
    private val lifecycleOwner: LifecycleOwner,
    private val savedStateRegistryOwner: SavedStateRegistryOwner,
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val mainViewModel: MainViewModel,
    private val isLocationPermissionGranted: () -> Boolean,
) : SurfaceCallback {
    private val presentationManager = ComposePresentationManager(carContext)

    private val initialCenter: GeoPoint =
        TomTomSdk.locationProvider.lastKnownLocation?.position ?: DEFAULT_POSITION

    private val statusBarHeight: Int by lazy { resolveStatusBarHeight() }
    private val navBarHeight: Int by lazy { resolveNavBarHeight() }

    private val cameraController = CameraController(initialCenter = initialCenter)

    private var isLocationProviderEnabled = false

    private var composeView: ComposeView? = null

    init {
        mainViewModel.setCameraController(cameraController)
    }

    private val navigationInfrastructure = MutableStateFlow(
        NavigationVisualizationInfrastructure(
            routingVisualizationDataProvider = flowOf(
                RoutingVisualizationDataProvider(
                    routes = mainViewModel.uiState.map { it.routes },
                    selectedRouteId = mainViewModel.uiState.map { it.selectedRoute?.id },
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

        presentationManager.destroy()

        if (!isLocationProviderEnabled && isLocationPermissionGranted()) {
            TomTomSdk.locationProvider.enable()
            isLocationProviderEnabled = true
        } else if (!isLocationPermissionGranted()) {
            Log.w(TAG, "Location permission not granted — skipping locationProvider.enable()")
        }

        val mapDisplayInfrastructure = createMapDisplayInfrastructure()
        composeView = createComposeView(mapDisplayInfrastructure).also {
            presentationManager.create(
                surfaceContainer = surfaceContainer,
                composeView = it,
            )
        }
    }

    override fun onSurfaceDestroyed(surfaceContainer: SurfaceContainer) {
        TomTomSdk.locationProvider.disable()
        isLocationProviderEnabled = false
        presentationManager.destroy()
    }

    override fun onScroll(
        distanceX: Float,
        distanceY: Float,
    ) {
        cameraController.setCameraTracking(CameraTrackingMode.None)
        cameraController.syncWithMapState()

        cameraController.currentCameraOptions.position?.let { currentCenter ->
            cameraController.currentCameraOptions.zoom?.let { currentZoom ->
                val newCenter = CameraCoordinateCalculator.computePannedCenter(
                    currentCenter = currentCenter,
                    currentZoom = currentZoom,
                    distanceX = distanceX,
                    distanceY = distanceY,
                )

                cameraController.moveCamera(CameraOptions(newCenter, currentZoom))
            }
        }
    }

    override fun onScale(
        focusX: Float,
        focusY: Float,
        scaleFactor: Float,
    ) {
        if (scaleFactor <= 0f) return

        cameraController.setCameraTracking(CameraTrackingMode.None)
        cameraController.syncWithMapState()

        val zoomDelta = CameraCoordinateCalculator.scaleFactorToZoomDelta(
            scaleFactor = scaleFactor,
            sensitivity = DEFAULT_GESTURE_SENSITIVITY,
        )

        if (zoomDelta.isFinite()) {
            cameraController.applyZoomDelta(zoomDelta, animate = true)
        }
    }

    override fun onClick(
        x: Float,
        y: Float,
    ) {
        val adjustedY = y + statusBarHeight
        val downTime = SystemClock.uptimeMillis()
        val motionEventDown = MotionEvent.obtain(
            downTime,
            downTime,
            MotionEvent.ACTION_DOWN,
            x,
            adjustedY,
            0,
        )
        composeView?.dispatchTouchEvent(motionEventDown)

        val motionEventUp = MotionEvent.obtain(
            downTime,
            SystemClock.uptimeMillis(),
            MotionEvent.ACTION_UP,
            x,
            adjustedY,
            0,
        )
        composeView?.dispatchTouchEvent(motionEventUp)

        motionEventDown.recycle()
        motionEventUp.recycle()
    }

    private fun SurfaceContainer.isValid(): Boolean = surface != null && width > 0 && height > 0

    private fun createMapDisplayInfrastructure(): MapDisplayInfrastructure =
        MapDisplayInfrastructure(sdkContext = TomTomSdk.sdkContext) {
            locationInfrastructure = MapLocationInfrastructure {
                locationProvider = TomTomSdk.locationProvider
            }
        }

    /** Animates the camera to a specific position. */
    fun animateTo(position: GeoPoint) {
        cameraController.animateCamera(cameraController.currentCameraOptions.deepCopy(position = position))
    }

    private fun createComposeView(mapDisplayInfrastructure: MapDisplayInfrastructure): ComposeView =
        ComposeView(carContext).apply {
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(savedStateRegistryOwner)
            setViewTreeViewModelStoreOwner(viewModelStoreOwner)

            setContent {
                MapScreen(
                    carContext = carContext,
                    mapDisplayInfrastructure = mapDisplayInfrastructure,
                    navigationInfrastructure = navigationInfrastructure,
                    initialCenter = initialCenter,
                    mainViewModel = mainViewModel,
                    modifier = Modifier.padding(
                        top = with(LocalDensity.current) { statusBarHeight.toDp() },
                        bottom = with(LocalDensity.current) { navBarHeight.toDp() },
                    ),
                    onMapViewStateReady = { state ->
                        cameraController.initialize(state, lifecycleOwner.lifecycleScope)
                        TomTomSdk.locationProvider.lastKnownLocation?.let { animateTo(it.position) }
                    },
                )
            }
        }

    private fun resolveStatusBarHeight(): Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        resolveDefaultDisplayInsets().top
    } else {
        resolveSystemDimensionPixelSize("status_bar_height")
    }

    private fun resolveNavBarHeight(): Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        resolveDefaultDisplayInsets().bottom
    } else {
        resolveSystemDimensionPixelSize("navigation_bar_height")
    }

    /**
     * Returns system bar insets from the default (real) display.
     *
     * [CarContext.getSystemService] is scoped to the car app's own
     * window context which lives on a [android.hardware.display.VirtualDisplay] — its
     * [WindowManager.currentWindowMetrics] reports zero insets because the virtual display has no
     * system bars. Using [CarContext.applicationContext] instead gives us a context bound to the
     * default display, so [WindowManager.currentWindowMetrics] returns the real insets.
     */
    @RequiresApi(Build.VERSION_CODES.R)
    private fun resolveDefaultDisplayInsets(): android.graphics.Insets {
        val windowManager = carContext.applicationContext.getSystemService(WindowManager::class.java)
        return windowManager.currentWindowMetrics.windowInsets
            .getInsetsIgnoringVisibility(
                WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars(),
            )
    }

    /**
     * Resolves the pixel size of a system dimension resource by its name.
     *
     * We use [android.content.res.Resources.getIdentifier] which is a discouraged API
     * because it's the only way to resolve system dimensions like "status_bar_height"
     * and "navigation_bar_height" on Android versions prior to [Build.VERSION_CODES.R]
     * when using a VirtualDisplay with Compose presentation.
     * Starting from [Build.VERSION_CODES.R], [resolveDefaultDisplayInsets] should be used instead.
     *
     * @param name The name of the system dimension resource to resolve.
     * @return The pixel size of the resolved dimension resource, or 0 if the resource is not found.
     */
    @SuppressLint("DiscouragedApi")
    private fun resolveSystemDimensionPixelSize(name: String): Int {
        val resourceId = carContext.resources.getIdentifier(name, "dimen", "android")
        return if (resourceId > 0) carContext.resources.getDimensionPixelSize(resourceId) else 0
    }

    private companion object {
        const val TAG = "MapSurfaceCallback"
    }
}
