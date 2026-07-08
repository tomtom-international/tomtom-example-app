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
import androidx.compose.runtime.rememberCoroutineScope
import com.example.application.common.extension.poiName
import com.example.application.map.model.MapCallbacks
import com.example.application.map.model.MapEnvironment
import com.example.application.map.model.MapScreenAction.ShowPoiFocus
import com.example.application.map.model.MapScreenAction.ShowRenderedPoiInfo
import com.example.application.map.model.MapScreenAction.ShowSearchResultFocus
import com.example.application.map.model.MapScreenAction.StartInteractiveMode
import com.example.application.map.model.MapScreenUiState
import com.example.application.map.model.Scenario.GUIDANCE
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.map.display.compose.TomTomMap
import com.tomtom.sdk.map.display.compose.nodes.CurrentLocationMarker
import com.tomtom.sdk.map.display.compose.nodes.Traffic
import com.tomtom.sdk.map.display.compose.properties.CurrentLocationMarkerProperties
import com.tomtom.sdk.map.display.compose.state.rememberTrafficState
import com.tomtom.sdk.map.display.location.LocationMarkerOptions
import com.tomtom.sdk.map.display.renderedfeature.RenderedFeatureQueryOptions
import com.tomtom.sdk.map.display.visualization.navigation.compose.BetterRouteVisualization
import com.tomtom.sdk.map.display.visualization.navigation.compose.NavigationVisualization
import com.tomtom.sdk.map.display.visualization.navigation.compose.state.rememberBetterRouteVisualizationState
import com.tomtom.sdk.map.display.visualization.routing.compose.TrafficVisualization
import com.tomtom.sdk.map.display.visualization.routing.compose.state.rememberTrafficVisualizationState
import kotlinx.coroutines.launch

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
    val coroutineScope = rememberCoroutineScope()
    TomTomMap(
        infrastructure = mapEnvironment.mapDisplayInfrastructure,
        state = mapEnvironment.mapViewState,
        onMapClick = { geoPoint ->
            if (mapScreenUiState.scenario != GUIDANCE) {
                coroutineScope.launch {
                    showRenderedPoiInfo(geoPoint, mapEnvironment, mapCallbacks)
                }
            }
        },
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

        MapViewPlaces(
            mapScreenUiState = mapScreenUiState,
            onPoiClick = { placeDetails ->
                mapCallbacks.onDispatchMapScreenAction(ShowSearchResultFocus(placeDetails))
            },
        )

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

private suspend fun showRenderedPoiInfo(
    geoPoint: GeoPoint,
    mapEnvironment: MapEnvironment,
    mapCallbacks: MapCallbacks,
) {
    val features = mapEnvironment.mapViewState.renderedFeatureState
        .getRenderedFeatures(
            geoPoint,
            RenderedFeatureQueryOptions(layerIds = POI_LAYER_IDS),
        )
    if (features.isNotEmpty()) {
        val poiName = features.first().poiName
        mapCallbacks.onDispatchMapScreenAction(ShowRenderedPoiInfo(geoPoint, poiName))
    }
}

private val POI_LAYER_IDS = listOf("POI", "POI - Micro")
