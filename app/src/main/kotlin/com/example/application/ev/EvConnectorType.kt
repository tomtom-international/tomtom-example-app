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

package com.example.application.ev

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.R
import com.tomtom.sdk.vehicle.ConnectorType

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
            ConnectorType.Iec60309Ac3PhaseRed to Pair(
                R.drawable.outline_outlet_24px,
                R.string.demo_search_ev_label_connector_type_option_industrial_red,
            ),
            ConnectorType.Iec60309Ac1PhaseBlue to Pair(
                R.drawable.outline_outlet_24px,
                R.string.demo_search_ev_label_connector_type_option_industrial_blue,
            ),
            ConnectorType.Iec60309DcWhite to Pair(
                R.drawable.outline_outlet_24px,
                R.string.demo_search_ev_label_connector_type_option_industrial_white_dc,
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
        val Iec60309Ac3PhaseRed = fromType(ConnectorType.Iec60309Ac3PhaseRed)
        val Iec60309Ac1PhaseBlue = fromType(ConnectorType.Iec60309Ac1PhaseBlue)
        val Iec60309DcWhite = fromType(ConnectorType.Iec60309DcWhite)
        val Tesla = fromType(ConnectorType.Tesla)
    }
}
