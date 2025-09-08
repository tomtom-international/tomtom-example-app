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

@file:Suppress("detekt:MatchingDeclarationName")

package com.example.application.common

import com.tomtom.quantity.Distance
import com.tomtom.sdk.common.measures.FormattedDistance
import java.util.Locale
import kotlin.math.roundToLong

fun formatDistance(
    distance: Distance,
    locale: Locale = Locale.getDefault(),
): String {
    val distanceInMeters = distance.inMeters().toLong()
    return when (locale.measurementSystemPerCountry()) {
        MeasurementSystem.KILOMETERS_AND_METERS -> formatDistanceKilometersAndMeters(distanceInMeters).toString()
        MeasurementSystem.MILES_AND_FEET -> formatDistanceMilesAndFeet(distanceInMeters).toString()
        MeasurementSystem.MILES_AND_YARDS -> formatDistanceMilesAndYards(distanceInMeters).toString()
    }
}

private fun Locale.measurementSystemPerCountry(): MeasurementSystem = when {
    COUNTRY_CODES_USING_MILES_AND_YARDS.contains(isO3Country) -> MeasurementSystem.MILES_AND_YARDS
    COUNTRY_CODES_USING_MILES_AND_FEET.contains(isO3Country) -> MeasurementSystem.MILES_AND_FEET
    else -> MeasurementSystem.KILOMETERS_AND_METERS
}

@Suppress("detekt:MagicNumber")
private fun formatDistanceKilometersAndMeters(distanceInMeters: Long) = when {
    distanceInMeters < 10.0 -> {
        FormattedDistance("0", DISTANCE_UNITS_METERS)
    }

    distanceInMeters < 500.0 -> {
        val roundedMeters = (distanceInMeters / 10.0f).roundToLong() * 10
        FormattedDistance(String.format(Locale.getDefault(), "%d", roundedMeters), DISTANCE_UNITS_METERS)
    }

    distanceInMeters < 950.0 -> {
        // Using 950m to avoid displaying 1000m after rounding to nearest 100
        val roundedMeters = (distanceInMeters / 100.0f).roundToLong() * 100
        FormattedDistance(String.format(Locale.getDefault(), "%d", roundedMeters), DISTANCE_UNITS_METERS)
    }

    distanceInMeters < 9950.0 -> {
        // Using 9950m to avoid displaying 10.0mi (ie 10 to 1 decimal place, as it should be 10mi)
        val roundedKilometers = (distanceInMeters / 100.0f) * 0.1
        FormattedDistance(String.format(Locale.getDefault(), "%.1f", roundedKilometers), DISTANCE_UNITS_KILOMETERS)
    }

    else -> {
        val roundedKilometers = (distanceInMeters / 1000.0f).roundToLong()
        FormattedDistance(String.format(Locale.getDefault(), "%d", roundedKilometers), DISTANCE_UNITS_KILOMETERS)
    }
}

@Suppress("detekt:MagicNumber")
private fun formatDistanceMilesAndFeet(distanceInMeters: Long): FormattedDistance {
    val distanceInMiles: Double = distanceInMeters / METERS_PER_MILE
    val completeMiles = distanceInMiles.toInt()
    val fractionInFeet: Double = (distanceInMiles - completeMiles.toDouble()) * FEET_PER_MILE

    return when {
        distanceInMeters < 0 -> {
            FormattedDistance("0", DISTANCE_UNITS_FEET)
        }

        completeMiles == 0 && fractionInFeet < 30.0 -> {
            FormattedDistance("0", DISTANCE_UNITS_FEET)
        }

        completeMiles == 0 && fractionInFeet < 500.0 -> {
            val fractionInFeetRounded = (fractionInFeet / 10.0).roundToLong() * 10
            FormattedDistance(String.format(Locale.getDefault(), "%d", fractionInFeetRounded), DISTANCE_UNITS_FEET)
        }
        // Using 950ft to avoid displaying 1000ft after rounding to nearest 100
        completeMiles == 0 && fractionInFeet < 950.0 -> {
            val fractionInFeetRounded = (fractionInFeet / 100.0).roundToLong() * 100
            FormattedDistance(String.format(Locale.getDefault(), "%d", fractionInFeetRounded), DISTANCE_UNITS_FEET)
        }
        // Using 9.95mi to avoid displaying 10.0mi (ie 10 to 1 decimal place, as it should be 10mi)
        distanceInMiles < 9.95 -> {
            val roundedMiles: Double = ((distanceInMiles / 0.1) * 0.1)
            FormattedDistance(String.format(Locale.getDefault(), "%.1f", roundedMiles), DISTANCE_UNITS_MILES)
        }

        else -> {
            val roundedMiles = distanceInMiles.roundToLong()
            FormattedDistance(String.format(Locale.getDefault(), "%d", roundedMiles), DISTANCE_UNITS_MILES)
        }
    }
}

@Suppress("detekt:MagicNumber")
private fun formatDistanceMilesAndYards(distanceInMeters: Long): FormattedDistance {
    val distanceInMiles: Double = distanceInMeters / METERS_PER_MILE
    val completeMiles = distanceInMiles.toInt()
    val fractionInYards: Double = (distanceInMiles - completeMiles.toDouble()) * YARDS_PER_MILE

    return when {
        distanceInMeters < 0 -> {
            FormattedDistance("0", DISTANCE_UNITS_YARDS)
        }

        completeMiles == 0 && fractionInYards < 10.0 -> {
            FormattedDistance("0", DISTANCE_UNITS_YARDS)
        }

        completeMiles == 0 && fractionInYards < 500.0 -> {
            val fractionInYardsRounded = (fractionInYards / 10.0).roundToLong() * 10
            FormattedDistance(String.format(Locale.getDefault(), "%d", fractionInYardsRounded), DISTANCE_UNITS_YARDS)
        }
        // Using 845yds to avoid displaying 900yds after rounding to nearest 100
        completeMiles == 0 && fractionInYards < 845.0 -> {
            val fractionInYardsRounded = (fractionInYards / 100.0).roundToLong() * 100
            FormattedDistance(String.format(Locale.getDefault(), "%d", fractionInYardsRounded), DISTANCE_UNITS_YARDS)
        }
        // Using 9.95mi to avoid displaying 10.0mi (ie 10 to 1 decimal place, as it should be 10mi)
        distanceInMiles < 9.95 -> {
            val roundedMiles: Double = ((distanceInMiles / 0.1) * 0.1)
            FormattedDistance(String.format(Locale.getDefault(), "%.1f", roundedMiles), DISTANCE_UNITS_MILES)
        }

        else -> {
            val roundedMiles = distanceInMiles.roundToLong()
            FormattedDistance(String.format(Locale.getDefault(), "%d", roundedMiles), DISTANCE_UNITS_MILES)
        }
    }
}

private enum class MeasurementSystem {
    KILOMETERS_AND_METERS,
    MILES_AND_FEET,
    MILES_AND_YARDS,
}

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
