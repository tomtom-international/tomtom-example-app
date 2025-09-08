/*
 * © 2025 TomTom NV. All rights reserved.
 *
 * This software is the proprietary copyright of TomTom NV and its subsidiaries and may be
 * used for internal evaluation purposes or commercial use strictly subject to separate
 * license agreement between you and TomTom NV. If you are the licensee, you are only permitted
 * to use this software in accordance with the terms of your license agreement. If you are
 * not the licensee, you are not authorized to use this software in any manner and should
 * immediately return or destroy it.
 */

package com.example.application.settings.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.application.settings.data.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application, val settingsRepository: SettingsRepository) :
    AndroidViewModel(application) {
    val settingsUiState: StateFlow<SettingsUiState> =
        settingsRepository.settings.map {
            SettingsUiState.Enabled(
                navSdkLogLevel = it.navSdkLogLevel,
                isNavSdkTtpLogsEnabled = it.isNavSdkTtpLogsEnabled,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = SettingsUiState.Disabled,
        )

    fun updateNavSdkLogLevel(index: Int) {
        viewModelScope.launch {
            val option = index - 1 // Option "DISABLED" should be -1
            settingsRepository.updateNavSdkLogLevel(option)
        }
    }

    fun updateNavSdkTtpLogsEnabled(value: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateNavSdkTtpLogsEnabled(value)
        }
    }

    companion object {
        val APPLICATION = object : CreationExtras.Key<Application> {}
        val SETTINGS_REPOSITORY = object : CreationExtras.Key<SettingsRepository> {}

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SettingsViewModel(
                    application = this[APPLICATION] as Application,
                    settingsRepository = this[SETTINGS_REPOSITORY] as SettingsRepository,
                )
            }
        }
    }
}
