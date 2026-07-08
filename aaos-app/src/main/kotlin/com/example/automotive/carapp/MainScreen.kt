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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.automotive.R
import com.example.automotive.common.permissions.PermissionsManager
import com.example.automotive.map.RoutesViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * Main navigation screen for AAOS application.
 *
 * @param carContext CarContext from Android Automotive framework
 * @param routesViewModel ViewModel for route planning and screen state
 * @param permissionsManager Manager for runtime permissions
 */
class MainScreen(
    carContext: CarContext,
    private val routesViewModel: RoutesViewModel,
    private val permissionsManager: PermissionsManager = PermissionsManager(carContext),
) : Screen(carContext) {
    init {
        Log.d(TAG, "Initializing MainScreen")
        permissionsManager.checkAndRequestPermissions {
            Log.d(TAG, "Permissions granted")
            routesViewModel.setPermissionsGranted(true)
        }
        lifecycleScope.launch {
            routesViewModel.uiState
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { invalidate() }
        }
    }

    override fun onGetTemplate(): Template {
        val state = routesViewModel.uiState.value
        return when {
            state.initializationError != null -> {
                Log.e(TAG, "Building SDK initialization error template")
                buildSdkErrorTemplate(state.initializationError)
            }

            !state.sdkInitialized -> {
                Log.d(TAG, "Building SDK loading template")
                buildSdkLoadingTemplate()
            }

            !state.permissionsGranted -> {
                Log.d(TAG, "Building permissions required template")
                buildPermissionsRequiredTemplate()
            }

            else -> {
                Log.d(TAG, "Building navigation template (loading=${state.isLoading}, hasRoutes=${state.hasRoutes})")
                buildNavigationTemplate(state)
            }
        }
    }

    private fun buildNavigationTemplate(state: MainScreenUIState): Template = NavigationTemplate.Builder()
        .setActionStrip(buildActionStrip(state))
        .setMapActionStrip(buildMapActionStrip())
        .build()

    private fun buildActionStrip(state: MainScreenUIState): ActionStrip = ActionStrip.Builder()
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
                    if (routesViewModel.uiState.value.hasRoutes) {
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

    private fun buildMapActionStrip(): ActionStrip = ActionStrip.Builder()
        .addAction(Action.Builder(Action.PAN).build())
        .build()

    private fun buildSdkLoadingTemplate(): Template =
        MessageTemplate.Builder(carContext.getString(R.string.sdk_initializing))
            .setLoading(true)
            .build()

    private fun buildSdkErrorTemplate(errorMessage: String): Template =
        MessageTemplate.Builder(carContext.getString(R.string.sdk_initialization_error, errorMessage))
            .build()

    private fun buildPermissionsRequiredTemplate(): Template = MessageTemplate.Builder(
        carContext.getString(R.string.permissions_required_message),
    )
        .addAction(
            Action.Builder()
                .setTitle(carContext.getString(R.string.action_grant_permissions))
                .setOnClickListener {
                    permissionsManager.checkAndRequestPermissions {
                        routesViewModel.setPermissionsGranted(true)
                    }
                }
                .build(),
        )
        .build()

    companion object {
        private const val TAG = "MainScreen"
    }
}
