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

import com.example.application.settings.data.model.Settings
import com.tomtom.sdk.telemetry.UserConsent
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<Settings>

    suspend fun updateTelemetryConsentLevel(value: Int)
}

fun Int.toUserConsent(): UserConsent = when (this) {
    -1 -> UserConsent.TelemetryOff
    0 -> UserConsent.LocationOnly
    1 -> UserConsent.TelemetryOn
    else -> UserConsent.TelemetryOff
}
