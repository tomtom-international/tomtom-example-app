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

package com.example.onboarding

import androidx.annotation.StringRes
import com.example.R
import com.tomtom.sdk.telemetry.UserConsent

/**
 * Type-safe representation of the user's telemetry consent choice.
 * [storageKey] is the integer persisted in DataStore for backward compatibility.
 */
enum class TelemetryConsentLevel(
    @StringRes val labelRes: Int,
    val storageKey: Int,
) {
    OFF(R.string.onboarding_privacy_option_no_telemetry, 0),
    LOCATION_ONLY(R.string.onboarding_privacy_option_location_only, 1),
    ON(R.string.onboarding_privacy_option_full_telemetry, 2),
    ;

    fun toUserConsent(): UserConsent = when (this) {
        OFF -> UserConsent.TelemetryOff
        LOCATION_ONLY -> UserConsent.LocationOnly
        ON -> UserConsent.TelemetryOn
    }

    companion object {
        fun fromStorageKey(key: Int): TelemetryConsentLevel = values().firstOrNull { it.storageKey == key } ?: OFF
    }
}
