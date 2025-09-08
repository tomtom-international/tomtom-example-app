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

import com.example.R
import com.tomtom.quantity.Distance
import com.tomtom.sdk.navigation.horizon.elements.safetylocation.SafetyLocationElement
import com.tomtom.sdk.safetylocations.model.SafetyLocationType

sealed class SafetyLocation(
    override val distance: Distance?,
    open val element: SafetyLocationElement,
) : UpcomingHorizonElements.HorizonElement(distance) {
    data class RedLightCamera(
        override val distance: Distance?,
        override val element: SafetyLocationElement,
    ) : SafetyLocation(distance, element) {
        override val iconResource: Int = R.drawable.traffic_light_24px
        override val descriptionResource: Int = R.string.horizon_label_safety_location_card_redlight_description
    }

    data class SpeedCamera(
        override val distance: Distance?,
        override val element: SafetyLocationElement,
    ) : SafetyLocation(distance, element) {
        override val descriptionResource = R.string.horizon_label_safety_location_card_speed_camera_description
        override val iconResource = R.drawable.speed_camera_24px
    }

    companion object {
        fun create(
            distance: Distance?,
            element: SafetyLocationElement?,
        ): SafetyLocation? = when (element?.safetyLocation?.type) {
            SafetyLocationType.Companion.RedLightCamera,
            SafetyLocationType.Companion.RedLightSpeedCamera,
            -> RedLightCamera(distance, element)

            SafetyLocationType.Companion.FixedSpeedCamera,
            SafetyLocationType.Companion.MobileSpeedCamera,
            -> SpeedCamera(distance, element)

            else -> null
        }
    }
}
