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

package com.example.application.search.ev

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.R
import com.example.application.search.ev.model.EvAccessType
import com.example.application.search.ev.model.EvConnectorType
import com.tomtom.quantity.Power
import com.tomtom.sdk.location.poi.ev.AccessType
import com.tomtom.sdk.location.poi.ev.Status
import com.tomtom.sdk.vehicle.ConnectorType

/** Supported EV search filter categories. */
enum class EvFilter {
    CHARGING_SPEED,
    CONNECTOR_TYPE,
    STATE,
    ACCESS_TYPE,
}

/**
 * Describes a filter category in the EV search UI.
 *
 * @param imageVector drawable resource for the category icon.
 * @param filterDescription string resource for the category label/description.
 * @param filterOptions list of available options within this category.
 * @param allowsMultipleOptions whether multiple options can be selected simultaneously.
 */
class EvFilterCategory(
    @DrawableRes val imageVector: Int,
    @StringRes val filterDescription: Int,
    val filterOptions: List<EvFilterOption>,
    val allowsMultipleOptions: Boolean,
) {
    fun isCategoryActive(activeFilters: Map<EvFilterCategory, Set<EvFilterOption>>) = activeFilters.containsKey(this)
}

/**
 * Base type for an EV filter option displayed under a category.
 *
 * @param imageVector drawable resource for the option icon.
 * @param filterOptionDescription string resource describing the option to the user.
 */
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

class AccessTypeEvFilterOption(
    imageVector: Int,
    filterOptionDescription: Int,
    val accessType: AccessType,
) : EvFilterOption(imageVector, filterOptionDescription)

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
        imageVector = R.drawable.tt_asset_icon_chargeslow_fill_32,
        filterDescription = R.string.demo_search_ev_title_charging_speed_category,
        allowsMultipleOptions = false,
        filterOptions = listOf(
            ChargingSpeedEvFilterOption(
                imageVector = R.drawable.tt_asset_icon_chargeslow_fill_32,
                filterOptionDescription = R.string.demo_search_ev_label_charging_speed_option_lt_11kw,
                minPower = null,
                maxPower = Power.kilowatts(11.0),
            ),
            ChargingSpeedEvFilterOption(
                imageVector = R.drawable.tt_asset_icon_chargeregular_fill_32,
                filterOptionDescription = R.string.demo_search_ev_label_charging_speed_option_12_49kw,
                minPower = Power.kilowatts(12.0),
                maxPower = Power.kilowatts(49.0),
            ),
            ChargingSpeedEvFilterOption(
                imageVector = R.drawable.tt_asset_icon_chargefast_fill_32,
                filterOptionDescription = R.string.demo_search_ev_label_charging_speed_option_50kw_plus,
                minPower = Power.kilowatts(50.0),
                maxPower = null,
            ),
            ChargingSpeedEvFilterOption(
                imageVector = R.drawable.tt_asset_icon_chargespeedultra_fill_32,
                filterOptionDescription = R.string.demo_search_ev_label_charging_speed_option_150kw_plus,
                minPower = Power.kilowatts(150.0),
                maxPower = null,
            ),
        ),
    ),
    EvFilter.CONNECTOR_TYPE to EvFilterCategory(
        imageVector = R.drawable.tt_asset_icon_generic_connector_fill_32,
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
            EvConnectorType.Tesla.toFilterOption(),
        ),
    ),
    EvFilter.STATE to EvFilterCategory(
        imageVector = R.drawable.tt_asset_icon_tick_line_32,
        filterDescription = R.string.demo_search_ev_title_state_category,
        allowsMultipleOptions = false,
        filterOptions = listOf(
            StatusEvFilterOption(
                imageVector = R.drawable.tt_asset_icon_tick_line_32,
                filterOptionDescription = R.string.demo_search_ev_label_state_option_available,
                status = Status.Available,
            ),
            StatusEvFilterOption(
                imageVector = R.drawable.outline_block_24px,
                filterOptionDescription = R.string.demo_search_ev_label_state_option_occupied,
                status = Status.Occupied,
            ),
            StatusEvFilterOption(
                imageVector = R.drawable.tt_asset_icon_warning_32,
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
