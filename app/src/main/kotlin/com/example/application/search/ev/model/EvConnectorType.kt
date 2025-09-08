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

package com.example.application.search.ev.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.R
import com.tomtom.sdk.vehicle.ConnectorType

/**
 * Wraps an EV connector type with a drawable icon and a localized description.
 */
class EvConnectorType private constructor(
    val connectorType: ConnectorType,
    @DrawableRes val imageVector: Int,
    @StringRes val connectorDescription: Int,
) {
    companion object {
        private val connectorProperties = hashMapOf(
            ConnectorType.StandardHouseholdCountrySpecific to Pair(
                R.drawable.outline_outlet_24px,
                R.string.demo_search_ev_label_connector_type_option_standard_household,
            ),
            ConnectorType.Iec62196Type1 to Pair(
                R.drawable.outline_outlet_24px,
                R.string.demo_search_ev_label_connector_type_option_type1,
            ),
            ConnectorType.Iec62196Type1Ccs to Pair(
                R.drawable.outline_outlet_24px,
                R.string.demo_search_ev_label_connector_type_option_type1_combo,
            ),
            ConnectorType.Iec62196Type2CableAttached to Pair(
                R.drawable.outline_outlet_24px,
                R.string.demo_search_ev_label_connector_type_option_type2_cable,
            ),
            ConnectorType.Iec62196Type2Outlet to Pair(
                R.drawable.outline_outlet_24px,
                R.string.demo_search_ev_label_connector_type_option_type2,
            ),
            ConnectorType.Iec62196Type2Ccs to Pair(
                R.drawable.outline_outlet_24px,
                R.string.demo_search_ev_label_connector_type_option_type2_combo,
            ),
            ConnectorType.Iec62196Type3 to Pair(
                R.drawable.outline_outlet_24px,
                R.string.demo_search_ev_label_connector_type_option_type3,
            ),
            ConnectorType.Chademo to Pair(
                R.drawable.outline_outlet_24px,
                R.string.demo_search_ev_label_connector_type_option_chademo,
            ),
            ConnectorType.Gbt20234Part2 to Pair(
                R.drawable.outline_outlet_24px,
                R.string.demo_search_ev_label_connector_type_option_china_part2,
            ),
            ConnectorType.Gbt20234Part3 to Pair(
                R.drawable.outline_outlet_24px,
                R.string.demo_search_ev_label_connector_type_option_china_part3,
            ),
            ConnectorType.Tesla to Pair(
                R.drawable.outline_outlet_24px,
                R.string.demo_search_ev_label_connector_type_option_tesla,
            ),
        )

        fun fromType(connectorType: ConnectorType): EvConnectorType {
            val (imageVector, connectorDescription) = connectorProperties[connectorType]
                ?: Pair(
                    R.drawable.outline_outlet_24px,
                    R.string.demo_search_ev_label_connector_type_option_unknown,
                )
            return EvConnectorType(connectorType, imageVector, connectorDescription)
        }

        val StandardHouseholdCountrySpecific = fromType(ConnectorType.StandardHouseholdCountrySpecific)
        val Iec62196Type1 = fromType(ConnectorType.Iec62196Type1)
        val Iec62196Type1Ccs = fromType(ConnectorType.Iec62196Type1Ccs)
        val Iec62196Type2CableAttached = fromType(ConnectorType.Iec62196Type2CableAttached)
        val Iec62196Type2Outlet = fromType(ConnectorType.Iec62196Type2Outlet)
        val Iec62196Type2Ccs = fromType(ConnectorType.Iec62196Type2Ccs)
        val Iec62196Type3 = fromType(ConnectorType.Iec62196Type3)
        val Chademo = fromType(ConnectorType.Chademo)
        val Gbt20234Part2 = fromType(ConnectorType.Gbt20234Part2)
        val Gbt20234Part3 = fromType(ConnectorType.Gbt20234Part3)
        val Tesla = fromType(ConnectorType.Tesla)
    }
}
