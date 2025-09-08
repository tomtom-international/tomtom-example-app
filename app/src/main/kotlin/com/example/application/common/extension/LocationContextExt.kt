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

package com.example.application.common.extension

import com.example.application.common.ISO3_GBR
import com.example.application.common.ISO3_USA
import com.tomtom.sdk.navigation.locationcontext.LocationContext

const val SPEED_LIMIT_VOID = "--"

fun LocationContext?.getSpeedData() = when {
    this?.address == null -> SpeedData(speed = "0", speedLimit = SPEED_LIMIT_VOID, isMph = null)

    shouldShowMphUnits() -> SpeedData(
        speed = speed.inWholeMilesPerHour().toString(),
        speedLimit = speedLimit?.speed?.inWholeMilesPerHour()?.takeIf { it > 0 }?.toString() ?: SPEED_LIMIT_VOID,
        isMph = true,
    )

    else -> SpeedData(
        speed = speed.inWholeKilometersPerHour().toString(),
        speedLimit = speedLimit?.speed?.inWholeKilometersPerHour()?.takeIf { it > 0 }?.toString() ?: SPEED_LIMIT_VOID,
        isMph = false,
    )
}

private fun LocationContext.shouldShowMphUnits() = address?.countryCodeIso3?.equals(ISO3_GBR) == true ||
    address?.countryCodeIso3?.equals(ISO3_USA) == true

data class SpeedData(val speed: String, val speedLimit: String, val isMph: Boolean?)
