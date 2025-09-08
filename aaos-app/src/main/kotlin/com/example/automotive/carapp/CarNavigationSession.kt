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
import androidx.car.app.Session
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import com.example.automotive.map.MapSurfaceCallback
import com.example.automotive.map.RoutesViewModel
import com.example.automotive.vehicle.VehicleRepository

/**
 * AAOS Session that manages the navigation screen, map surface, and ViewModels.
 * Implements lifecycle owners to support Compose integration in the map presentation.
 */
class CarNavigationSession : Session(), SavedStateRegistryOwner, ViewModelStoreOwner {
    private val appManager: AppManager by lazy { carContext.getCarService(AppManager::class.java) }
    private var surfaceCallbackRegistered = false

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val _viewModelStore = ViewModelStore()

    private val routesViewModel: RoutesViewModel by lazy {
        val factory = viewModelFactory {
            initializer {
                RoutesViewModel(vehicleRepository = VehicleRepository(carContext))
            }
        }
        ViewModelProvider(this, factory)[RoutesViewModel::class.java]
    }

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override val viewModelStore: ViewModelStore
        get() = _viewModelStore

    init {
        savedStateRegistryController.performRestore(null)
    }

    override fun onCreateScreen(intent: Intent): Screen {
        ensureSurfaceCallback()
        return MainScreen(carContext, routesViewModel)
    }

    private fun ensureSurfaceCallback() {
        if (!surfaceCallbackRegistered) {
            appManager.setSurfaceCallback(
                MapSurfaceCallback(
                    context = carContext,
                    lifecycleOwner = this,
                    savedStateRegistryOwner = this,
                    viewModelStoreOwner = this,
                    routesViewModel = routesViewModel,
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
