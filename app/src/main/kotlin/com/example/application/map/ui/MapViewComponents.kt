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
import com.example.application.common.PlaceDetails
import com.example.application.common.ui.getPinMarkerProperties
import com.example.application.map.model.MapScreenUiState
import com.example.application.map.model.Scenario.DESTINATION_ARRIVAL
import com.example.application.map.model.Scenario.FREE_DRIVING
import com.example.application.map.model.Scenario.HOME
import com.example.application.map.model.Scenario.POI_FOCUS
import com.example.application.search.getPoiIcon
import com.tomtom.sdk.map.display.compose.TomTomMapComposable
import com.tomtom.sdk.map.display.compose.model.MarkerData
import com.tomtom.sdk.map.display.compose.nodes.Marker
import com.tomtom.sdk.map.display.compose.state.rememberMarkerState

@Composable
@TomTomMapComposable
fun MapViewPlaces(
    mapScreenUiState: MapScreenUiState,
    onPoiClick: (PlaceDetails) -> Unit,
) {
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
                onClick = { onPoiClick(placeDetails) },
            )
        }
    }

    if (mapScreenUiState.markerCoordinates != null &&
        mapScreenUiState.placeDetails != null &&
        mapScreenUiState.scenario in setOf(POI_FOCUS, DESTINATION_ARRIVAL)
    ) {
        Marker(
            data = MarkerData(geoPoint = mapScreenUiState.markerCoordinates),
            properties = getPinMarkerProperties(
                getPoiIcon(mapScreenUiState.placeDetails.place.details?.categoryIds?.elementAt(0)?.standard),
            ),
            state = rememberMarkerState(),
        )
    }
}
