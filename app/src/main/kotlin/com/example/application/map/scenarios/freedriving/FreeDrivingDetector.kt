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

import android.util.Log
import com.example.application.map.model.Scenario
import com.example.application.map.model.Scenario.FREE_DRIVING
import com.example.application.map.model.Scenario.HOME
import com.tomtom.sdk.location.LocationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Detects whether the user is driving by collecting [FreeDrivingManager.getIsDrivingFlow] and reports
 * the transitions through [startDetecting]'s callbacks. The caller owns the navigation session and
 * screen state.
 *
 * @param freeDrivingManager source of the periodic driving-state signal.
 * @param scope coroutine scope the detection runs in; typically the owner's `viewModelScope`.
 */
class FreeDrivingDetector(
    private val freeDrivingManager: FreeDrivingManager,
    private val scope: CoroutineScope,
) {
    private var detectionJob: Job? = null

    /**
     * Starts observing the driving state, unless [scenario] is neither [Scenario.HOME] nor
     * [Scenario.FREE_DRIVING]. Safe to call repeatedly: any in-flight observation is cancelled first.
     *
     * @param scenario the screen's current scenario.
     * @param locationProvider the active location provider sourcing the driving signal.
     * @param onStartFreeDriving invoked when driving begins from [Scenario.HOME].
     * @param onStopFreeDriving invoked when driving ends while in [Scenario.FREE_DRIVING].
     */
    fun startDetecting(
        scenario: Scenario,
        locationProvider: LocationProvider,
        onStartFreeDriving: () -> Unit,
        onStopFreeDriving: () -> Unit,
    ) {
        if (scenario != HOME && scenario != FREE_DRIVING) return
        stopDetecting()
        detectionJob = scope.launch {
            freeDrivingManager.getIsDrivingFlow(locationProvider).collect { isDriving ->
                Log.d(TAG, "isDriving: $isDriving")
                when {
                    isDriving && scenario == HOME -> onStartFreeDriving()
                    !isDriving && scenario == FREE_DRIVING -> {
                        onStopFreeDriving()
                        cancel()
                    }
                }
            }
        }
    }

    /** Cancels driving-state observation, if any is running. */
    fun stopDetecting() {
        detectionJob?.cancel()
    }

    private companion object {
        private const val TAG = "FreeDrivingDetector"
    }
}
