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

package com.example.automotive.settings.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.example.automotive.settings.data.model.ConsentLevel
import com.example.automotive.settings.data.model.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Persists user settings using DataStore Preferences.
 */
class LocalSettingsRepository(private val localDataSource: DataStore<Preferences>) {
    val settings: Flow<Settings> = localDataSource.data.map { preferences ->
        Settings(
            consentLevel = preferences[TELEMETRY_CONSENT_LEVEL_KEY]?.let {
                ConsentLevel.fromStorageKey(it)
            },
        )
    }

    suspend fun updateConsentLevel(level: ConsentLevel) {
        localDataSource.edit { preferences ->
            preferences[TELEMETRY_CONSENT_LEVEL_KEY] = level.storageKey
        }
    }

    private companion object {
        val TELEMETRY_CONSENT_LEVEL_KEY = intPreferencesKey("telemetry_consent_level")
    }
}
