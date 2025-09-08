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

package com.example.application.map.scenarios.freedriving

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
import com.example.application.common.ui.RecenterMapButton
import com.example.application.common.ui.Speedometer
import com.example.application.common.ui.fillMaxWidthByOrientation
import com.example.application.horizon.HorizonCard
import com.example.application.search.SearchBottomSheet

/**
 * Renders the Free Driving scenario UI with horizon alerts, speedometer, recenter button, and
 * a search bottom sheet, responsive to device orientation and safe‑area padding.
 */
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
