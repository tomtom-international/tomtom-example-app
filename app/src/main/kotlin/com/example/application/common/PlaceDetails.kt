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

package com.example.application.common

import com.tomtom.quantity.Power
import com.tomtom.sdk.location.Address
import com.tomtom.sdk.location.Place
import com.tomtom.sdk.location.poi.Poi
import com.tomtom.sdk.location.poi.StandardCategoryId
import com.tomtom.sdk.location.poi.ev.AccessType
import com.tomtom.sdk.location.poi.ev.Status
import com.tomtom.sdk.vehicle.ConnectorType

data class PlaceDetails(
    val place: Place,
    val accessType: AccessType? = null,
    val nearbyPoiCategories: Set<StandardCategoryId> = emptySet(),
)

val PlaceDetails.locationDetails: String
    get() = this.place.details?.let { this.place.poiDetails.trim() }
        ?: this.place.address.locationDetails.trim()

val PlaceDetails.name: String
    get() = this.place.details?.names?.first() ?: this.place.address.locationName

val PlaceDetails.chargePointAvailability: MutableMap<ConnectorType, Map<Power, List<Status?>>>
    get() {
        val chargePointAvailabilityMap = mutableMapOf<ConnectorType, MutableMap<Power, MutableList<Status?>>>()

        place.details?.chargingPark?.chargingStations?.forEach { chargingStation ->
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

        place.details?.chargingPark?.chargingStations?.forEach { chargingStation ->
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
