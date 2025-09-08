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

package com.example.application.poifocus

import androidx.compose.runtime.Stable
import com.example.application.common.PlaceDetails
import com.example.application.ui.RecenterMapStateHolder
import com.tomtom.sdk.map.display.camera.CameraOptions

@Stable
data class PoiFocusStateHolder(
    val recenterMapStateHolder: RecenterMapStateHolder,
    val placeDetails: PlaceDetails?,
    val onAnimateCamera: (CameraOptions) -> Unit,
    val onClearClick: () -> Unit,
    val onRouteButtonClick: () -> Unit,
    val isDeviceInLandscape: Boolean,
    val onSafeAreaTopPaddingUpdate: (Int) -> Unit,
    val onSafeAreaBottomPaddingUpdate: (Int) -> Unit,
)
