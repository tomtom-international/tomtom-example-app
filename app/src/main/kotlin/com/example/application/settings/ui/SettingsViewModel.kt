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

package com.example.application.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.application.settings.data.SettingsRepository
import com.example.application.settings.data.toUserConsent
import com.tomtom.sdk.telemetry.Telemetry
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the Settings screen.
 * Exposes settings state and handles user actions to update preferences and telemetry/logging.
 */
class SettingsViewModel(
    val settingsRepository: SettingsRepository,
    val onRestartApp: () -> Unit,
) : ViewModel() {
    val settingsUiState: StateFlow<SettingsUiState> =
        settingsRepository.settings.map {
            SettingsUiState.Enabled(
                telemetryConsentLevel = it.telemetryConsentLevel,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = SettingsUiState.Disabled,
        )

    fun updateTelemetryConsentLevel(index: Int) {
        viewModelScope.launch {
            settingsRepository.updateTelemetryConsentLevel(index - 1)
            Telemetry.setConsent((index - 1).toUserConsent())
        }
    }

    companion object {
        val SETTINGS_REPOSITORY = object : CreationExtras.Key<SettingsRepository> {}
        val ON_RESTART_APP = object : CreationExtras.Key<() -> Unit> {}

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SettingsViewModel(
                    settingsRepository = this[SETTINGS_REPOSITORY] as SettingsRepository,
                    onRestartApp = this[ON_RESTART_APP] as () -> Unit,
                )
            }
        }
    }
}
