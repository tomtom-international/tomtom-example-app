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

import android.util.Log
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.application.common.extension.onFailureIgnoreCancellation
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
import java.io.File

/**
 * ViewModel that drives the home/startup flow for selecting the Navigation application or the demos.
 */
class MainViewModel(
    private val settingsRepository: SettingsRepository,
    private val appFilesDir: File,
    private val onTomTomSdkInitialize: suspend (DeploymentMode, suspend () -> UserConsent) -> Unit,
    private val onExtractMapAssets: () -> Unit,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val onboardMapPath by lazy { appFilesDir.resolve(RELATIVE_ONBOARD_MAP_PATH) }
    val ndsKeyStorePath by lazy { onboardMapPath.resolve(RELATIVE_KEYSTORE_PATH) }

    val mapDir by lazy {
        val defaultMapDir = onboardMapPath.resolve(RELATIVE_NDS_STORE_PATH)
        val alternativeMapDir = onboardMapPath.resolve(ALTERNATIVE_RELATIVE_NDS_STORE_PATH)
        if (!defaultMapDir.isValidMapDir() && alternativeMapDir.isValidMapDir()) {
            Log.i(TAG, "Using alternative path as map dir, since that is valid.")
            alternativeMapDir
        } else {
            defaultMapDir
        }
    }

    init {
        viewModelScope.launch(ioDispatcher) {
            val persistedLevel = settingsRepository.settings.first().telemetryConsentLevel
            _uiState.update { it.copy(persistedConsentLevel = persistedLevel) }

            if (!regionStoreExists()) {
                Log.d(TAG, "Map doesn't exist, starting extraction")
                runCatching {
                    onExtractMapAssets()
                }.onFailureIgnoreCancellation {
                    Log.e(TAG, "Unable to install the offline map: ", it)
                    _uiState.update { it.copy(mapExtractionState = MapExtractionState.Failed) }
                    return@launch
                }.onSuccess {
                    Log.d(TAG, "Map installed successfully")
                }
            } else {
                Log.d(TAG, "Map already exists, skipping installation")
            }
            _uiState.update { it.copy(mapExtractionState = MapExtractionState.Completed) }
        }
    }

    fun onDeploymentModeSelected(type: DeploymentMode) {
        if (_uiState.value.sdkInitializationState != SdkInitializationState.NotStarted) {
            return
        }

        _uiState.update { it.copy(sdkInitializationState = SdkInitializationState.InProgress) }
        viewModelScope.launch(ioDispatcher) {
            runCatching {
                initializeTomTomSdk(type)
            }.onFailureIgnoreCancellation {
                Log.e(TAG, "SDK initialization failed: ", it)
                _uiState.update { it.copy(sdkInitializationState = SdkInitializationState.Failed) }
                return@launch
            }
            _uiState.update { it.copy(sdkInitializationState = SdkInitializationState.Completed) }
        }
    }

    fun onConsentConfirmed(telemetryConsentIndex: Int) {
        if (_uiState.value.persistedConsentLevel != null) return

        _uiState.update { it.copy(persistedConsentLevel = telemetryConsentIndex) }
        viewModelScope.launch(ioDispatcher) {
            settingsRepository.updateTelemetryConsentLevel(telemetryConsentIndex)
        }
    }

    private suspend fun initializeTomTomSdk(deploymentMode: DeploymentMode) {
        val consentLevel = checkNotNull(_uiState.value.persistedConsentLevel) {
            "Telemetry consent must be confirmed before SDK initialization."
        }

        if (!TomTomSdk.isInitialized) {
            onTomTomSdkInitialize(deploymentMode) { consentLevel.toUserConsent() }
        }
    }

    private fun regionStoreExists() = mapDir.listFiles()?.isNotEmpty() ?: false

    private fun File.isValidMapDir() = this.isDirectory && this.resolve(RELATIVE_ROOT_NDS_PATH).isFile

    companion object {
        const val TAG = "MainViewModel"

        const val MAP_ASSET_PATH = "sample-map/map.zip"
        const val KEYSTORE_ASSET_PATH = "sample-map/keystore.zip"
        private const val RELATIVE_ONBOARD_MAP_PATH = "offline"
        private const val RELATIVE_NDS_STORE_PATH = "DATA"
        private const val ALTERNATIVE_RELATIVE_NDS_STORE_PATH = "map"
        private const val RELATIVE_ROOT_NDS_PATH = "ROOT.NDS"
        private const val RELATIVE_KEYSTORE_PATH = "keystore.sqlite"

        val SETTINGS_REPOSITORY_KEY = object : CreationExtras.Key<SettingsRepository> {}

        val APP_FILES_DIR_KEY = object : CreationExtras.Key<File> {}

        val ON_TOMTOM_SDK_INITIALIZE_KEY =
            object : CreationExtras.Key<suspend (DeploymentMode, suspend () -> UserConsent) -> Unit> {}
        val ON_EXTRACT_MAP_ASSETS_KEY = object : CreationExtras.Key<() -> Unit> {}

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MainViewModel(
                    settingsRepository = this[SETTINGS_REPOSITORY_KEY] as SettingsRepository,
                    appFilesDir = this[APP_FILES_DIR_KEY] as File,
                    onTomTomSdkInitialize = this[ON_TOMTOM_SDK_INITIALIZE_KEY] as suspend (
                        DeploymentMode,
                        suspend () -> UserConsent,
                    ) -> Unit,
                    onExtractMapAssets = this[ON_EXTRACT_MAP_ASSETS_KEY] as () -> Unit,
                )
            }
        }
    }
}

enum class DeploymentMode {
    ONLINE_ONLY,
    ONLINE_FIRST,
}

enum class MapExtractionState {
    InProgress,
    Completed,
    Failed,
}

enum class SdkInitializationState {
    NotStarted,
    InProgress,
    Completed,
    Failed,
}

@Stable
data class MainUiState(
    val mapExtractionState: MapExtractionState = MapExtractionState.InProgress,
    val sdkInitializationState: SdkInitializationState = SdkInitializationState.NotStarted,
    val persistedConsentLevel: Int? = null,
)
