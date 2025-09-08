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

package com.example.application.common

import com.tomtom.quantity.Power
import com.tomtom.sdk.location.Address
import com.tomtom.sdk.location.BetaLocationApi
import com.tomtom.sdk.location.Place
import com.tomtom.sdk.location.poi.Poi
import com.tomtom.sdk.location.poi.StandardCategoryId
import com.tomtom.sdk.location.poi.ev.AccessType
import com.tomtom.sdk.location.poi.ev.Status
import com.tomtom.sdk.vehicle.ConnectorType

@OptIn(BetaLocationApi::class)
data class PlaceDetails(
    val place: Place,
    val poi: Poi? = null,
    val accessType: AccessType? = null,
    val nearbyPoiCategories: Set<StandardCategoryId> = emptySet(),
)

val PlaceDetails.locationDetails: String
    get() = this.poi?.let { this.place.poiDetails.trim() }
        ?: this.place.address.locationDetails.trim()

val PlaceDetails.name: String
    get() = this.poi?.name ?: this.place.address.locationName

val PlaceDetails.chargePointAvailability: MutableMap<ConnectorType, Map<Power, List<Status?>>>
    get() {
        val chargePointAvailabilityMap = mutableMapOf<ConnectorType, MutableMap<Power, MutableList<Status?>>>()

        poi?.chargingPark?.chargingStations?.forEach { chargingStation ->
            chargingStation.chargingPoints.forEach { chargingPoint ->

                val status = chargingPoint.status

                chargingPoint.connectors.forEach { connector ->
                    val power = connector.connectorDetails?.ratedPower
                    val connectorType = connector.connectorDetails?.connectorType

                    if (power != null && connectorType != null) {
                        val powerMap = chargePointAvailabilityMap.getOrPut(connectorType) { mutableMapOf() }
                        val statusList = powerMap.getOrPut(power) { mutableListOf() }
                        statusList.add(status)
                    }
                }
            }
        }
        return chargePointAvailabilityMap.mapValues { it.value.mapValues { entry -> entry.value.toList() } }
            .mapValues { it.value }
            .toMutableMap()
    }

val PlaceDetails.powerAvailability: MutableMap<Power, List<Status?>>
    get() {
        val powerAvailabilityMap = mutableMapOf<Power, MutableList<Status?>>()

        poi?.chargingPark?.chargingStations?.forEach { chargingStation ->
            chargingStation.chargingPoints.forEach { chargingPoint ->
                val status = chargingPoint.status
                chargingPoint.connectors.forEach { connector ->
                    val power = connector.connectorDetails?.ratedPower
                    if (power != null) {
                        val statusList = powerAvailabilityMap.getOrPut(power) { mutableListOf() }
                        statusList.add(status)
                    }
                }
            }
        }
        return powerAvailabilityMap.mapValues { it.value.toList() }.toMutableMap()
    }

val Address?.locationName: String
    get() = this?.let {
        it.freeformAddress.substringBefore(",").trim().ifEmpty {
            it.streetNameAndNumber.trim().ifEmpty {
                it.streetName.trim().ifEmpty { null }
            }
        }
    } ?: ""

val Address?.locationDetails: String
    get() = (
        this?.let {
            freeformAddress.substringAfter(",").ifEmpty {
                countrySecondarySubdivision.ifEmpty {
                    municipality.ifEmpty { "" }
                }
            }
        } ?: ""
    ) + (this?.countryCodeIso3?.let { ", $it" } ?: "")

private val Poi.name: String?
    get() = this.let {
        it.names.elementAt(0).ifEmpty {
            it.urls.elementAt(0).ifEmpty { null }
        }
    }

private val Place.poiDetails: String
    get() = (
        (
            address?.let {
                it.freeformAddress.ifEmpty {
                    it.countrySecondarySubdivision.ifEmpty {
                        it.municipality.ifEmpty { null }
                    }
                }
            } ?: name
        ) + (address?.countryCodeIso3?.let { ", $it" } ?: "")
    ).trim()
