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

package com.example.application.common.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
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
