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

package com.example

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.application.settings.data.SettingsRepository
import com.example.application.settings.data.toUserConsent
import com.tomtom.sdk.init.TomTomSdk
import com.tomtom.sdk.telemetry.UserConsent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel that drives the home/startup flow for selecting the Navigation application or the demos.
 */
class MainViewModel
    constructor(
        private val settingsRepository: SettingsRepository,
        private val onTomTomSdkInitialize: suspend (suspend () -> UserConsent) -> Unit,
        private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(MainUiState())
        val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch(ioDispatcher) {
                if (settingsRepository.settings.first().telemetryConsentLevel == null) {
                    _uiState.update { it.copy(isTelemetryConsentDialogShown = true) }
                } else {
                    initializeTomTomSdk()
                }
            }
        }

        fun confirmTelemetryConsent(index: Int) {
            viewModelScope.launch(ioDispatcher) {
                _uiState.update { it.copy(isTelemetryConsentDialogShown = false) }
                settingsRepository.updateTelemetryConsentLevel(index)
                initializeTomTomSdk()
            }
        }

        private suspend fun initializeTomTomSdk() {
            if (!TomTomSdk.isInitialized) {
                onTomTomSdkInitialize(
                    suspend {
                        settingsRepository.settings.first().telemetryConsentLevel?.toUserConsent()
                            ?: UserConsent.TelemetryOff
                    },
                )
            }
        }

        companion object {
            const val TAG = "MainViewModel"

            val SETTINGS_REPOSITORY_KEY = object : CreationExtras.Key<SettingsRepository> {}

            val ON_TOMTOM_SDK_INITIALIZE_KEY =
                object : CreationExtras.Key<suspend (suspend () -> UserConsent) -> Unit> {}

            val Factory: ViewModelProvider.Factory = viewModelFactory {
                initializer {
                    MainViewModel(
                        settingsRepository = this[SETTINGS_REPOSITORY_KEY] as SettingsRepository,
                        onTomTomSdkInitialize = this[ON_TOMTOM_SDK_INITIALIZE_KEY] as suspend (
                            suspend () -> UserConsent,
                        ) -> Unit,
                    )
                }
            }
        }
    }

@Stable
data class MainUiState(
    val isTelemetryConsentDialogShown: Boolean = false,
)
