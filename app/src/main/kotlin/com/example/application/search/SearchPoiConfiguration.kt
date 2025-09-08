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
import androidx.annotation.StringRes
import com.example.R
import com.tomtom.sdk.location.poi.StandardCategoryId

data class SearchPoiConfiguration(
    @DrawableRes val imageVector: Int,
    @StringRes val contentDescription: Int,
)

@DrawableRes
fun getPoiIcon(categoryId: StandardCategoryId?): Int = poiOptions[categoryId]?.imageVector
    ?: R.drawable.location_on_24px

@StringRes
fun getPoiDescriptionString(categoryId: StandardCategoryId): Int =
    poiOptions.getOrDefault(categoryId, null)?.contentDescription
        ?: R.string.search_content_description_search_result

val poiOptions: HashMap<StandardCategoryId, SearchPoiConfiguration> = hashMapOf(
    // Gas Station Poi configuration
    StandardCategoryId.GasStation to SearchPoiConfiguration(
        R.drawable.tt_asset_icon_fuel_fill_32,
        R.string.search_content_description_gas_station_poi,
    ),
    // Parking Poi configuration
    StandardCategoryId.ParkingGarage to SearchPoiConfiguration(
        R.drawable.tt_asset_icon_parking_fill_32,
        R.string.search_content_description_parking_poi,
    ),
    // Restaurant Poi configuration
    StandardCategoryId.Restaurant to SearchPoiConfiguration(
        R.drawable.tt_asset_icon_restaurant_fill_32,
        R.string.search_content_description_restaurant_poi,
    ),
    // Hotel Poi configuration
    StandardCategoryId.Hotel to SearchPoiConfiguration(
        R.drawable.tt_asset_icon_hotelmotel_fill_32,
        R.string.search_content_description_hotel_poi,
    ),
)
