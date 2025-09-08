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

package com.example.application.extension

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
