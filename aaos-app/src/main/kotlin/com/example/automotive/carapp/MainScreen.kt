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

import android.content.ActivityNotFoundException
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import android.util.Log
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarIcon
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.Template
import androidx.car.app.navigation.model.NavigationTemplate
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
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
 * @param onRecenter Callback invoked when the user taps the recenter button
 */
class MainScreen(
    carContext: CarContext,
    private val routesViewModel: RoutesViewModel,
    private val permissionsManager: PermissionsManager = PermissionsManager(carContext),
    private val onRecenter: (() -> Unit)? = null,
) : Screen(carContext) {
    init {
        Log.d(TAG, "Initializing MainScreen")

        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                if (!routesViewModel.uiState.value.permissionsGranted) {
                    permissionsManager.checkAndRequestPermissions {
                        Log.d(TAG, "Permissions granted")
                        routesViewModel.setPermissionsGranted(true)
                        checkAndReportLocationEnabled()
                    }
                } else {
                    checkAndReportLocationEnabled()
                }
            }
        })

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

            !state.locationEnabled -> {
                Log.d(TAG, "Building location disabled template")
                buildLocationDisabledTemplate()
            }

            else -> {
                Log.d(
                    TAG,
                    "Building navigation template (loading=${state.isLoading}, hasRoutes=${state.routes.isNotEmpty()})",
                )
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
                        state.routes.isNotEmpty() -> carContext.getString(R.string.action_clear_route)
                        else -> carContext.getString(R.string.action_plan_route)
                    },
                )
                .setOnClickListener {
                    if (routesViewModel.uiState.value.routes.isNotEmpty()) {
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

    private fun buildRecenterButton(): Action = Action.Builder()
        .setIcon(
            CarIcon.Builder(
                IconCompat.createWithResource(
                    carContext,
                    R.drawable.tt_asset_icon_recenter_line_32,
                ),
            ).build(),
        )
        .setOnClickListener {
            Log.d(TAG, "User clicked: Recenter")
            onRecenter?.invoke()
        }
        .build()

    private fun buildMapActionStrip(): ActionStrip = ActionStrip.Builder()
        .addAction(Action.Builder(Action.PAN).build())
        .addAction(buildRecenterButton())
        .build()

    private fun checkAndReportLocationEnabled() {
        try {
            val lm = carContext.getSystemService("location") as? LocationManager
            lm?.let { routesViewModel.setLocationEnabled(it.isLocationEnabled) }
        } catch (e: SecurityException) {
            Log.w(TAG, "Could not check location enabled state: $e")
        }
    }

    private fun tryEnableLocationInApp() {
        val lm = carContext.getSystemService(LocationManager::class.java)
        if (lm?.isLocationEnabled == true) {
            routesViewModel.setLocationEnabled(true)
        } else {
            openLocationSettings()
        }
    }

    private fun openLocationSettings() {
        try {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            carContext.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Log.w(TAG, "Could not open location settings: $e")
        }
    }

    private fun buildLocationDisabledTemplate(): Template = MessageTemplate.Builder(
        carContext.getString(R.string.location_disabled_message),
    )
        .addAction(
            Action.Builder()
                .setTitle(carContext.getString(R.string.action_enable_location))
                .setOnClickListener { tryEnableLocationInApp() }
                .build(),
        )
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
