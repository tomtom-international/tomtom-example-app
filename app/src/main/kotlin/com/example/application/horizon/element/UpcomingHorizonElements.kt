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

package com.example.application.horizon.element

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.application.common.formatDistance
import com.tomtom.quantity.Distance

data class UpcomingHorizonElements(
    val trafficElement: Traffic? = null,
    val safetyLocationElement: SafetyLocation? = null,
) {
    sealed class HorizonElement(open val distance: Distance?) {
        @get:DrawableRes
        abstract val iconResource: Int

        @get:StringRes
        abstract val descriptionResource: Int

        @get:StringRes
        open val fallbackDelayResId: Int? = null

        open val delayMinutes: Long? = null

        fun formattedDistance(): String? = distance
            ?.takeIf { it.inMeters() >= 0 }
            ?.let { distance -> formatDistance(distance) }
    }
}
