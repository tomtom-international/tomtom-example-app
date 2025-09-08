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

import android.util.Log
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.Template
import androidx.car.app.navigation.model.NavigationTemplate
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.automotive.R
import com.example.automotive.common.permissions.PermissionsManager
import com.example.automotive.map.RoutesViewModel
import kotlinx.coroutines.launch

/**
 * Main navigation screen for the AAOS application.
 * Displays the navigation template with route planning controls and manages permissions.
 *
 * @param carContext The CarContext from Android Automotive framework
 * @param routesViewModel ViewModel managing route planning state and operations
 * @param permissionsManager Manager for runtime permission requests (injectable for testing)
 */
class MainScreen(
    carContext: CarContext,
    private val routesViewModel: RoutesViewModel,
    private val permissionsManager: PermissionsManager = PermissionsManager(carContext),
) : Screen(carContext), DefaultLifecycleObserver {
    private var permissionsGranted = false

    init {
        lifecycle.addObserver(this)
        Log.d(TAG, "MainScreen initialized")
        permissionsManager.checkAndRequestPermissions {
            Log.d(TAG, "Permissions granted")
            permissionsGranted = true
            invalidate() // Refresh UI to enable buttons
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        Log.d(TAG, "Screen started, collecting ViewModel state")
        lifecycleScope.launch {
            owner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect combined UI state for efficient updates
                routesViewModel.uiState.collect { state ->
                    Log.d(TAG, "UI state updated: loading=${state.isLoading}, hasRoutes=${state.hasRoutes}")
                    invalidate()
                }
            }
        }
    }

    override fun onGetTemplate(): Template {
        if (!permissionsGranted) {
            Log.d(TAG, "Building permissions required template")
            return buildPermissionsRequiredTemplate()
        }

        val state = routesViewModel.uiState.value
        Log.d(TAG, "Building navigation template (loading=${state.isLoading}, hasRoutes=${state.hasRoutes})")
        val builder = NavigationTemplate.Builder()
        builder.setActionStrip(buildActionStrip())
        builder.setMapActionStrip(buildMapActionStrip())
        return builder.build()
    }

    private fun buildActionStrip(): ActionStrip {
        // Capture state atomically to avoid race conditions
        val state = routesViewModel.uiState.value

        return ActionStrip.Builder()
            .addAction(
                Action.Builder()
                    .setTitle(
                        when {
                            state.isLoading -> carContext.getString(R.string.action_plan_route_loading)
                            state.hasRoutes -> carContext.getString(R.string.action_clear_route)
                            else -> carContext.getString(R.string.action_plan_route)
                        },
                    )
                    .setOnClickListener {
                        if (state.hasRoutes) {
                            Log.d(TAG, "User clicked: Clear routes")
                            routesViewModel.clearRoutes()
                        } else {
                            Log.d(TAG, "User clicked: Plan route")
                            routesViewModel.planSampleEvRoute()
                        }
                    }
                    .setEnabled(!state.isLoading)
                    .build(),
            )
            .build()
    }

    private fun buildMapActionStrip(): ActionStrip {
        return ActionStrip.Builder()
            .addAction(Action.Builder(Action.PAN).build())
            .build()
    }

    private fun buildPermissionsRequiredTemplate(): Template {
        return MessageTemplate.Builder(
            carContext.getString(R.string.permissions_required_message),
        )
            .addAction(
                Action.Builder()
                    .setTitle(carContext.getString(R.string.action_grant_permissions))
                    .setOnClickListener {
                        permissionsManager.checkAndRequestPermissions {
                            permissionsGranted = true
                            invalidate()
                        }
                    }
                    .build(),
            )
            .build()
    }

    companion object {
        private const val TAG = "MainScreen"
    }
}
