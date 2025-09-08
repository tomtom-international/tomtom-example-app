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
import com.example.application.horizon.element.UpcomingHorizonElements.HorizonElement
import com.tomtom.quantity.Distance
import com.tomtom.sdk.navigation.horizon.elements.traffic.TrafficElement

data class Traffic
    constructor(
        override val distance: Distance?,
        val element: TrafficElement?,
    ) : HorizonElement(distance) {
        override val iconResource: Int = R.drawable.tt_asset_icon_jam_fill_48

        override val descriptionResource: Int = if ((distance?.inMeters() ?: -1.0) >= 0.0) {
            R.string.horizon_label_traffic_card_description
        } else {
            R.string.horizon_label_traffic_card_description_without_distance
        }

        override val fallbackDelayResId: Int = R.string.horizon_label_traffic_delay_unknown

        override val delayMinutes: Long? = element?.trafficEvent?.delay?.inWholeMinutes
    }
