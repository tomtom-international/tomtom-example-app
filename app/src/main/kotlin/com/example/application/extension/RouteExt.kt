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

package com.example.application.extension

import com.example.application.common.formatDistance
import com.tomtom.sdk.location.GeoLocation
import com.tomtom.sdk.navigation.progress.RouteProgress
import com.tomtom.sdk.routing.route.Route
import java.time.Instant.now

fun Route.formattedArrivalTime(): String = this.summary.arrivalTime.toInstant().toEpochMilli().asFormattedTime()

fun RouteProgress.formattedRemainingTime(): String =
    now().plusSeconds(remainingTime.inWholeSeconds).toEpochMilli().asFormattedTime()

fun Route.formattedDistance(): String = formatDistance(summary.length)

fun RouteProgress.formattedRemainingDistance(): String = formatDistance(remainingDistance)

fun Route.formattedDuration(): String? =
    summary.travelTime.toComponents { days, hours, minutes, _, _ -> formatDuration(days, hours, minutes) }

fun RouteProgress.formattedDuration(): String? =
    remainingTime.toComponents { days, hours, minutes, _, _ -> formatDuration(days, hours, minutes) }

val Route.locations: List<GeoLocation>
    get() = legs.flatMap { it.points }.map { GeoLocation(it) }

private fun formatDuration(
    days: Long,
    hours: Int,
    minutes: Int,
): String? = when {
    days > 0 -> "${days}d ${hours}h"
    hours > 0 && minutes > 0 -> "$hours hr $minutes min"
    hours > 0 -> "$hours hr"
    minutes > 0 -> "$minutes min"
    else -> null
}
