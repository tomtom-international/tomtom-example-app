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

package com.example.demo.search.ev

import androidx.compose.runtime.Composable
import com.example.R
import com.example.application.common.ui.getPinMarkerProperties
import com.example.application.search.SearchResultItemContent
import com.tomtom.sdk.map.display.compose.TomTomMapComposable
import com.tomtom.sdk.map.display.compose.model.MarkerData
import com.tomtom.sdk.map.display.compose.nodes.Marker
import com.tomtom.sdk.map.display.compose.state.rememberMarkerState

@Composable
@TomTomMapComposable
fun PlacesEvSearch(
    searchResults: List<SearchResultItemContent>,
    onSearchResultClick: (SearchResultItemContent) -> Unit,
) {
    searchResults.forEach { searchResult ->
        Marker(
            data = MarkerData(geoPoint = searchResult.placeDetails.place.coordinate),
            properties = getPinMarkerProperties(R.drawable.tt_asset_icon_evcharger_fill_32),
            state = rememberMarkerState(),
            onClick = { onSearchResultClick(searchResult) },
        )
    }
}
