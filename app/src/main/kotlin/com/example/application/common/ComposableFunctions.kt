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

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun isDeviceInLandscape(): Boolean {
    val orientation = LocalConfiguration.current.orientation
    return remember(orientation) { orientation == Configuration.ORIENTATION_LANDSCAPE }
}

@Composable
fun Modifier.fillMaxWidthByOrientation(isDeviceInLandscape: Boolean) = if (isDeviceInLandscape) {
    fillMaxWidth(fraction = 0.5f)
} else {
    fillMaxWidth()
}

@Composable
@ReadOnlyComposable
fun safeAreaStartPadding(isDeviceInLandscape: Boolean): Dp = if (isDeviceInLandscape) {
    LocalDensity.current.run { (LocalWindowInfo.current.containerSize.width.toDp() / 2) }
} else {
    0.dp
}
