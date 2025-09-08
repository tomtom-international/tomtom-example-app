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

package com.example.application.map.ui

import androidx.compose.runtime.Composable
import com.example.application.common.ui.getPinMarkerProperties
import com.example.application.map.model.MapCallbacks
import com.example.application.map.model.MapEnvironment
import com.example.application.map.model.MapScreenAction.ShowPoiFocus
import com.example.application.map.model.MapScreenAction.ShowSearchResultFocus
import com.example.application.map.model.MapScreenAction.StartInteractiveMode
import com.example.application.map.model.MapScreenUiState
import com.example.application.map.model.Scenario.DESTINATION_ARRIVAL
import com.example.application.map.model.Scenario.FREE_DRIVING
import com.example.application.map.model.Scenario.GUIDANCE
import com.example.application.map.model.Scenario.HOME
import com.example.application.map.model.Scenario.POI_FOCUS
import com.example.application.search.getPoiIcon
import com.tomtom.sdk.map.display.compose.TomTomMap
import com.tomtom.sdk.map.display.compose.model.MarkerData
import com.tomtom.sdk.map.display.compose.nodes.CurrentLocationMarker
import com.tomtom.sdk.map.display.compose.nodes.Marker
import com.tomtom.sdk.map.display.compose.nodes.Traffic
import com.tomtom.sdk.map.display.compose.properties.CurrentLocationMarkerProperties
import com.tomtom.sdk.map.display.compose.state.rememberMarkerState
import com.tomtom.sdk.map.display.compose.state.rememberTrafficState
import com.tomtom.sdk.map.display.location.LocationMarkerOptions
import com.tomtom.sdk.map.display.visualization.navigation.compose.BetterRouteVisualization
import com.tomtom.sdk.map.display.visualization.navigation.compose.NavigationVisualization
import com.tomtom.sdk.map.display.visualization.navigation.compose.state.rememberBetterRouteVisualizationState
import com.tomtom.sdk.map.display.visualization.routing.compose.TrafficVisualization
import com.tomtom.sdk.map.display.visualization.routing.compose.state.rememberTrafficVisualizationState

/**
 * Renders the TomTom map for MapScreen.
 *
 * Shows traffic, current location, POI/destination markers, and navigation visualizations,
 * and forwards map gestures as screen actions.
 */
@Composable
fun MapView(
    mapEnvironment: MapEnvironment,
    mapScreenUiState: MapScreenUiState,
    mapCallbacks: MapCallbacks,
) {
    TomTomMap(
        infrastructure = mapEnvironment.mapDisplayInfrastructure,
        state = mapEnvironment.mapViewState,
        onMapLongClick = { destination ->
            if (mapScreenUiState.scenario != GUIDANCE) {
                mapCallbacks.onDispatchMapScreenAction(ShowPoiFocus(destination))
            }
        },
        onMapDoubleClickListener = { mapCallbacks.onDispatchMapScreenAction(StartInteractiveMode) },
        onMapPanningListener = { mapCallbacks.onDispatchMapScreenAction(StartInteractiveMode) },
    ) {
        Traffic(
            state = rememberTrafficState(
                showTrafficFlow = false,
                showTrafficIncidents = true,
            ),
        )
        CurrentLocationMarker(
            CurrentLocationMarkerProperties {
                type = LocationMarkerOptions.Type.Chevron
            },
        )

        if (mapScreenUiState.poiPlaces.isNotEmpty() &&
            mapScreenUiState.scenario in setOf(HOME, FREE_DRIVING)
        ) {
            mapScreenUiState.poiPlaces.forEach { placeDetails ->
                Marker(
                    data = MarkerData(geoPoint = placeDetails.place.coordinate),
                    properties = getPinMarkerProperties(
                        getPoiIcon(placeDetails.place.details?.categoryIds?.elementAt(0)?.standard),
                    ),
                    state = rememberMarkerState(),
                    onClick = { mapCallbacks.onDispatchMapScreenAction(ShowSearchResultFocus(placeDetails)) },
                )
            }
        }

        if (mapScreenUiState.destinationMarker != null &&
            mapScreenUiState.scenario in setOf(POI_FOCUS, DESTINATION_ARRIVAL)
        ) {
            Marker(
                data = MarkerData(geoPoint = mapScreenUiState.destinationMarker),
                properties = getPinMarkerProperties(
                    getPoiIcon(mapScreenUiState.placeDetails?.place?.details?.categoryIds?.elementAt(0)?.standard),
                ),
                state = rememberMarkerState(),
            )
        }

        NavigationVisualization(
            infrastructure = mapEnvironment.navigationVisualizationInfrastructure,
        ) {
            TrafficVisualization(
                state = rememberTrafficVisualizationState(
                    trafficIncidentsEnabled = true,
                ),
            )
            BetterRouteVisualization(
                state = rememberBetterRouteVisualizationState(
                    enabled = true,
                ),
            )
        }
    }
}
