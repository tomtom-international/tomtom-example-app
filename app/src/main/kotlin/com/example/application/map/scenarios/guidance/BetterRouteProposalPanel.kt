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

package com.example.application.map.scenarios.guidance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.application.common.formatDistance
import com.example.application.common.ui.fillMaxWidthByOrientation
import com.example.application.ui.theme.NavSdkExampleTheme
import com.tomtom.quantity.Distance
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/** Offers the pending better route proposal to the driver.*/
@Composable
fun BetterRouteProposalPanel(
    proposalFlow: StateFlow<BetterRouteProposal?>,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
    isDeviceInLandscape: Boolean,
    modifier: Modifier = Modifier,
) {
    val proposal by proposalFlow.collectAsStateWithLifecycle()

    proposal?.let {
        BetterRouteProposalCard(
            timeSaved = it.timeSaved,
            remainingDistance = it.route.summary.length,
            onAccept = onAccept,
            onDismiss = onDismiss,
            isDeviceInLandscape = isDeviceInLandscape,
            modifier = modifier,
        )
    }
}

/** @param remainingDistance How far is left to drive once the proposed route is taken. */
@Composable
private fun BetterRouteProposalCard(
    timeSaved: Duration,
    remainingDistance: Distance,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
    isDeviceInLandscape: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidthByOrientation(isDeviceInLandscape)
            .clip(RoundedCornerShape(20.dp))
            .background(color = MaterialTheme.colorScheme.surface)
            .padding(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.tt_asset_icon_route_line_32),
                contentDescription = stringResource(R.string.guidance_better_route_content_description_icon),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp),
            )
            Text(
                text = timeSavedLabel(timeSaved),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
            )
            Text(
                text = formatDistance(remainingDistance),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onAccept, modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.guidance_better_route_button_accept),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.guidance_better_route_button_dismiss),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

@Composable
private fun timeSavedLabel(timeSaved: Duration): String = if (timeSaved >= MIN_DISPLAYED_TIME_SAVED) {
    stringResource(R.string.guidance_better_route_label_time_saved, timeSaved.inWholeMinutes)
} else {
    stringResource(R.string.guidance_better_route_label_better_route)
}

private val MIN_DISPLAYED_TIME_SAVED = 1.minutes

private val PREVIEW_REMAINING_DISTANCE = Distance.kilometers(12)

@PreviewLightDark
@Composable
private fun BetterRouteProposalPanelPreview() {
    NavSdkExampleTheme {
        BetterRouteProposalCard(
            timeSaved = 6.minutes,
            remainingDistance = PREVIEW_REMAINING_DISTANCE,
            onAccept = {},
            onDismiss = {},
            isDeviceInLandscape = false,
        )
    }
}

@PreviewLightDark
@Composable
private fun BetterRouteProposalPanelBelowAMinutePreview() {
    NavSdkExampleTheme {
        BetterRouteProposalCard(
            timeSaved = Duration.ZERO,
            remainingDistance = PREVIEW_REMAINING_DISTANCE,
            onAccept = {},
            onDismiss = {},
            isDeviceInLandscape = false,
        )
    }
}
