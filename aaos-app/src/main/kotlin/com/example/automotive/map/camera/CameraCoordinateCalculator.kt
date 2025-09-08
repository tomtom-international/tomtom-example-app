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

package com.example.automotive.map.camera

import com.tomtom.sdk.location.GeoPoint
import kotlin.math.cos
import kotlin.math.pow

/**
 * Utility for camera coordinate transformations, including pan gestures, zoom calculations,
 * and Mercator projection adjustments.
 */
object CameraCoordinateCalculator {
    private const val TILE_SIZE_PX = 512.0
    private const val MIN_LATITUDE = -85.0
    private const val MAX_LATITUDE = 85.0
    private const val FULL_LONGITUDE_DEGREES = 360.0
    private const val LONGITUDE_HALF_RANGE = 180.0

    /**
     * Computes the new camera center after a pan gesture.
     *
     * @param currentCenter Current camera position
     * @param currentZoom Current zoom level
     * @param distanceX Pan distance in pixels (positive = pan right, screen content moves left)
     * @param distanceY Pan distance in pixels (positive = pan down, screen content moves up)
     * @return New geographic center point with wrapped longitude and clamped latitude
     */
    fun computePannedCenter(
        currentCenter: GeoPoint,
        currentZoom: Double,
        distanceX: Float,
        distanceY: Float,
    ): GeoPoint {
        val scale = 2.0.pow(currentZoom)
        val degreesPerPixelLon = FULL_LONGITUDE_DEGREES / (TILE_SIZE_PX * scale)

        // Apply cosine correction for latitude to account for Mercator projection distortion
        val degreesPerPixelLat = degreesPerPixelLon * cos(Math.toRadians(currentCenter.latitude))

        val deltaLon = distanceX.toDouble() * degreesPerPixelLon
        val deltaLat = -distanceY.toDouble() * degreesPerPixelLat // Negative because screen Y increases downward

        val newLat = (currentCenter.latitude + deltaLat).coerceIn(MIN_LATITUDE, MAX_LATITUDE)
        val newLon = wrapLongitude(currentCenter.longitude + deltaLon)

        return GeoPoint(newLat, newLon)
    }

    /**
     * Wraps longitude to the range [-180, 180].
     *
     * @param longitude Longitude value to wrap
     * @return Wrapped longitude in the range [-180, 180]
     */
    fun wrapLongitude(longitude: Double): Double {
        // Handle the edge case where longitude is exactly -180 or 180
        if (longitude == -LONGITUDE_HALF_RANGE || longitude == LONGITUDE_HALF_RANGE) {
            return longitude
        }

        // Normalize to [0, 360) range first
        var wrapped = longitude % FULL_LONGITUDE_DEGREES

        // Handle negative modulo results
        if (wrapped < 0) {
            wrapped += FULL_LONGITUDE_DEGREES
        }

        // Convert [0, 360) to [-180, 180]
        if (wrapped > LONGITUDE_HALF_RANGE) {
            wrapped -= FULL_LONGITUDE_DEGREES
        }

        return wrapped
    }

    /**
     * Converts a scale factor from a pinch gesture to a zoom level delta.
     *
     * @param scaleFactor Pinch scale factor (2.0 = doubled size)
     * @param sensitivity Gesture sensitivity multiplier (default 2.0)
     * @return Zoom level delta to apply
     */
    fun scaleFactorToZoomDelta(
        scaleFactor: Float,
        sensitivity: Double = 2.0,
    ): Double {
        // 2x scale ~ +1 zoom level (before sensitivity)
        // Using log base 2 because each zoom level doubles the scale
        return Math.log(scaleFactor.toDouble()) / Math.log(2.0) * sensitivity
    }
}
