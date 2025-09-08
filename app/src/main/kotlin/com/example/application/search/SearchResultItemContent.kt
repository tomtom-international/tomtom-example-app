/*
 * © 2025 TomTom NV. All rights reserved.
 *
 * This software is the proprietary copyright of TomTom NV and its subsidiaries and may be
 * used for internal evaluation purposes or commercial use strictly subject to separate
 * license agreement between you and TomTom NV. If you are the licensee, you are only permitted
 * to use this software in accordance with the terms of your license agreement. If you are
 * not the licensee, you are not authorized to use this software in any manner and should
 * immediately return or destroy it.
 */

package com.example.application.search

import androidx.annotation.DrawableRes
import com.example.R
import com.example.application.common.PlaceDetails
import com.example.application.common.formatDistance
import com.tomtom.sdk.location.BetaLocationApi
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.search.BetaEvSearchApi
import com.tomtom.sdk.search.BetaNearbyEvSearchApi
import com.tomtom.sdk.search.model.result.EvSearchResult
import com.tomtom.sdk.search.model.result.SearchResult

data class SearchResultItemContent(
    @DrawableRes val iconId: Int,
    val placeDetails: PlaceDetails,
    val distance: String,
)

@OptIn(BetaLocationApi::class)
fun SearchResult.toSearchResultItemContent() = SearchResultItemContent(
    iconId = poi?.let {
        getPoiIcon(it.categoryIds.elementAt(0).standard)
    } ?: R.drawable.location_on_24px,
    placeDetails = poi?.let {
        PlaceDetails(place, it)
    } ?: PlaceDetails(place),
    distance = distance?.let { formatDistance(it) } ?: "",
)

@OptIn(BetaEvSearchApi::class, BetaLocationApi::class, BetaNearbyEvSearchApi::class)
fun EvSearchResult.toSearchResultItemContent(geoBias: GeoPoint? = null) = SearchResultItemContent(
    iconId = getPoiIcon(poi.categoryIds.elementAt(0).standard),
    placeDetails = PlaceDetails(place, poi, accessType, nearbyPoiCategories),
    distance = geoBias?.let { formatDistance(place.coordinate.distanceTo(it)) } ?: "",
)
