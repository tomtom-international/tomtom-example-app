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

package com.example.application.search.ev.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.R
import com.tomtom.sdk.location.poi.StandardCategoryId

/**
 * Maps a nearby standard POI category to a drawable icon and a localized content description.
 */
class EvNearbyPoiCategory private constructor(
    @DrawableRes val imageVector: Int,
    @StringRes val nearbyCategoryDescription: Int,
) {
    companion object {
        private val nearbyCategoryProperties = hashMapOf(
            StandardCategoryId.Restaurant to Pair(
                R.drawable.tt_asset_icon_restaurant_fill_32,
                R.string.search_content_description_restaurant_poi,
            ),
            StandardCategoryId.HotelMotel to Pair(
                R.drawable.tt_asset_icon_hotelmotel_fill_32,
                R.string.search_content_description_hotel_poi,
            ),
            StandardCategoryId.CafePub to Pair(
                R.drawable.tt_asset_icon_cafe_line_32,
                R.string.search_content_description_cafe_poi,
            ),
            StandardCategoryId.PublicAmenity to Pair(
                R.drawable.tt_asset_icon_restroom_line_32,
                R.string.search_content_description_public_amenity_poi,
            ),
            StandardCategoryId.Shop to Pair(
                R.drawable.tt_asset_icon_shop_line_32,
                R.string.search_content_description_shop_poi,
            ),
            StandardCategoryId.ParkRecreationArea to Pair(
                R.drawable.tt_asset_icon_park_line_32,
                R.string.search_content_description_park_poi,
            ),
        )

        fun fromType(nearbyCategory: StandardCategoryId): EvNearbyPoiCategory {
            val (imageVector, accessTypeDescription) =
                nearbyCategoryProperties[nearbyCategory]
                    ?: Pair(
                        R.drawable.tt_asset_icon_star_line_32,
                        R.string.demo_search_ev_label_unknown_poi,
                    )
            return EvNearbyPoiCategory(imageVector, accessTypeDescription)
        }
    }
}
