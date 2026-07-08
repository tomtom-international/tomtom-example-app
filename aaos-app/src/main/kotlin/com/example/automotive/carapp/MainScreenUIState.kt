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

/**
 * UI state for the main screen.
 *
 * @param sdkInitialized Whether the TomTom SDK has finished initializing
 * @param initializationError SDK initialization error message, null if no error
 * @param permissionsGranted Whether required runtime permissions have been granted
 * @param isLoading Whether route planning is in progress
 * @param hasRoutes Whether routes are currently available
 */
data class MainScreenUIState(
    val sdkInitialized: Boolean = false,
    val initializationError: String? = null,
    val permissionsGranted: Boolean = false,
    val isLoading: Boolean = false,
    val hasRoutes: Boolean = false,
)
