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

package com.example.automotive.carapp

import com.tomtom.sdk.routing.route.Route

/**
 * UI state for the main screen.
 *
 * @param sdkInitialized Whether the TomTom SDK has finished initializing
 * @param initializationError SDK initialization error message, null if no error
 * @param permissionsGranted Whether required runtime permissions have been granted
 * @param isLoading Whether route planning is in progress
 * @param routes The list of planned routes; empty if no routes are available
 * @param selectedRoute The currently selected route, or null if none
 * @param locationEnabled Whether location services are enabled on the device
 */
data class MainScreenUIState(
    val sdkInitialized: Boolean = false,
    val initializationError: String? = null,
    val permissionsGranted: Boolean = false,
    val isLoading: Boolean = false,
    val routes: List<Route> = emptyList(),
    val selectedRoute: Route? = null,
    val locationEnabled: Boolean = true,
)
