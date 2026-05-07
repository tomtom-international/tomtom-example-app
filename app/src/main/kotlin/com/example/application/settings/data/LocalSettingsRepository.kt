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

package com.example.application.settings.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.example.application.settings.data.model.Settings
import kotlinx.coroutines.flow.map

class LocalSettingsRepository(private val localDataSource: DataStore<Preferences>) :
    SettingsRepository {
    override val settings = localDataSource.data.map { preferences ->
        Settings(
            telemetryConsentLevel = preferences[TELEMETRY_CONSENT_LEVEL_KEY],
        )
    }

    override suspend fun updateTelemetryConsentLevel(value: Int) {
        localDataSource.edit { preferences ->
            preferences[TELEMETRY_CONSENT_LEVEL_KEY] = value
        }
    }

    private companion object {
        val TELEMETRY_CONSENT_LEVEL_KEY = intPreferencesKey("telemetry_consent_level")
    }
}
