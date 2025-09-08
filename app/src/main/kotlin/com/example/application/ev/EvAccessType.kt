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
import com.tomtom.sdk.location.BetaLocationApi
import com.tomtom.sdk.location.poi.ev.AccessType

@OptIn(BetaLocationApi::class)
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
                R.drawable.outline_work_24px,
                R.string.demo_search_ev_label_access_type_option_company,
            ),
        )

        fun fromType(accessType: AccessType): EvAccessType {
            val (imageVector, accessTypeDescription) = accessTypeProperties[accessType]
                ?: Pair(
                    R.drawable.outline_star_24px,
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
