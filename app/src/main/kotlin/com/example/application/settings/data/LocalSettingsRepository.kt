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

package com.example.application.settings.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.example.application.settings.data.model.Settings
import kotlinx.coroutines.flow.map

class LocalSettingsRepository(private val localDataSource: DataStore<Preferences>) :
    SettingsRepository {
    override val settings = localDataSource.data.map { preferences ->
        Settings(
            navSdkLogLevel = preferences[NAV_SDK_LOGS_ENABLED_KEY] ?: -1,
            isNavSdkTtpLogsEnabled = preferences[NAV_SDK_TTP_LOGS_ENABLED_KEY] ?: false,
        )
    }

    override suspend fun updateNavSdkLogLevel(value: Int) {
        localDataSource.edit { preferences ->
            preferences[NAV_SDK_LOGS_ENABLED_KEY] = value
        }
    }

    override suspend fun updateNavSdkTtpLogsEnabled(value: Boolean) {
        localDataSource.edit { preferences ->
            preferences[NAV_SDK_TTP_LOGS_ENABLED_KEY] = value
        }
    }

    private companion object {
        val NAV_SDK_LOGS_ENABLED_KEY = intPreferencesKey("nav_sdk_logs_enabled")
        val NAV_SDK_TTP_LOGS_ENABLED_KEY = booleanPreferencesKey("nav_sdk_ttp_logs_enabled")
    }
}
