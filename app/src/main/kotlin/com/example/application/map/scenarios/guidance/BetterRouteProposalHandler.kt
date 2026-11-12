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

package com.example.application.map.scenarios.guidance

import android.util.Log
import com.example.application.common.extension.arrivalTime
import com.tomtom.sdk.navigation.ActiveRouteChangedListener
import com.tomtom.sdk.navigation.RouteAddedListener
import com.tomtom.sdk.navigation.RouteAddedReason
import com.tomtom.sdk.navigation.RouteRemovedListener
import com.tomtom.sdk.navigation.TomTomNavigation
import com.tomtom.sdk.navigation.progress.RouteProgress
import com.tomtom.sdk.routing.route.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Owns the better route proposal listeners on [TomTomNavigation] and exposes the pending [proposal].
 */
class BetterRouteProposalHandler(
    private val navigation: TomTomNavigation,
    private val routeProgress: StateFlow<RouteProgress?>,
) {
    private val _proposal = MutableStateFlow<BetterRouteProposal?>(null)
    val proposal: StateFlow<BetterRouteProposal?> = _proposal.asStateFlow()

    @Volatile
    private var isStarted = false

    private val routeAddedListener = RouteAddedListener { route, _, reason ->
        if (isStarted && reason.offersABetterRoute()) {
            onBetterRouteProposed(route)
        }
    }

    private val routeRemovedListener = RouteRemovedListener { route, _ ->
        if (isStarted) {
            _proposal.update { pending -> pending?.takeUnless { it.route.id == route.id } }
        }
    }

    private val activeRouteChangedListener = ActiveRouteChangedListener {
        if (isStarted) {
            _proposal.update { null }
        }
    }

    fun start() {
        isStarted = true

        navigation.addRouteAddedListener(routeAddedListener)
        navigation.addRouteRemovedListener(routeRemovedListener)
        navigation.addActiveRouteChangedListener(activeRouteChangedListener)
    }

    fun stop() {
        isStarted = false

        navigation.removeRouteAddedListener(routeAddedListener)
        navigation.removeRouteRemovedListener(routeRemovedListener)
        navigation.removeActiveRouteChangedListener(activeRouteChangedListener)

        _proposal.update { null }
    }

    fun accept() {
        val route = _proposal.value?.route ?: return
        Log.d(TAG, "Better route proposal accepted: ${route.id}")
        navigation.selectActiveRoute(route.id)
    }

    fun dismiss() {
        Log.d(TAG, "Better route proposal dismissed")
        _proposal.update { null }
    }

    private fun RouteAddedReason.offersABetterRoute(): Boolean = when (this) {
        is RouteAddedReason.BetterRouteProposed -> true
        else -> false
    }

    private fun onBetterRouteProposed(route: Route) {
        val progress = routeProgress.value

        Log.d(
            TAG,
            "Better route proposed: routeId=${route.id}, " +
                "arrivalTime=${route.summary.arrivalTime}, remainingTravelTime=${progress?.remainingTime}",
        )

        val timeSaved = progress?.let { timeSavedBy(route, it) } ?: return

        _proposal.update { BetterRouteProposal(route, timeSaved) }
    }

    private fun timeSavedBy(
        route: Route,
        progress: RouteProgress,
    ): Duration {
        val activeRouteArrival = progress.arrivalTime().toEpochMilli()
        val proposedRouteArrival = route.summary.arrivalTime.toInstant().toEpochMilli()
        return (activeRouteArrival - proposedRouteArrival).milliseconds
    }

    private companion object {
        private const val TAG = "BetterRouteProposalHandler"
    }
}

/**
 * A better route that navigation has proposed for the active trip.
 *
 * @param route The proposed route, which becomes the active route once the driver accepts it.
 * @param timeSaved How much earlier the driver is expected to arrive by taking [route].
 */
data class BetterRouteProposal(
    val route: Route,
    val timeSaved: Duration,
)
