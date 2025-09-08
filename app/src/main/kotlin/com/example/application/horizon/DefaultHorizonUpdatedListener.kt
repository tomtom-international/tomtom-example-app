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

package com.example.application.horizon

import android.util.Log
import com.example.application.horizon.element.SafetyLocation
import com.example.application.horizon.element.Traffic
import com.example.application.horizon.element.UpcomingHorizonElements
import com.tomtom.quantity.Distance
import com.tomtom.sdk.navigation.HorizonUpdatedListener
import com.tomtom.sdk.navigation.horizon.HorizonOptions
import com.tomtom.sdk.navigation.horizon.HorizonPosition
import com.tomtom.sdk.navigation.horizon.HorizonSnapshot
import com.tomtom.sdk.navigation.horizon.elements.HorizonElement
import com.tomtom.sdk.navigation.horizon.elements.safetylocation.SafetyLocationElement
import com.tomtom.sdk.navigation.horizon.elements.safetylocation.SafetyLocationElementType
import com.tomtom.sdk.navigation.horizon.elements.traffic.TrafficElement
import com.tomtom.sdk.navigation.horizon.elements.traffic.TrafficElementType

/**
 * Aggregates upcoming traffic and safety-location horizon elements and emits them via a callback on updates.
 */
class DefaultHorizonUpdatedListener(
    private val onHorizonElementsUpdated: (UpcomingHorizonElements) -> Unit,
) : HorizonUpdatedListener {
    private var latestSafetyLocationElement: SafetyLocationElement? = null
    private var latestTrafficElement: TrafficElement? = null
    private var latestHorizonPosition: HorizonPosition? = null
    private var latestSnapshot: HorizonSnapshot? = null

    override fun onPositionUpdated(
        options: HorizonOptions,
        position: HorizonPosition,
    ) {
        latestHorizonPosition = position
        latestSnapshot?.let { snapshot ->
            val traffic = latestTrafficElement?.let {
                Traffic(
                    distance = calculateDistanceIfPossible(snapshot, it),
                    element = it,
                )
            }

            val safety = latestSafetyLocationElement?.let {
                SafetyLocation.create(
                    distance = calculateDistanceIfPossible(snapshot, it),
                    element = it,
                )
            }

            onHorizonElementsUpdated(
                UpcomingHorizonElements(
                    trafficElement = traffic,
                    safetyLocationElement = safety,
                ),
            )
        }
    }

    override fun onSnapshotUpdated(
        options: HorizonOptions,
        snapshot: HorizonSnapshot,
    ) {
        latestSnapshot = snapshot
        latestTrafficElement = snapshot.paths.firstOrNull()
            ?.getElements(TrafficElementType)
            ?.firstOrNull() as? TrafficElement

        latestSafetyLocationElement = snapshot.paths.firstOrNull()
            ?.getElements(SafetyLocationElementType)
            ?.firstOrNull() as? SafetyLocationElement

        val traffic = latestTrafficElement?.let { trafficElement ->
            Traffic(
                distance = calculateDistanceIfPossible(snapshot, trafficElement),
                element = trafficElement,
            )
        }

        val safetyLocation = latestSafetyLocationElement?.let { safetyLocation ->
            SafetyLocation.create(
                distance = calculateDistanceIfPossible(snapshot, safetyLocation),
                element = safetyLocation,
            )
        }

        onHorizonElementsUpdated(
            UpcomingHorizonElements(
                trafficElement = traffic,
                safetyLocationElement = safetyLocation,
            ),
        )
    }

    override fun onHorizonReset(options: HorizonOptions) {
        Log.d(TAG, "Horizon reset")
    }

    private fun calculateDistanceIfPossible(
        snapshot: HorizonSnapshot,
        horizonElement: HorizonElement?,
    ): Distance? = horizonElement?.let { element ->
        latestHorizonPosition?.let { position ->
            snapshot.distanceTo(element, position)
        }
    }

    companion object {
        private const val TAG = "DefaultHorizonUpdatedListener"
    }
}
