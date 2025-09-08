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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.application.common.SEARCH_BOTTOMSHEET_PEEK_HEIGHT
import com.example.application.common.fillMaxWidthByOrientation
import com.example.application.horizon.HorizonCard
import com.example.application.search.SearchBottomSheet
import com.example.application.ui.RecenterMapButton
import com.example.application.ui.Speedometer

@Composable
fun BoxScope.FreeDrivingUiComponents(stateHolder: FreeDrivingStateHolder) {
    HorizonElementsTopPanel(stateHolder)

    val currentDensity = LocalDensity.current
    LaunchedEffect(Unit) {
        stateHolder.onSafeAreaBottomPaddingUpdate(
            with(currentDensity) {
                if (stateHolder.isDeviceInLandscape) 0 else (SEARCH_BOTTOMSHEET_PEEK_HEIGHT).dp.toPx().toInt()
            },
        )
    }

    if (stateHolder.isDeviceInLandscape) {
        Row(
            modifier = Modifier.align(Alignment.BottomStart),
            verticalAlignment = Alignment.Bottom,
        ) {
            SearchBottomSheet(searchStateHolder = stateHolder.searchStateHolder)

            OverlayUi(stateHolder, modifier = Modifier.padding(16.dp))
        }
    } else {
        Box(contentAlignment = Alignment.BottomStart) {
            OverlayUi(
                stateHolder = stateHolder,
                modifier = Modifier.padding(
                    start = 16.dp,
                    bottom = (SEARCH_BOTTOMSHEET_PEEK_HEIGHT).dp + 16.dp,
                ),
            )

            SearchBottomSheet(searchStateHolder = stateHolder.searchStateHolder)
        }
    }
}

@Composable
private fun HorizonElementsTopPanel(stateHolder: FreeDrivingStateHolder) {
    val horizonElements by stateHolder.horizonElementsFlow.collectAsStateWithLifecycle()

    horizonElements?.let { upcomingHorizonElements ->
        val elements = listOfNotNull(
            upcomingHorizonElements.trafficElement,
            upcomingHorizonElements.safetyLocationElement,
        ).sortedBy { it.distance }
        if (elements.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidthByOrientation(stateHolder.isDeviceInLandscape)
                    .onGloballyPositioned {
                        stateHolder.onSafeAreaTopPaddingUpdate(it.size.height)
                    }
                    .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(
                        top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                    ),
            ) {
                elements.forEachIndexed { index, horizonElement ->
                    HorizonCard(
                        horizonElement = horizonElement,
                        isBottomCard = index == elements.lastIndex,
                    )
                }
            }
        }
    }
}

@Composable
private fun OverlayUi(
    stateHolder: FreeDrivingStateHolder,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        RecenterMapButton(stateHolder = stateHolder.recenterMapStateHolder)

        Spacer(modifier = Modifier.height(16.dp))

        Speedometer(stateHolder.locationContext, stateHolder.isDeviceInLandscape)
    }
}
