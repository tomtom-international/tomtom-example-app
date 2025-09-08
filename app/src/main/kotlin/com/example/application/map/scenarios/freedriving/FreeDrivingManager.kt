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

package com.example.application.map.scenarios.freedriving

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
