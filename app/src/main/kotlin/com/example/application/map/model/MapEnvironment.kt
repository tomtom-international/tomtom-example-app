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

package com.example.application.map.model

import com.tomtom.sdk.map.display.compose.model.MapDisplayInfrastructure
import com.tomtom.sdk.map.display.compose.state.MapViewState
import com.tomtom.sdk.map.display.visualization.navigation.compose.model.NavigationVisualizationInfrastructure

/**
 * Environment dependencies and device configuration required by the map screen.
 *
 * @param mapDisplayInfrastructure: map rendering infrastructure.
 * @param navigationVisualizationInfrastructure: navigation visualization infrastructure.
 * @param mapViewState: the single source of truth for the MapView state across the map subtree.
 * @param isDeviceInLandscape: whether the device is currently in landscape orientation.
 */
data class MapEnvironment(
    val mapDisplayInfrastructure: MapDisplayInfrastructure,
    val navigationVisualizationInfrastructure: NavigationVisualizationInfrastructure,
    val mapViewState: MapViewState,
    val isDeviceInLandscape: Boolean,
)
