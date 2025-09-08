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

package com.example.application.common

import androidx.annotation.DrawableRes
import androidx.compose.ui.geometry.Offset
import com.example.R
import com.tomtom.sdk.map.display.compose.BetaMapComposableApi
import com.tomtom.sdk.map.display.compose.properties.MarkerProperties
import com.tomtom.sdk.map.display.image.ImageFactory

private const val TT_PIN_MARKER_X_OFFSET = 0.5f
private const val TT_PIN_MARKER_Y_OFFSET = 0.45f

@OptIn(BetaMapComposableApi::class)
fun getPinMarkerProperties(
    @DrawableRes pinIconImageRes: Int,
): MarkerProperties = MarkerProperties(pinImage = ImageFactory.fromResource(R.drawable.tt_pin)) {
    pinIconImage = ImageFactory.fromResource(pinIconImageRes)
    pinIconAnchor = Offset(TT_PIN_MARKER_X_OFFSET, TT_PIN_MARKER_Y_OFFSET)
}
