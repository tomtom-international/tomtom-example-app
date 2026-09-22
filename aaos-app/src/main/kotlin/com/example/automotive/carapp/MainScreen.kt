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
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.Template
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.automotive.R
import com.example.automotive.common.permissions.PermissionsManager
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * Main navigation screen for AAOS application.
 *
 * @param carContext CarContext from Android Automotive framework
 * @param mainViewModel ViewModel for route planning and screen state
 * @param permissionsManager Manager for runtime permissions
 */
class MainScreen(
    carContext: CarContext,
    private val mainViewModel: MainViewModel,
    private val permissionsManager: PermissionsManager = PermissionsManager(carContext),
) : Screen(carContext) {
    private val navigationTemplateBuilder = NavigationTemplateBuilder(carContext, mainViewModel)

    init {
        Log.d(TAG, "Initializing MainScreen")

        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                if (!mainViewModel.uiState.value.permissionsGranted) {
                    permissionsManager.checkAndRequestPermissions {
                        Log.d(TAG, "Permissions granted")
                        mainViewModel.setPermissionsGranted(true)
                        checkAndReportLocationEnabled()
                    }
                } else {
                    checkAndReportLocationEnabled()
                }
            }
        })

        lifecycleScope.launch {
            mainViewModel.uiState
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { invalidate() }
        }
    }

    override fun onGetTemplate(): Template {
        val state = mainViewModel.uiState.value
        return when {
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
                navigationTemplateBuilder.buildNavigationTemplate(state)
            }
        }
    }

    private fun checkAndReportLocationEnabled() {
        try {
            val lm = carContext.getSystemService("location") as? LocationManager
            lm?.let { mainViewModel.setLocationEnabled(it.isLocationEnabled) }
        } catch (e: SecurityException) {
            Log.w(TAG, "Could not check location enabled state: $e")
        }
    }

    private fun tryEnableLocationInApp() {
        val lm = carContext.getSystemService(LocationManager::class.java)
        if (lm?.isLocationEnabled == true) {
            mainViewModel.setLocationEnabled(true)
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
        carContext.getString(R.string.template_title_location_disabled),
    )
        .addAction(
            Action.Builder()
                .setTitle(carContext.getString(R.string.template_action_title_enable_location))
                .setOnClickListener { tryEnableLocationInApp() }
                .build(),
        )
        .build()

    private fun buildPermissionsRequiredTemplate(): Template = MessageTemplate.Builder(
        carContext.getString(R.string.template_title_permissions_required),
    )
        .addAction(
            Action.Builder()
                .setTitle(carContext.getString(R.string.template_action_title_grant_permissions))
                .setOnClickListener {
                    permissionsManager.checkAndRequestPermissions {
                        mainViewModel.setPermissionsGranted(true)
                    }
                }
                .build(),
        )
        .build()

    companion object {
        private const val TAG = "MainScreen"
    }
}
