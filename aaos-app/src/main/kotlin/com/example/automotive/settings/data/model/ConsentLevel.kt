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

package com.example.automotive.settings.data.model

import com.tomtom.sdk.telemetry.UserConsent

/**
 * Type-safe representation of the user's telemetry consent choice.
 * [storageKey] is the integer persisted in DataStore for backward compatibility.
 */
enum class ConsentLevel(val storageKey: Int) {
    OFF(0),
    LOCATION_ONLY(1),
    ON(2),
    ;

    fun toUserConsent(): UserConsent = when (this) {
        OFF -> UserConsent.TelemetryOff
        LOCATION_ONLY -> UserConsent.LocationOnly
        ON -> UserConsent.TelemetryOn
    }

    companion object {
        fun fromStorageKey(key: Int): ConsentLevel = values().firstOrNull { it.storageKey == key } ?: OFF
    }
}
