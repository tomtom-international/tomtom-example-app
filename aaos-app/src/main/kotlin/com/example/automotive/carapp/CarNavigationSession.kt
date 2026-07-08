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

import android.content.Intent
import android.util.Log
import androidx.car.app.AppManager
import androidx.car.app.Screen
import androidx.car.app.ScreenManager
import androidx.car.app.Session
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.Template
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
import com.example.automotive.R
import com.example.automotive.map.MapSurfaceCallback
import com.example.automotive.map.RoutesViewModel
import com.example.automotive.settings.data.model.ConsentLevel
import com.example.automotive.vehicle.VehicleRepository
import com.tomtom.sdk.init.TomTomSdk
import com.tomtom.sdk.init.createRoutePlanner
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * AAOS Session managing navigation screen and ViewModels.
 *
 * @param sdkInitialized StateFlow indicating SDK initialization status
 * @param initializationError StateFlow containing SDK initialization error, null if no error
 * @param consentRequired StateFlow tracking consent state:
 *   null  = DataStore read still in progress,
 *   true  = consent required (first launch),
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

        lifecycleScope.launch {
            val required = consentRequired.filterNotNull().first()
            val screenManager = carContext.getCarService(ScreenManager::class.java)
            if (required) {
                screenManager.push(
                    TelemetryConsentScreen(carContext) { level ->
                        onConsentSelected(level)
                        screenManager.push(buildMainScreen())
                    },
                )
            } else {
                screenManager.push(buildMainScreen())
            }
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        if (surfaceCallbackRegistered) return
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

    override fun onCreateScreen(intent: Intent): Screen {
        return when (consentRequired.value) {
            null -> buildLoadingScreen()
            true -> TelemetryConsentScreen(carContext) { level ->
                onConsentSelected(level)
                carContext.getCarService(ScreenManager::class.java).push(buildMainScreen())
            }
            false -> buildMainScreen()
        }
    }

    private fun buildLoadingScreen(): Screen = object : Screen(carContext) {
        override fun onGetTemplate(): Template =
            MessageTemplate.Builder(carContext.getString(R.string.sdk_initializing))
                .setLoading(true)
                .build()
    }

    private fun buildMainScreen(): MainScreen {
        if (sdkInitialized.value) {
            ensureSurfaceCallback(routesViewModel)
        }
        return MainScreen(
            carContext = carContext,
            routesViewModel = routesViewModel,
        )
    }

    private fun ensureSurfaceCallback(viewModel: RoutesViewModel) {
        if (!surfaceCallbackRegistered) {
            appManager.setSurfaceCallback(
                MapSurfaceCallback(
                    context = carContext,
                    lifecycleOwner = this,
                    savedStateRegistryOwner = this,
                    viewModelStoreOwner = this,
                    routesViewModel = viewModel,
                ),
            )
            surfaceCallbackRegistered = true
            Log.d(TAG, "SurfaceCallback registered")
        }
    }

    private companion object {
        const val TAG = "CarNavigationSession"
    }
}
