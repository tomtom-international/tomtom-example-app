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

import com.tomtom.quantity.Distance
import java.util.Locale
import kotlin.math.roundToLong

fun formatDistance(
    distance: Distance,
    locale: Locale = Locale.getDefault(),
): String {
    val distanceInMeters = distance.inMeters().toLong()
    return when (locale.measurementSystemPerCountry()) {
        MeasurementSystem.KILOMETERS_AND_METERS -> formatDistanceKilometersAndMeters(distanceInMeters)
        MeasurementSystem.MILES_AND_FEET -> formatDistanceMilesAndFeet(distanceInMeters)
        MeasurementSystem.MILES_AND_YARDS -> formatDistanceMilesAndYards(distanceInMeters)
    }
}

private fun formattedDistanceString(
    distance: String,
    unit: String,
): String = "$distance $unit"

private fun Locale.measurementSystemPerCountry(): MeasurementSystem = when {
    COUNTRY_CODES_USING_MILES_AND_YARDS.contains(isO3Country) -> MeasurementSystem.MILES_AND_YARDS
    COUNTRY_CODES_USING_MILES_AND_FEET.contains(isO3Country) -> MeasurementSystem.MILES_AND_FEET
    else -> MeasurementSystem.KILOMETERS_AND_METERS
}

private fun formatDistanceKilometersAndMeters(distanceInMeters: Long): String = when {
    distanceInMeters < METERS_DISPLAY_THRESHOLD -> {
        formattedDistanceString("0", DISTANCE_UNITS_METERS)
    }

    distanceInMeters < METERS_ROUND_TO_10_THRESHOLD -> {
        val roundedMeters = (distanceInMeters.toDouble() / ROUNDING_STEP_10).roundToLong() * ROUNDING_STEP_10
        formattedDistanceString(String.format(Locale.getDefault(), "%d", roundedMeters), DISTANCE_UNITS_METERS)
    }

    distanceInMeters < METERS_ROUND_TO_100_THRESHOLD -> {
        // Using 950m to avoid displaying 1000m after rounding to nearest 100
        val roundedMeters = (distanceInMeters.toDouble() / ROUNDING_STEP_100).roundToLong() * ROUNDING_STEP_100
        formattedDistanceString(String.format(Locale.getDefault(), "%d", roundedMeters), DISTANCE_UNITS_METERS)
    }

    distanceInMeters < METERS_USE_KM_DECIMAL_THRESHOLD -> {
        // Using 9950m to avoid displaying 10.0km (ie 10 to 1 decimal place, as it should be 10km)
        val roundedKilometers = distanceInMeters / METERS_PER_KILOMETER
        formattedDistanceString(
            String.format(Locale.getDefault(), "%.1f", roundedKilometers),
            DISTANCE_UNITS_KILOMETERS,
        )
    }

    else -> {
        val roundedKilometers = (distanceInMeters / METERS_PER_KILOMETER).roundToLong()
        formattedDistanceString(String.format(Locale.getDefault(), "%d", roundedKilometers), DISTANCE_UNITS_KILOMETERS)
    }
}

private fun formatDistanceMilesAndFeet(distanceInMeters: Long): String {
    val distanceInMiles: Double = distanceInMeters / METERS_PER_MILE
    val completeMiles = distanceInMiles.toInt()
    val fractionInFeet: Double = (distanceInMiles - completeMiles.toDouble()) * FEET_PER_MILE

    return when {
        distanceInMeters < 0 -> {
            formattedDistanceString("0", DISTANCE_UNITS_FEET)
        }

        completeMiles == 0 && fractionInFeet < FEET_DISPLAY_THRESHOLD -> {
            formattedDistanceString("0", DISTANCE_UNITS_FEET)
        }

        completeMiles == 0 && fractionInFeet < FEET_ROUND_TO_10_THRESHOLD -> {
            val fractionInFeetRounded = (fractionInFeet / ROUNDING_STEP_10).roundToLong() * ROUNDING_STEP_10
            formattedDistanceString(
                String.format(Locale.getDefault(), "%d", fractionInFeetRounded),
                DISTANCE_UNITS_FEET,
            )
        }
        // Using 950ft to avoid displaying 1000ft after rounding to nearest 100
        completeMiles == 0 && fractionInFeet < FEET_ROUND_TO_100_THRESHOLD -> {
            val fractionInFeetRounded = (fractionInFeet / ROUNDING_STEP_100).roundToLong() * ROUNDING_STEP_100
            formattedDistanceString(
                String.format(Locale.getDefault(), "%d", fractionInFeetRounded),
                DISTANCE_UNITS_FEET,
            )
        }
        // Using 9.95mi to avoid displaying 10.0mi (ie 10 to 1 decimal place, as it should be 10mi)
        distanceInMiles < MILES_USE_DECIMAL_THRESHOLD -> {
            val roundedMiles: Double = ((distanceInMiles / MILES_DECIMAL_STEP) * MILES_DECIMAL_STEP)
            formattedDistanceString(String.format(Locale.getDefault(), "%.1f", roundedMiles), DISTANCE_UNITS_MILES)
        }

        else -> {
            val roundedMiles = distanceInMiles.roundToLong()
            formattedDistanceString(String.format(Locale.getDefault(), "%d", roundedMiles), DISTANCE_UNITS_MILES)
        }
    }
}

