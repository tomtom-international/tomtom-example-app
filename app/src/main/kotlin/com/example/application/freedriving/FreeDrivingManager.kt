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

package com.example.application.freedriving

import com.tomtom.quantity.Distance
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.LocationProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.time.delay
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

/** This class is responsible for determining if the user is driving.
 * If the current user location changes more than 30 meters compared to any of the last 100 seconds of cached locations,
 * the user is considered to be driving. If none of the cached locations are farther than 30 meters,
 * the user is not driving.
 * It ignores locations with an accuracy greater than 15 meters. **/
class FreeDrivingManager(private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default) {
    private var isDriving = false

    private val locationCache = mutableListOf<GeoPoint?>()

    fun getIsDrivingFlow(onGetLocationProvider: () -> LocationProvider?) = flow {
        while (currentCoroutineContext().isActive) {
            onGetLocationProvider()?.lastKnownLocation?.let { lastKnownLocation ->
                lastKnownLocation.accuracy?.let {
                    if (it < ACCURACY_THRESHOLD) {
                        emit(lastKnownLocation.position)
                    } else {
                        emit(null)
                    }
                } ?: emit(lastKnownLocation.position)
            }
            delay(LOCATION_EVENTS_INTERVAL)
        }
    }.map { position ->
        if (locationCache.size >= MAX_CACHE_SIZE) {
            locationCache.removeAt(0)
        }
        locationCache.add(position)

        if (position != null) {
            isDriving =
                locationCache.any { cachedLocation ->
                    if (cachedLocation != null) {
                        position.distanceTo(cachedLocation) > THRESHOLD_DISTANCE_METERS
                    } else {
                        false
                    }
                }
        }

        isDriving
    }.distinctUntilChanged().flowOn(defaultDispatcher)

    companion object {
        private val LOCATION_EVENTS_INTERVAL = 1.seconds.toJavaDuration()
        private val THRESHOLD_DISTANCE_METERS = Distance.meters(30)
        private val ACCURACY_THRESHOLD = Distance.meters(15)
        private const val MAX_CACHE_SIZE = 100
    }
}
