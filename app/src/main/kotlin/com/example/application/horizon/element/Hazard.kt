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

import android.util.Log
import com.example.R
import com.tomtom.quantity.Distance
import com.tomtom.sdk.hazards.model.HazardType
import com.tomtom.sdk.navigation.horizon.elements.hazard.HazardElement

sealed class Hazard(
    override val distance: Distance?,
    open val element: HazardElement,
) : UpcomingHorizonElements.HorizonElement(distance) {
    data class Generic(
        override val distance: Distance?,
        override val element: HazardElement,
    ) : Hazard(distance, element) {
        override val iconResource: Int = R.drawable.tt_asset_icon_warning_32
        override val descriptionResource: Int = R.string.horizon_label_hazard_card_generic_description
    }

    data class BadRoadConditions(
        override val distance: Distance?,
        override val element: HazardElement,
    ) : Hazard(distance, element) {
        override val iconResource: Int = R.drawable.tt_asset_icon_badroad_line_32
        override val descriptionResource: Int = R.string.horizon_label_hazard_card_bad_road_description
    }

    data class Accident(
        override val distance: Distance?,
        override val element: HazardElement,
    ) : Hazard(distance, element) {
        override val iconResource: Int = R.drawable.tt_asset_icon_accident_line_32
        override val descriptionResource: Int = R.string.horizon_label_hazard_card_accident_description
    }

    data class SlipperyRoad(
        override val distance: Distance?,
        override val element: HazardElement,
    ) : Hazard(distance, element) {
        override val iconResource: Int = R.drawable.tt_asset_icon_slipperyroad_line_32
        override val descriptionResource: Int = R.string.horizon_label_hazard_card_slippery_road_description
    }

    data class ReducedVisibility(
        override val distance: Distance?,
        override val element: HazardElement,
    ) : Hazard(distance, element) {
        override val iconResource: Int = R.drawable.tt_asset_icon_fog_line_32
        override val descriptionResource: Int = R.string.horizon_label_hazard_card_reduced_visibility_description
    }

    data class BrokenDownVehicle(
        override val distance: Distance?,
        override val element: HazardElement,
    ) : Hazard(distance, element) {
        override val iconResource: Int = R.drawable.tt_asset_icon_breakdown_fill_32
        override val descriptionResource: Int = R.string.horizon_label_hazard_card_broken_down_vehicle_description
    }

    data class ObjectsOnRoad(
        override val distance: Distance?,
        override val element: HazardElement,
    ) : Hazard(distance, element) {
        override val iconResource: Int = R.drawable.tt_asset_icon_cone_line_32
        override val descriptionResource: Int = R.string.horizon_label_hazard_card_objects_on_road_description
    }

    data class WrongWayDriver(
        override val distance: Distance?,
        override val element: HazardElement,
    ) : Hazard(distance, element) {
        override val iconResource: Int = R.drawable.tt_asset_icon_wrong_way_driver_32
        override val descriptionResource: Int = R.string.horizon_label_hazard_card_wrong_way_driver_description
    }

    data class StrongWind(
        override val distance: Distance?,
        override val element: HazardElement,
    ) : Hazard(distance, element) {
        override val iconResource: Int = R.drawable.tt_asset_icon_wind_line_32
        override val descriptionResource: Int = R.string.horizon_label_hazard_card_strong_wind_description
    }

    companion object {
        fun create(
            distance: Distance?,
            element: HazardElement?,
        ): Hazard? {
            Log.d("Hazard.create", "Creating hazard with type: ${element?.hazard?.type}")
            return when (element?.hazard?.type) {
                HazardType.Companion.Generic -> Generic(distance, element)

                HazardType.Companion.BadRoadConditions -> BadRoadConditions(distance, element)

                HazardType.Companion.Accident -> Accident(distance, element)

                HazardType.Companion.SlipperyRoad -> SlipperyRoad(distance, element)

                HazardType.Companion.ReducedVisibility -> ReducedVisibility(distance, element)

                HazardType.Companion.BrokenDownVehicle -> BrokenDownVehicle(distance, element)

                HazardType.Companion.ObjectsOnRoad -> ObjectsOnRoad(distance, element)

                HazardType.Companion.WrongWayDriver -> WrongWayDriver(distance, element)

                HazardType.Companion.StrongWind -> StrongWind(distance, element)

                else -> element?.let { Generic(distance, it) }
            }
        }
    }
}
