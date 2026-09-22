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
import com.tomtom.sdk.traffic.common.Category

sealed class Traffic(
    override val distance: Distance?,
    open val element: TrafficElement,
) : HorizonElement(distance) {
    override val delayMinutes: Long? get() = element.trafficEvent.delay?.inWholeMinutes

    data class Jam(
        override val distance: Distance?,
        override val element: TrafficElement,
    ) : Traffic(distance, element) {
        override val iconResource: Int = R.drawable.tt_asset_icon_jam_fill_48
        override val descriptionResource: Int = if ((distance?.inMeters() ?: -1.0) >= 0.0) {
            R.string.horizon_label_traffic_card_description
        } else {
            R.string.horizon_label_traffic_card_in_jam_description
        }
    }

    data class RoadClosure(
        override val distance: Distance?,
        override val element: TrafficElement,
    ) : Traffic(distance, element) {
        override val iconResource: Int = R.drawable.tt_asset_icon_cone_line_32
        override val descriptionResource: Int = R.string.horizon_label_traffic_card_road_closure_description
    }

    data class Accident(
        override val distance: Distance?,
        override val element: TrafficElement,
    ) : Traffic(distance, element) {
        override val iconResource: Int = R.drawable.tt_asset_icon_accident_line_32
        override val descriptionResource: Int = R.string.horizon_label_traffic_card_accident_description
    }

    data class RoadWorks(
        override val distance: Distance?,
        override val element: TrafficElement,
    ) : Traffic(distance, element) {
        override val iconResource: Int = R.drawable.tt_asset_icon_workadded_line_32
        override val descriptionResource: Int = R.string.horizon_label_traffic_card_road_works_description
    }

    data class LaneClosed(
        override val distance: Distance?,
        override val element: TrafficElement,
    ) : Traffic(distance, element) {
        override val iconResource: Int = R.drawable.tt_asset_icon_cone_line_32
        override val descriptionResource: Int = R.string.horizon_label_traffic_card_lane_closed_description
    }

    data class Generic(
        override val distance: Distance?,
        override val element: TrafficElement,
    ) : Traffic(distance, element) {
        override val iconResource: Int = R.drawable.tt_asset_icon_jam_fill_48
        override val descriptionResource: Int = R.string.horizon_label_traffic_card_description
    }

    companion object {
        fun create(
            distance: Distance?,
            element: TrafficElement?,
        ): Traffic? {
            element ?: return null
            return when (element.trafficEvent.category) {
                Category.Jam -> Jam(distance, element)
                Category.RoadClosure -> RoadClosure(distance, element)
                Category.Accident -> Accident(distance, element)
                Category.RoadWorks -> RoadWorks(distance, element)
                Category.LaneClosed -> LaneClosed(distance, element)
                else -> Generic(distance, element)
            }
        }
    }
}
