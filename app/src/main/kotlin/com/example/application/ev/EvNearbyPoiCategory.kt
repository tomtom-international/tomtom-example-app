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

package com.example.application.ev

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.R
import com.tomtom.sdk.location.AlphaStandardCategoryIdApi
import com.tomtom.sdk.location.poi.StandardCategoryId

class EvNearbyPoiCategory private constructor(
    @DrawableRes val imageVector: Int,
    @StringRes val nearbyCategoryDescription: Int,
) {
    companion object {
        private val nearbyCategoryProperties = hashMapOf(
            StandardCategoryId.Restaurant to Pair(
                R.drawable.restaurant_24px,
                R.string.search_content_description_restaurant_poi,
            ),
            StandardCategoryId.HotelMotel to Pair(
                R.drawable.hotel_24px,
                R.string.search_content_description_hotel_poi,
            ),
            StandardCategoryId.CafePub to Pair(
                R.drawable.outline_cafe_24px,
                R.string.search_content_description_cafe_poi,
            ),
            StandardCategoryId.PublicAmenity to Pair(
                R.drawable.outline_family_restroom_24px,
                R.string.search_content_description_public_amenity_poi,
            ),
            StandardCategoryId.Shop to Pair(
                R.drawable.outline_shopping_bag_24px,
                R.string.search_content_description_shop_poi,
            ),
            StandardCategoryId.ParkRecreationArea to Pair(
                R.drawable.outline_park_24px,
                R.string.search_content_description_park_poi,
            ),
        )

        @OptIn(AlphaStandardCategoryIdApi::class)
        fun fromType(nearbyCategory: StandardCategoryId): EvNearbyPoiCategory {
            val (imageVector, accessTypeDescription) =
                nearbyCategoryProperties[nearbyCategory]
                    ?: nearbyCategoryProperties[nearbyCategory.parent]
                    ?: Pair(
                        R.drawable.outline_star_24px,
                        R.string.demo_search_ev_label_unknown_poi,
                    )
            return EvNearbyPoiCategory(imageVector, accessTypeDescription)
        }
    }
}
