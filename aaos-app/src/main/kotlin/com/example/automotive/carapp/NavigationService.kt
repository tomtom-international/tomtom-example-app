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
import androidx.car.app.CarAppService
import androidx.car.app.Session
import androidx.car.app.SessionInfo
import androidx.car.app.validation.HostValidator
import androidx.datastore.preferences.preferencesDataStore
import com.example.automotive.carapp.cluster.ClusterData
import com.example.automotive.carapp.cluster.ClusterNavigationSession
import com.example.automotive.common.HostValidatorFactory
import com.example.automotive.common.SdkInitializer
import com.example.automotive.settings.data.LocalSettingsRepository
import com.example.automotive.settings.data.model.ConsentLevel
import com.tomtom.sdk.init.TomTomSdk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.IOException

private val CarAppService.dataStore by preferencesDataStore(name = "user_preferences")

private const val UNKNOWN_ERROR = "Unknown error"

/** CarAppService managing SDK initialization and session creation. */
class NavigationService : CarAppService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val sdkInitialized = MutableStateFlow(false)

    private val initializationError = MutableStateFlow<String?>(null)

    /**
     * Tracks whether telemetry consent is needed.
     * null  = DataStore read still in progress (unknown)
     * true  = consent required (first launch)
     * false = consent already stored
     */
    private val consentRequired = MutableStateFlow<Boolean?>(null)

    private val clusterData = ClusterData(
        isActiveGuidance = MutableStateFlow(false),
        clusterRoutes = MutableStateFlow(emptyList()),
        clusterSelectedRoute = MutableStateFlow(null),
    )

    private val sdkInitializer by lazy { SdkInitializer(applicationContext) }

    private val settingsRepository by lazy { LocalSettingsRepository(dataStore) }

    fun onConsentSelected(level: ConsentLevel) {
        serviceScope.launch {
            settingsRepository.updateConsentLevel(level)
            consentRequired.value = false
            initializeSdkAsync()
        }
    }

    private suspend fun initializeSdkAsync() {
        if (TomTomSdk.isInitialized) {
            sdkInitialized.value = true
            return
        }
        try {
            sdkInitializer.initializeSdk {
                settingsRepository.settings.first().consentLevel?.toUserConsent()
                    ?: ConsentLevel.OFF.toUserConsent()
            }
            sdkInitialized.value = true
        } catch (error: IllegalStateException) {
            Log.e(TAG, "Failed to initialize SDK: ${error.message}", error)
            initializationError.value = error.message ?: UNKNOWN_ERROR
        } catch (error: SecurityException) {
            Log.e(TAG, "Failed to initialize SDK: ${error.message}", error)
            initializationError.value = error.message ?: UNKNOWN_ERROR
        } catch (error: IOException) {
            Log.e(TAG, "Failed to initialize SDK: ${error.message}", error)
            initializationError.value = error.message ?: UNKNOWN_ERROR
        }
    }

    override fun createHostValidator(): HostValidator {
        return HostValidatorFactory.create(applicationContext)
    }

    override fun onCreateSession(sessionInfo: SessionInfo): Session {
        return if (sessionInfo.displayType == SessionInfo.DISPLAY_TYPE_CLUSTER) {
            Log.d(TAG, "Creating ClusterNavigationSession")

            ClusterNavigationSession(clusterData = clusterData)
        } else {
            Log.d(TAG, "Creating CarNavigationSession")

            serviceScope.launch {
                val consentLevel = settingsRepository.settings.first().consentLevel
                if (consentLevel == null) {
                    consentRequired.value = true
                } else {
                    consentRequired.value = false
                    initializeSdkAsync()
                }
            }

            CarNavigationSession(
                sdkInitialized = sdkInitialized,
                initializationError = initializationError,
                consentRequired = consentRequired,
                onConsentSelected = ::onConsentSelected,
                clusterData = clusterData,
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private companion object {
        const val TAG = "NavigationService"
    }
}