private fun formatDistanceMilesAndYards(distanceInMeters: Long): String {
    val distanceInMiles: Double = distanceInMeters / METERS_PER_MILE
    val completeMiles = distanceInMiles.toInt()
    val fractionInYards: Double = (distanceInMiles - completeMiles.toDouble()) * YARDS_PER_MILE

    return when {
        distanceInMeters < 0 -> {
            formattedDistanceString("0", DISTANCE_UNITS_YARDS)
        }

        completeMiles == 0 && fractionInYards < YARDS_DISPLAY_THRESHOLD -> {
            formattedDistanceString("0", DISTANCE_UNITS_YARDS)
        }

        completeMiles == 0 && fractionInYards < YARDS_ROUND_TO_10_THRESHOLD -> {
            val fractionInYardsRounded = (fractionInYards / ROUNDING_STEP_10).roundToLong() * ROUNDING_STEP_10
            formattedDistanceString(
                String.format(Locale.getDefault(), "%d", fractionInYardsRounded),
                DISTANCE_UNITS_YARDS,
            )
        }
        // Using 845yds to avoid displaying 900yds after rounding to nearest 100
        completeMiles == 0 && fractionInYards < YARDS_ROUND_TO_100_THRESHOLD -> {
            val fractionInYardsRounded = (fractionInYards / ROUNDING_STEP_100).roundToLong() * ROUNDING_STEP_100
            formattedDistanceString(
                String.format(Locale.getDefault(), "%d", fractionInYardsRounded),
                DISTANCE_UNITS_YARDS,
            )
        }
        // Using 9.95mi to avoid displaying 10.0mi (ie 10 to 1 decimal place, as it should be 10mi)
        distanceInMiles < MILES_USE_DECIMAL_THRESHOLD -> {
            val roundedMiles: Double = ((distanceInMiles / MILES_DECIMAL_STEP) * MILES_DECIMAL_STEP)
            formattedDistanceString(String.format(Locale.getDefault(), "%.1f", roundedMiles), DISTANCE_UNITS_MILES)
        }

        else -> {
            val roundedMiles = distanceInMiles.roundToLong()
            formattedDistanceString(String.format(Locale.getDefault(), "%d", roundedMiles), DISTANCE_UNITS_MILES)
        }
    }
}

private enum class MeasurementSystem {
    KILOMETERS_AND_METERS,
    MILES_AND_FEET,
    MILES_AND_YARDS,
}

private const val ISO3_USA = "USA"
private const val ISO3_GBR = "GBR"
private const val ISO3_PRI = "PRI"
private val COUNTRY_CODES_USING_MILES_AND_YARDS: Set<String> = HashSet(listOf(ISO3_GBR))
private val COUNTRY_CODES_USING_MILES_AND_FEET: Set<String> = HashSet(listOf(ISO3_USA, ISO3_PRI))

private const val DISTANCE_UNITS_METERS = "m"
private const val DISTANCE_UNITS_FEET = "ft"
private const val DISTANCE_UNITS_YARDS = "yd"

private const val DISTANCE_UNITS_KILOMETERS = "km"
private const val DISTANCE_UNITS_MILES = "mi"

private const val FEET_PER_MILE = 5280.0
private const val YARDS_PER_MILE = 1760.0
private const val METERS_PER_MILE = 1609.344
private const val METERS_PER_KILOMETER = 1000.0

private const val ROUNDING_STEP_10 = 10
private const val ROUNDING_STEP_100 = 100

private const val METERS_DISPLAY_THRESHOLD = 10.0
private const val METERS_ROUND_TO_10_THRESHOLD = 500.0
private const val METERS_ROUND_TO_100_THRESHOLD = 950.0
private const val METERS_USE_KM_DECIMAL_THRESHOLD = 9950.0

private const val FEET_DISPLAY_THRESHOLD = 30.0
private const val FEET_ROUND_TO_10_THRESHOLD = 500.0
private const val FEET_ROUND_TO_100_THRESHOLD = 950.0

private const val YARDS_DISPLAY_THRESHOLD = 10.0
private const val YARDS_ROUND_TO_10_THRESHOLD = 500.0
private const val YARDS_ROUND_TO_100_THRESHOLD = 845.0

private const val MILES_USE_DECIMAL_THRESHOLD = 9.95
private const val MILES_DECIMAL_STEP = 0.1
