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

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.application.horizon.element.UpcomingHorizonElements.HorizonElement

@Composable
internal fun HorizonElement.delayText(): String? = delayMinutes?.let { mins ->
    if (mins < 1) {
        stringResource(R.string.horizon_label_traffic_delay_under_a_minute)
    } else {
        stringResource(R.string.horizon_label_traffic_delay_minutes, mins.toInt())
    }
} ?: fallbackDelayResId?.let { stringResource(it) }
