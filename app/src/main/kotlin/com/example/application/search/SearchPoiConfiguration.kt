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
import androidx.annotation.StringRes
import com.example.R
import com.tomtom.sdk.location.AlphaStandardCategoryIdApi
import com.tomtom.sdk.location.poi.StandardCategoryId

data class SearchPoiConfiguration(
    @DrawableRes val imageVector: Int,
    @StringRes val contentDescription: Int,
)

@OptIn(AlphaStandardCategoryIdApi::class)
@DrawableRes
fun getPoiIcon(categoryId: StandardCategoryId?): Int = poiOptions.getOrDefault(categoryId, null)?.imageVector
    ?: poiOptions.getOrDefault(categoryId?.parent, null)?.imageVector ?: R.drawable.location_on_24px

@StringRes
fun getPoiDescriptionString(categoryId: StandardCategoryId): Int =
    poiOptions.getOrDefault(categoryId, null)?.contentDescription
        ?: R.string.search_content_description_search_result

val poiOptions: HashMap<StandardCategoryId, SearchPoiConfiguration> = hashMapOf(
    // Gas Station Poi configuration
    StandardCategoryId.GasStation to SearchPoiConfiguration(
        R.drawable.local_gas_station_24px,
        R.string.search_content_description_gas_station_poi,
    ),
    // Parking Poi configuration
    StandardCategoryId.ParkingGarage to SearchPoiConfiguration(
        R.drawable.local_parking_24px,
        R.string.search_content_description_parking_poi,
    ),
    // Restaurant Poi configuration
    StandardCategoryId.Restaurant to SearchPoiConfiguration(
        R.drawable.restaurant_24px,
        R.string.search_content_description_restaurant_poi,
    ),
    // Hotel Poi configuration
    StandardCategoryId.Hotel to SearchPoiConfiguration(
        R.drawable.hotel_24px,
        R.string.search_content_description_hotel_poi,
    ),
)
