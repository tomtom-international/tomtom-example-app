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

package com.example.application.freedriving

import com.example.application.horizon.element.UpcomingHorizonElements
import com.example.application.search.SearchStateHolder
import com.example.application.ui.RecenterMapStateHolder
import com.tomtom.sdk.navigation.locationcontext.LocationContext
import kotlinx.coroutines.flow.StateFlow

@Suppress("detekt:LongParameterList")
class FreeDrivingStateHolder(
    val recenterMapStateHolder: RecenterMapStateHolder,
    val searchStateHolder: SearchStateHolder,
    val locationContext: StateFlow<LocationContext?>,
    val isDeviceInLandscape: Boolean,
    val onSafeAreaTopPaddingUpdate: (Int) -> Unit,
    val onSafeAreaBottomPaddingUpdate: (Int) -> Unit,
    val horizonElementsFlow: StateFlow<UpcomingHorizonElements?>,
)
