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
import androidx.car.app.navigation.NavigationManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import com.example.automotive.carapp.cluster.ClusterData
import com.example.automotive.map.MapSurfaceCallback
import com.example.automotive.settings.data.model.ConsentLevel
import com.example.automotive.vehicle.VehicleRepository
import com.tomtom.sdk.init.TomTomSdk
import com.tomtom.sdk.init.createReverseGeocoder
import com.tomtom.sdk.init.createRoutePlanner
import kotlinx.coroutines.flow.StateFlow

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
 * @param clusterData Container for cluster map state flows.
 */
class CarNavigationSession(
    private val sdkInitialized: StateFlow<Boolean>,
    private val initializationError: StateFlow<String?>,
    private val consentRequired: StateFlow<Boolean?>,
    private val onConsentSelected: (ConsentLevel) -> Unit,
    private val clusterData: ClusterData,
) : Session(), SavedStateRegistryOwner, ViewModelStoreOwner, DefaultLifecycleObserver {
    private val appManager: AppManager by lazy { carContext.getCarService(AppManager::class.java) }
    private var surfaceCallbackRegistered = false
    private var mapSurfaceCallback: MapSurfaceCallback? = null

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val _viewModelStore = ViewModelStore()

    /**
     * View model for EV route planning.
     * Created on first access from [buildMainScreen], which [LoadingScreen] only invokes after
     * [sdkInitialized] is true — so the [TomTomSdk] accessors are safe to call here.
     */
    private val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(
            this,
            viewModelFactory {
                initializer {
                    MainViewModel(
                        routePlanner = TomTomSdk.createRoutePlanner(),
                        reverseGeocoder = TomTomSdk.createReverseGeocoder(),
                        navigation = TomTomSdk.navigation,
                        locationProvider = TomTomSdk.locationProvider,
                        navigationManager = carContext.getCarService(NavigationManager::class.java),
                        vehicleRepository = VehicleRepository(carContext),
                        clusterData = clusterData,
                    )
                }
            },
        )[MainViewModel::class.java]
    }

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override val viewModelStore: ViewModelStore
        get() = _viewModelStore

    init {
        savedStateRegistryController.performRestore(null)
        lifecycle.addObserver(this)
    }

    override fun onCreateScreen(intent: Intent): Screen = LoadingScreen(
        carContext = carContext,
        consentRequired = consentRequired,
        onConsentSelected = onConsentSelected,
        sdkInitialized = sdkInitialized,
        initializationError = initializationError,
        buildMainScreen = ::buildMainScreen,
    )

    private fun buildMainScreen(): MainScreen {
        ensureSurfaceCallback(mainViewModel)
        return MainScreen(
            carContext = carContext,
            mainViewModel = mainViewModel,
        )
    }

    private fun ensureSurfaceCallback(viewModel: MainViewModel) {
        if (!surfaceCallbackRegistered) {
            try {
                mapSurfaceCallback = MapSurfaceCallback(
                    carContext = carContext,
                    lifecycleOwner = this,
                    savedStateRegistryOwner = this,
                    viewModelStoreOwner = this,
                    mainViewModel = viewModel,
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
