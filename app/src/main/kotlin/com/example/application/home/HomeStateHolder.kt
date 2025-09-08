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

package com.example.application.home

import androidx.compose.runtime.Stable
import com.example.application.common.PlaceDetails
import com.example.application.search.SearchStateHolder
import com.example.application.ui.RecenterMapStateHolder
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.map.display.camera.CameraOptions
import kotlinx.coroutines.flow.StateFlow

@Stable
data class HomeStateHolder(
    val locationUpdates: StateFlow<GeoPoint?>,
    val poiPlaces: List<PlaceDetails>,
    val recenterMapStateHolder: RecenterMapStateHolder,
    val onAnimateCamera: (CameraOptions) -> Unit,
    val searchStateHolder: SearchStateHolder,
    val isDeviceInLandscape: Boolean,
    val onSafeAreaTopPaddingUpdate: (Int) -> Unit,
    val onSafeAreaBottomPaddingUpdate: (Int) -> Unit,
    val onSettingsClick: () -> Unit,
)
