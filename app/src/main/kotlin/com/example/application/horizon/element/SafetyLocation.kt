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
        override val iconResource = R.drawable.tt_asset_icon_speedcamerastart_line_32
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
