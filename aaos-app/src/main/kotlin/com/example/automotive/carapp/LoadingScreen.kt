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

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.ScreenManager
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.Template
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.automotive.R
import com.example.automotive.settings.data.model.ConsentLevel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Initial loading screen for AAOS application.
 *
 * Displays a loading spinner while waiting for the consent state to be read from DataStore.
 * On [Lifecycle.Event.ON_CREATE], launches a coroutine that waits for [consentRequired] to emit
 * a non-null value, then navigates to [TelemetryConsentScreen] or [MainScreen] accordingly.
 *
 * @param carContext CarContext from the Car App Library
 * @param consentRequired StateFlow tracking consent state:
 *   null = DataStore read still in progress,
 *   true = consent required (first launch),
 *   false = consent already stored
 * @param onConsentSelected Callback invoked when the user selects a telemetry consent level
 * @param buildMainScreen Factory lambda that constructs the [MainScreen]
 */
class LoadingScreen(
    carContext: CarContext,
    private val consentRequired: StateFlow<Boolean?>,
    private val onConsentSelected: (ConsentLevel) -> Unit,
    private val buildMainScreen: () -> MainScreen,
) : Screen(carContext) {
    init {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                lifecycleScope.launch {
                    val screenManager = carContext.getCarService(ScreenManager::class.java)
                    val consentNeeded = consentRequired.filterNotNull().first()
                    if (consentNeeded) {
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
        })
    }

    override fun onGetTemplate(): Template = MessageTemplate.Builder(carContext.getString(R.string.sdk_initializing))
        .setLoading(true)
        .build()
}
