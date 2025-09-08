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

package com.example.application.horizon.element

import com.example.R
import com.example.application.horizon.element.UpcomingHorizonElements.HorizonElement
import com.tomtom.quantity.Distance
import com.tomtom.sdk.navigation.horizon.annotation.BetaHorizonTrafficElementApi
import com.tomtom.sdk.navigation.horizon.elements.traffic.TrafficElement
import com.tomtom.sdk.traffic.common.BetaCoreTrafficEventApi

data class Traffic
    @OptIn(
        BetaHorizonTrafficElementApi::class,
        BetaCoreTrafficEventApi::class,
    )
    constructor(
        override val distance: Distance?,
        val element: TrafficElement?,
    ) : HorizonElement(distance) {
        override val iconResource: Int = R.drawable.traffic_jam_24px

        override val descriptionResource: Int = if ((distance?.inMeters() ?: -1.0) >= 0.0) {
            R.string.horizon_label_traffic_card_description
        } else {
            R.string.horizon_label_traffic_card_description_without_distance
        }

        override val fallbackDelayResId: Int = R.string.horizon_label_traffic_delay_unknown

        @OptIn(BetaHorizonTrafficElementApi::class, BetaCoreTrafficEventApi::class)
        override val delayMinutes: Long? = element?.trafficEvent?.delay?.inWholeMinutes
    }
