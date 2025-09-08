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
import com.tomtom.sdk.location.poi.ev.AccessType

/**
 * Wraps an EV station access type with a drawable icon and a localized description.
 */
class EvAccessType private constructor(
    val accessType: AccessType,
    @DrawableRes val imageVector: Int,
    @StringRes val accessTypeDescription: Int,
) {
    companion object {
        private val accessTypeProperties = hashMapOf(
            AccessType.Public to Pair(
                R.drawable.outline_public_24px,
                R.string.demo_search_ev_label_access_type_option_public,
            ),
            AccessType.Private to Pair(
                R.drawable.outline_lock_24px,
                R.string.demo_search_ev_label_access_type_option_private,
            ),
            AccessType.Restricted to Pair(
                R.drawable.outline_passkey_24px,
                R.string.demo_search_ev_label_access_type_option_restricted,
            ),
            AccessType.Company to Pair(
                R.drawable.tt_asset_icon_workadded_line_32,
                R.string.demo_search_ev_label_access_type_option_company,
            ),
        )

        fun fromType(accessType: AccessType): EvAccessType {
            val (imageVector, accessTypeDescription) = accessTypeProperties[accessType]
                ?: Pair(
                    R.drawable.tt_asset_icon_star_line_32,
                    R.string.demo_search_ev_label_unknown_access,
                )
            return EvAccessType(accessType, imageVector, accessTypeDescription)
        }

        val Public = fromType(AccessType.Public)
        val Private = fromType(AccessType.Private)
        val Restricted = fromType(AccessType.Restricted)
        val Company = fromType(AccessType.Company)
    }
}
