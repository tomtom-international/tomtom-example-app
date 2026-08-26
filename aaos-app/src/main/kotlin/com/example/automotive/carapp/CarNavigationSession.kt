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

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.car.app.AppManager
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import com.example.automotive.map.MapSurfaceCallback
import com.example.automotive.map.RoutesViewModel
import com.example.automotive.settings.data.model.ConsentLevel
import com.example.automotive.vehicle.VehicleRepository
import com.tomtom.sdk.init.TomTomSdk
import com.tomtom.sdk.init.createRoutePlanner
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * AAOS Session managing navigation screen and ViewModels.
 *
 * @param sdkInitialized StateFlow indicating SDK initialization status
 * @param initializationError StateFlow containing SDK initialization error, null if no error
 * @param consentRequired StateFlow tracking consent state:
 *   null = DataStore read still in progress,
 *   true = consent required (first launch),
 *   false = consent already stored
 * @param onConsentSelected Callback invoked when the user selects a telemetry consent level
 */
class CarNavigationSession(
    val sdkInitialized: StateFlow<Boolean>,
    val initializationError: StateFlow<String?>,
    val consentRequired: StateFlow<Boolean?>,
    val onConsentSelected: (ConsentLevel) -> Unit,
) : Session(), SavedStateRegistryOwner, ViewModelStoreOwner, DefaultLifecycleObserver {
    private val appManager: AppManager by lazy { carContext.getCarService(AppManager::class.java) }
    private var surfaceCallbackRegistered = false
    private var mapSurfaceCallback: MapSurfaceCallback? = null

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val _viewModelStore = ViewModelStore()

    /**
     * View model for EV route planning.
     * The RoutePlanner instance is always injected via setRoutePlanner() after SDK initialization,
     * so it is always constructed with null and never relies on sdkInitialized.value at init time.
     */
    private val routesViewModel: RoutesViewModel by lazy {
        ViewModelProvider(
            this,
            viewModelFactory {
                initializer {
                    RoutesViewModel(
                        routePlanner = null,
                        vehicleRepository = VehicleRepository(carContext),
                        sdkInitialized = sdkInitialized,
                        initializationError = initializationError,
                    )
                }
            },
        )[RoutesViewModel::class.java]
    }

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override val viewModelStore: ViewModelStore
        get() = _viewModelStore

    init {
        savedStateRegistryController.performRestore(null)
        lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        if (!surfaceCallbackRegistered) {
            lifecycleScope.launch {
                // Wait until the SDK is truly initialized (first { it } skips any false emissions).
                sdkInitialized.first { it }
                if (!surfaceCallbackRegistered) {
                    Log.d(TAG, "SDK became ready, setting route planner and registering surface callback")
                    routesViewModel.setRoutePlanner(TomTomSdk.createRoutePlanner())
                    ensureSurfaceCallback(routesViewModel)
                }
            }
        }
    }

    override fun onCreateScreen(intent: Intent): Screen =
        LoadingScreen(carContext, consentRequired, onConsentSelected, ::buildMainScreen)

    private fun buildMainScreen(): MainScreen {
        if (sdkInitialized.value) {
            ensureSurfaceCallback(routesViewModel)
        }
        return MainScreen(
            carContext = carContext,
            routesViewModel = routesViewModel,
            onRecenter = { mapSurfaceCallback?.recenterToUserLocation() },
        )
    }

    private fun ensureSurfaceCallback(viewModel: RoutesViewModel) {
        if (!surfaceCallbackRegistered) {
            try {
                mapSurfaceCallback = MapSurfaceCallback(
                    context = carContext,
                    lifecycleOwner = this,
                    savedStateRegistryOwner = this,
                    viewModelStoreOwner = this,
                    routesViewModel = viewModel,
                    isLocationPermissionGranted = {
                        ContextCompat.checkSelfPermission(
                            carContext,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                        ) == PackageManager.PERMISSION_GRANTED
                    },
                )
                appManager.setSurfaceCallback(mapSurfaceCallback)
                surfaceCallbackRegistered = true
                Log.d(TAG, "SurfaceCallback registered")
            } catch (e: IllegalStateException) {
                Log.w(TAG, "Could not register SurfaceCallback, host not yet bound: $e")
            }
        }
    }

    private companion object {
        const val TAG = "CarNavigationSession"
    }
}
