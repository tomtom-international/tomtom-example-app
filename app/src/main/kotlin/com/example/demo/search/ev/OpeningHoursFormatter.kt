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

package com.example.demo.search.ev

import com.tomtom.sdk.location.poi.time.OpeningHours
import java.text.DateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Formats the opening hours for the given [day] as a display string.
 *
 * Returns [closedLabel] if no range overlaps [day], [open24hLabel] if [day] falls inside a
 * single range spanning ≥ 24 hours (the SDK's encoding for a continuously-open station —
 * see the class docs on [OpeningHours]), otherwise the intersecting intervals clipped to
 * the day and joined by commas (e.g. `08:00–12:00, 14:00–20:00`).
 */
internal fun OpeningHours.formatForDay(
    day: Date,
    open24hLabel: String,
    closedLabel: String,
    locale: Locale = Locale.getDefault(),
): String {
    val dayStart = day.startOfDay()
    val dayEnd = dayStart.plusDays(1)
    val overlapping = ranges.filter { it.endInclusive > dayStart && it.start < dayEnd }
    return when {
        overlapping.isEmpty() -> closedLabel
        overlapping.any { it.spansAtLeastFullDay() } -> open24hLabel
        else -> {
            val timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT, locale)
            overlapping
                .map { maxOf(it.start, dayStart)..minOf(it.endInclusive, dayEnd) }
                .joinToString(", ") { "${timeFormat.format(it.start)}–${timeFormat.format(it.endInclusive)}" }
        }
    }
}

/** The 6 days immediately following [today], each at start-of-day. */
internal fun daysAfterToday(today: Date): List<Date> {
    val start = today.startOfDay()
    return (1..DAYS_AFTER_TODAY).map { start.plusDays(it) }
}

// A range lasting ≥ 24 hours represents a continuously-open span covering at least
// one full calendar day — every day it touches is effectively 24h.
private fun ClosedRange<Date>.spansAtLeastFullDay(): Boolean =
    endInclusive.time - start.time >= FULL_DAY_MS - FULL_DAY_TOLERANCE_MS

private fun Date.startOfDay(): Date = Calendar.getInstance().apply {
    time = this@startOfDay
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.time

private fun Date.plusDays(days: Int): Date = Calendar.getInstance().apply {
    time = this@plusDays
    add(Calendar.DAY_OF_MONTH, days)
}.time

private const val DAYS_AFTER_TODAY = 6
private const val FULL_DAY_MS = 24 * 60 * 60 * 1000L
private const val FULL_DAY_TOLERANCE_MS = 60_000L
