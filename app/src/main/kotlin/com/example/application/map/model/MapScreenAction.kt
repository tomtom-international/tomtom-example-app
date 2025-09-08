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

import com.example.application.common.PlaceDetails
import com.example.application.search.SearchResultItemContent
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.navigation.NavigationOptions
import com.tomtom.sdk.routing.RoutingFailure
import com.tomtom.sdk.search.common.error.SearchFailure

/**
 * Map screen intents processed by the ViewModel to mutate map state and trigger navigation/UI updates.
 */
sealed interface MapScreenAction {
    object ClearMap : MapScreenAction

    object ClearSearch : MapScreenAction

    object RecenterMap : MapScreenAction

    object StartInteractiveMode : MapScreenAction

    object ShowRoutePreview : MapScreenAction

    object StopGuidance : MapScreenAction

    data class StartGuidance(
        val navigationOptions: NavigationOptions,
    ) : MapScreenAction

    data class ShowSearchResultFocus(val placeDetails: PlaceDetails) : MapScreenAction

    data class ShowPoiCategorySearchResultFocus(val poiResults: List<SearchResultItemContent>) : MapScreenAction

    data class ShowPoiFocus(val geoPoint: GeoPoint) : MapScreenAction

    data class ShowSearchFailure(val searchFailure: SearchFailure) : MapScreenAction

    data class CleanRoutePreview(val onClearRoutes: () -> Unit) : MapScreenAction

    data class ShowRoutingFailure(val routingFailure: RoutingFailure) : MapScreenAction

    data class ToggleBottomSheet(val isExpanded: Boolean?) : MapScreenAction

    data class ToggleCameraTrackingMode(val checked: Boolean) : MapScreenAction
}
