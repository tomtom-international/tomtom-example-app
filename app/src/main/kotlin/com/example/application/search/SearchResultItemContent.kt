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

package com.example.application.search

import androidx.annotation.DrawableRes
import com.example.R
import com.example.application.common.PlaceDetails
import com.example.application.common.formatDistance
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.search.model.result.EvSearchResult
import com.tomtom.sdk.search.model.result.SearchResult

data class SearchResultItemContent(
    @DrawableRes val iconId: Int,
    val placeDetails: PlaceDetails,
    val distance: String,
)

fun SearchResult.toSearchResultItemContent() = SearchResultItemContent(
    iconId = place.details?.let { getPoiIcon(it.categoryIds.elementAt(0).standard) } ?: R.drawable.location_on_24px,
    placeDetails = PlaceDetails(place),
    distance = distance?.let { formatDistance(it) } ?: "",
)

fun EvSearchResult.toSearchResultItemContent(geoBias: GeoPoint? = null) = SearchResultItemContent(
    iconId = getPoiIcon(place.details?.categoryIds?.elementAt(0)?.standard),
    placeDetails = PlaceDetails(place, accessType, nearbyPoiCategories),
    distance = geoBias?.let { formatDistance(place.coordinate.distanceTo(it)) } ?: "",
)
