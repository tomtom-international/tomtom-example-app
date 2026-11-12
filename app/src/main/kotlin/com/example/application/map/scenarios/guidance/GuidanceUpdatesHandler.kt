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
import com.example.application.common.tts.TextToSpeechEngine
import com.tomtom.quantity.Distance
import com.tomtom.sdk.navigation.AnnouncementListener
import com.tomtom.sdk.navigation.DestinationArrivalListener
import com.tomtom.sdk.navigation.GuidanceUpdatedListener
import com.tomtom.sdk.navigation.LaneGuidanceUpdatedListener
import com.tomtom.sdk.navigation.ProgressUpdatedListener
import com.tomtom.sdk.navigation.TomTomNavigation
import com.tomtom.sdk.navigation.announcement.VerbalMessage
import com.tomtom.sdk.navigation.announcement.WarningAnnouncement
import com.tomtom.sdk.navigation.guidance.GuidanceAnnouncement
import com.tomtom.sdk.navigation.guidance.InstructionPhase
import com.tomtom.sdk.navigation.guidance.LaneGuidance
import com.tomtom.sdk.navigation.guidance.instruction.GuidanceInstruction
import com.tomtom.sdk.navigation.progress.RouteProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Owns the turn-by-turn guidance listeners on [TomTomNavigation] and exposes the derived
 * [nextInstruction], [laneGuidance], and [routeProgress]. Announcements play via [textToSpeechEngine];
 * arrival is reported via [onDestinationArrival] so screen state stays with the caller.
 *
 * The caller starts/stops navigation and owns the ambient (location/horizon) listeners that free
 * driving also uses.
 */
class GuidanceUpdatesHandler(
    private val navigation: TomTomNavigation,
    private val textToSpeechEngine: TextToSpeechEngine,
    private val onDestinationArrival: () -> Unit,
) {
    private val _nextInstruction = MutableStateFlow<NextInstruction?>(null)
    val nextInstruction: StateFlow<NextInstruction?> = _nextInstruction.asStateFlow()

    private val _laneGuidance = MutableStateFlow<LaneGuidance?>(null)
    val laneGuidance: StateFlow<LaneGuidance?> = _laneGuidance.asStateFlow()

    private val _routeProgress = MutableStateFlow<RouteProgress?>(null)
    val routeProgress: StateFlow<RouteProgress?> = _routeProgress.asStateFlow()

    private val announcementListener = AnnouncementListener { announcements ->
        announcements.forEach { announcement ->
            if (announcement is WarningAnnouncement) {
                val message = announcement.message
                Log.d(TAG, "Announcement type: ${announcement::class.java.simpleName}, message: $message")
                if (message is VerbalMessage) {
                    textToSpeechEngine.playMessage(
                        message = message.plainTextMessage,
                        onError = { error -> Log.e(TAG, "Error playing announcement: $error") },
                    )
                }
            }
        }
    }

    private val guidanceUpdatedListener = object : GuidanceUpdatedListener {
        override fun onAnnouncementGenerated(
            announcement: GuidanceAnnouncement,
            shouldPlay: Boolean,
        ) {
            Log.d(TAG, "Guidance announcement: $announcement")

            if (shouldPlay) {
                textToSpeechEngine.playMessage(
                    message = announcement.plainTextMessage,
                    onError = { error -> Log.e(TAG, "Error playing announcement: $error") },
                )
            }
        }

        override fun onDistanceToNextInstructionChanged(
            distance: Distance,
            instructions: List<GuidanceInstruction>,
            currentPhase: InstructionPhase,
        ) {
            Log.d(TAG, "New distance to instruction $distance")
            _nextInstruction.update { it?.copy(distanceToManeuver = distance) }
        }

        override fun onInstructionsChanged(instructions: List<GuidanceInstruction>) {
            Log.d(TAG, "New instruction received from GuidanceUpdatedListener: $instructions")
            _nextInstruction.update { processNextInstruction(instructions) }
        }
    }

    private val progressUpdatedListener = ProgressUpdatedListener { routeProgress ->
        _routeProgress.tryEmit(routeProgress)
    }

    private val destinationArrivalListener = DestinationArrivalListener { _ ->
        onDestinationArrival()
    }

    private val laneGuidanceUpdatedListener = object : LaneGuidanceUpdatedListener {
        override fun onLaneGuidanceEnded(laneGuidance: LaneGuidance) {
            Log.d(TAG, "Lane guidance ended: $laneGuidance")
            _laneGuidance.update { null }
        }

        override fun onLaneGuidanceStarted(laneGuidance: LaneGuidance) {
            Log.d(TAG, "Lane guidance started: $laneGuidance")
            _laneGuidance.update { laneGuidance }
        }
    }

    // Registers the listeners; the caller starts navigation afterwards.
    fun start() {
        navigation.addAnnouncementListener(announcementListener)
        navigation.addGuidanceUpdatedListener(guidanceUpdatedListener)
        navigation.addProgressUpdatedListener(progressUpdatedListener)
        navigation.addDestinationArrivalListener(destinationArrivalListener)
        navigation.addLaneGuidanceUpdatedListener(laneGuidanceUpdatedListener)
    }

    // Unregisters the listeners and clears the signals so stale data can't outlive the trip.
    fun stop() {
        navigation.removeAnnouncementListener(announcementListener)
        navigation.removeProgressUpdatedListener(progressUpdatedListener)
        navigation.removeGuidanceUpdatedListener(guidanceUpdatedListener)
        navigation.removeDestinationArrivalListener(destinationArrivalListener)
        navigation.removeLaneGuidanceUpdatedListener(laneGuidanceUpdatedListener)

        _nextInstruction.update { null }
        _laneGuidance.update { null }
        _routeProgress.update { null }
    }

    private fun processNextInstruction(instructions: List<GuidanceInstruction>?): NextInstruction? =
        instructions?.firstOrNull()?.let { firstInstruction ->
            NextInstruction(
                maneuverType = firstInstruction.toManeuverType(),
                distanceToManeuver = firstInstruction.routeOffset,
                roadName = firstInstruction.nextSignificantRoad?.name,
                towardName = firstInstruction.signpost?.towardName,
                exitNumber = firstInstruction.signpost?.exitNumber,
                exitName = firstInstruction.signpost?.exitName,
            )
        }

    private companion object {
        private const val TAG = "GuidanceUpdatesHandler"
    }
}
