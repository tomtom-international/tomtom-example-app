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
import com.tomtom.sdk.navigation.horizon.annotation.BetaHorizonTrafficElementApi
import com.tomtom.sdk.navigation.horizon.elements.HorizonElement
import com.tomtom.sdk.navigation.horizon.elements.safetylocation.SafetyLocationElement
import com.tomtom.sdk.navigation.horizon.elements.safetylocation.SafetyLocationElementType
import com.tomtom.sdk.navigation.horizon.elements.traffic.TrafficElement
import com.tomtom.sdk.navigation.horizon.elements.traffic.TrafficElementType
import com.tomtom.sdk.traffic.common.BetaCoreTrafficEventApi

@OptIn(BetaHorizonTrafficElementApi::class, BetaCoreTrafficEventApi::class)
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
