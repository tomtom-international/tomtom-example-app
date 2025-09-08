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

package com.example.application.search

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.R
import com.example.application.ev.EvAccessType
import com.example.application.ev.EvConnectorType
import com.tomtom.quantity.Power
import com.tomtom.sdk.location.BetaLocationApi
import com.tomtom.sdk.location.poi.ev.AccessType
import com.tomtom.sdk.location.poi.ev.Status
import com.tomtom.sdk.vehicle.ConnectorType

enum class EvFilter {
    CHARGING_SPEED,
    CONNECTOR_TYPE,
    STATE,
    ACCESS_TYPE,
}

class EvFilterCategory(
    @DrawableRes val imageVector: Int,
    @StringRes val filterDescription: Int,
    val filterOptions: List<EvFilterOption>,
    val allowsMultipleOptions: Boolean,
) {
    fun isCategoryActive(activeFilters: Map<EvFilterCategory, Set<EvFilterOption>>) = activeFilters.containsKey(this)
}

sealed class EvFilterOption(
    @DrawableRes val imageVector: Int,
    @StringRes val filterOptionDescription: Int,
) {
    fun isOptionSelected(
        selectedFilterCategory: EvFilterCategory,
        activeFilters: Map<EvFilterCategory, Set<EvFilterOption>>,
    ) = activeFilters[selectedFilterCategory]?.contains(this) ?: false
}

class ChargingSpeedEvFilterOption(
    imageVector: Int,
    filterOptionDescription: Int,
    val minPower: Power?,
    val maxPower: Power?,
) : EvFilterOption(imageVector, filterOptionDescription)

class ConnectorTypeEvFilterOption(
    imageVector: Int,
    filterOptionDescription: Int,
    val connectorType: ConnectorType,
) : EvFilterOption(imageVector, filterOptionDescription)

class StatusEvFilterOption(
    imageVector: Int,
    filterOptionDescription: Int,
    val status: Status,
) : EvFilterOption(imageVector, filterOptionDescription)

@OptIn(BetaLocationApi::class)
class AccessTypeEvFilterOption(
    imageVector: Int,
    filterOptionDescription: Int,
    val accessType: AccessType,
) : EvFilterOption(imageVector, filterOptionDescription)

@OptIn(BetaLocationApi::class)
private fun EvAccessType.toFilterOption(): AccessTypeEvFilterOption = AccessTypeEvFilterOption(
    imageVector = imageVector,
    filterOptionDescription = accessTypeDescription,
    accessType = accessType,
)

private fun EvConnectorType.toFilterOption(): ConnectorTypeEvFilterOption = ConnectorTypeEvFilterOption(
    imageVector = imageVector,
    filterOptionDescription = connectorDescription,
    connectorType = connectorType,
)

@Suppress("detekt:MagicNumber")
val evFilterCategories: Map<EvFilter, EvFilterCategory> = hashMapOf(
    EvFilter.CHARGING_SPEED to EvFilterCategory(
        imageVector = R.drawable.bolt_24px,
        filterDescription = R.string.demo_search_ev_title_charging_speed_category,
        allowsMultipleOptions = false,
        filterOptions = listOf(
            ChargingSpeedEvFilterOption(
                imageVector = R.drawable.bolt_24px,
                filterOptionDescription = R.string.demo_search_ev_label_charging_speed_option_lt_11kw,
                minPower = null,
                maxPower = Power.kilowatts(11.0),
            ),
            ChargingSpeedEvFilterOption(
                imageVector = R.drawable.bolt_24px,
                filterOptionDescription = R.string.demo_search_ev_label_charging_speed_option_12_49kw,
                minPower = Power.kilowatts(12.0),
                maxPower = Power.kilowatts(49.0),
            ),
            ChargingSpeedEvFilterOption(
                imageVector = R.drawable.bolt_24px,
                filterOptionDescription = R.string.demo_search_ev_label_charging_speed_option_50kw_plus,
                minPower = Power.kilowatts(50.0),
                maxPower = null,
            ),
            ChargingSpeedEvFilterOption(
                imageVector = R.drawable.bolt_24px,
                filterOptionDescription = R.string.demo_search_ev_label_charging_speed_option_150kw_plus,
                minPower = Power.kilowatts(150.0),
                maxPower = null,
            ),
        ),
    ),
    EvFilter.CONNECTOR_TYPE to EvFilterCategory(
        imageVector = R.drawable.power_24px,
        filterDescription = R.string.demo_search_ev_title_connector_type_category,
        allowsMultipleOptions = true,
        filterOptions = listOf(
            EvConnectorType.StandardHouseholdCountrySpecific.toFilterOption(),
            EvConnectorType.Iec62196Type1.toFilterOption(),
            EvConnectorType.Iec62196Type1Ccs.toFilterOption(),
            EvConnectorType.Iec62196Type2CableAttached.toFilterOption(),
            EvConnectorType.Iec62196Type2Outlet.toFilterOption(),
            EvConnectorType.Iec62196Type2Ccs.toFilterOption(),
            EvConnectorType.Iec62196Type3.toFilterOption(),
            EvConnectorType.Chademo.toFilterOption(),
            EvConnectorType.Gbt20234Part2.toFilterOption(),
            EvConnectorType.Gbt20234Part3.toFilterOption(),
            EvConnectorType.Iec60309Ac3PhaseRed.toFilterOption(),
            EvConnectorType.Iec60309Ac1PhaseBlue.toFilterOption(),
            EvConnectorType.Iec60309DcWhite.toFilterOption(),
            EvConnectorType.Tesla.toFilterOption(),
        ),
    ),
    EvFilter.STATE to EvFilterCategory(
        imageVector = R.drawable.outline_check_circle_24px,
        filterDescription = R.string.demo_search_ev_title_state_category,
        allowsMultipleOptions = false,
        filterOptions = listOf(
            StatusEvFilterOption(
                imageVector = R.drawable.outline_check_circle_24px,
                filterOptionDescription = R.string.demo_search_ev_label_state_option_available,
                status = Status.Available,
            ),
            StatusEvFilterOption(
                imageVector = R.drawable.outline_block_24px,
                filterOptionDescription = R.string.demo_search_ev_label_state_option_occupied,
                status = Status.Occupied,
            ),
            StatusEvFilterOption(
                imageVector = R.drawable.outline_warning_24px,
                filterOptionDescription = R.string.demo_search_ev_label_state_option_out_of_service,
                status = Status.OutOfService,
            ),
            StatusEvFilterOption(
                imageVector = R.drawable.outline_bookmark_24px,
                filterOptionDescription = R.string.demo_search_ev_label_state_option_reserved,
                status = Status.Reserved,
            ),
        ),
    ),
    EvFilter.ACCESS_TYPE to EvFilterCategory(
        imageVector = R.drawable.outline_public_24px,
        filterDescription = R.string.demo_search_ev_title_access_type_category,
        allowsMultipleOptions = true,
        filterOptions = listOf(
            EvAccessType.Public.toFilterOption(),
            EvAccessType.Private.toFilterOption(),
            EvAccessType.Restricted.toFilterOption(),
            EvAccessType.Company.toFilterOption(),
        ),
    ),
)
