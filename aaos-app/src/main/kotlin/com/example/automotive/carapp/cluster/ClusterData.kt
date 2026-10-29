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

package com.example.automotive.carapp.cluster

import com.tomtom.sdk.routing.route.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Data shared with the cluster screen.
 */
class ClusterData {
    private val _isActiveGuidance = MutableStateFlow(false)
    val isActiveGuidance: StateFlow<Boolean> = _isActiveGuidance.asStateFlow()

    private val _clusterRoutes = MutableStateFlow<List<Route>>(emptyList())
    val clusterRoutes: StateFlow<List<Route>> = _clusterRoutes.asStateFlow()

    private val _clusterSelectedRoute = MutableStateFlow<Route?>(null)
    val clusterSelectedRoute: StateFlow<Route?> = _clusterSelectedRoute.asStateFlow()

    fun activateGuidance(
        routes: List<Route>,
        selectedRoute: Route,
    ) {
        _isActiveGuidance.value = true
        _clusterRoutes.value = routes
        _clusterSelectedRoute.value = selectedRoute
    }

    fun deactivateGuidance() {
        _isActiveGuidance.value = false
        _clusterRoutes.value = emptyList()
        _clusterSelectedRoute.value = null
    }
}
