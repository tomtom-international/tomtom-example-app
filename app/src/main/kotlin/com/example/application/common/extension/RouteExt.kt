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

package com.example.application.common.extension

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
