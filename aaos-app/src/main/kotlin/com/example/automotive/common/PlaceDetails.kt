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

package com.example.automotive.common

import com.tomtom.sdk.location.Address
import com.tomtom.sdk.location.Place

data class PlaceDetails(
    val place: Place,
)

val PlaceDetails.name: String
    get() = place.details?.names?.firstOrNull() ?: place.address.locationName

val PlaceDetails.locationDetails: String
    get() = place.details?.let { place.poiDetails.trim() }
        ?: place.address.locationDetails.trim()

private val Address?.locationName: String
    get() = this?.let {
        it.freeformAddress.substringBefore(",").trim().ifEmpty {
            it.streetNameAndNumber.trim().ifEmpty {
                it.streetName.trim().ifEmpty { null }
            }
        }
    } ?: ""

private val Address?.locationDetails: String
    get() = (
        this?.let {
            freeformAddress.substringAfter(",").ifEmpty {
                countrySecondarySubdivision.ifEmpty {
                    municipality.ifEmpty { "" }
                }
            }
        } ?: ""
    ) + (this?.countryCodeIso3?.let { ", $it" } ?: "")

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
