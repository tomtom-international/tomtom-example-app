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

package com.example.application.horizon

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.application.horizon.element.SafetyLocation
import com.example.application.horizon.element.Traffic
import com.example.application.horizon.element.UpcomingHorizonElements.HorizonElement
import com.example.application.horizon.element.delayText
import com.tomtom.quantity.Distance
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.navigation.horizon.elements.safetylocation.SafetyLocationElement
import com.tomtom.sdk.navigation.horizon.elements.traffic.TrafficElement
import com.tomtom.sdk.safetylocations.model.SafetyLocationId
import com.tomtom.sdk.safetylocations.model.SafetyLocationType
import com.tomtom.sdk.traffic.common.CoreTrafficEvent
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Composable
fun HorizonCard(
    horizonElement: HorizonElement,
    isBottomCard: Boolean,
    modifier: Modifier = Modifier,
) {
    val cardModifier = modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.surface)
        .padding(
            vertical = 8.dp,
            horizontal = 16.dp,
        )

    val horizonCardProperties = createHorizonCardProperties(horizonElement)

    Row(
        modifier = cardModifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(horizonCardProperties.iconRes),
            contentDescription = stringResource(id = R.string.horizon_card_icon),
            modifier = Modifier
                .size(40.dp)
                .padding(end = 8.dp),
            tint = MaterialTheme.colorScheme.primary,
        )

        horizonCardProperties.distance?.let { distance ->
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = distance,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(horizonCardProperties.descriptionRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } ?: Text(
            modifier = Modifier.weight(1f),
            text = stringResource(horizonCardProperties.descriptionRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        horizonCardProperties.delayText?.let { delayText ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.tt_asset_icon_clock_line_32),
                    contentDescription = stringResource(id = R.string.horizon_card_delay_icon),
                    modifier = Modifier
                        .size(20.dp)
                        .padding(end = 4.dp),
                    tint = MaterialTheme.colorScheme.error,
                )
                Text(
                    text = delayText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }

    if (!isBottomCard) {
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 2.dp,
        )
    }
}

private data class HorizonCardProperties(
    val distance: String?,
    val iconRes: Int,
    val descriptionRes: Int,
    val delayText: String?,
)

@Suppress("detekt:CyclomaticComplexMethod")
@Composable
private fun createHorizonCardProperties(horizonElement: HorizonElement): HorizonCardProperties = HorizonCardProperties(
    distance = horizonElement.formattedDistance(),
    iconRes = horizonElement.iconResource,
    descriptionRes = horizonElement.descriptionResource,
    delayText = horizonElement.delayText(),
)

@Suppress("detekt:MagicNumber")
@PreviewLightDark
@Composable
fun TrafficHorizonCardPreview() {
    val mockTrafficElement = Traffic(
        distance = Distance.meters(1200.0),
        element = TrafficElement(
            id = 0,
            pathId = 0,
            startOffset = Distance.meters(1200.0),
            endOffset = Distance.meters(1450.0),
            trafficEvent = CoreTrafficEvent(
                id = "mock_event",
                delay = 5.toDuration(DurationUnit.MINUTES),
            ),
        ),
    )
    HorizonCard(horizonElement = mockTrafficElement, isBottomCard = false)
}

@Suppress("detekt:MagicNumber")
@PreviewLightDark
@Composable
fun SafetyLocationHorizonCardPreview() {
    val mockSafetyLocationElement = SafetyLocation.create(
        distance = Distance.meters(500.0),
        element = SafetyLocationElement(
            id = 0,
            pathId = 0,
            startOffset = Distance.meters(1200.0),
            endOffset = Distance.meters(1450.0),
            safetyLocation = com.tomtom.sdk.safetylocations.model.SafetyLocation(
                id = SafetyLocationId(0),
                speedLimit = null,
                startLocation = GeoPoint(0.0, 0.0),
                type = SafetyLocationType.FixedSpeedCamera,
            ),
        ),
    )

    requireNotNull(mockSafetyLocationElement)
    HorizonCard(horizonElement = mockSafetyLocationElement, isBottomCard = true)
}

@Suppress("detekt:MagicNumber")
@PreviewLightDark
@Composable
fun SafetyLocationRedLightHorizonCardPreview() {
    val mockSafetyLocationElement = SafetyLocation.create(
        distance = Distance.meters(500.0),
        element = SafetyLocationElement(
            id = 0,
            pathId = 0,
            startOffset = Distance.meters(1200.0),
            endOffset = Distance.meters(1450.0),
            safetyLocation = com.tomtom.sdk.safetylocations.model.SafetyLocation(
                id = SafetyLocationId(0),
                speedLimit = null,
                startLocation = GeoPoint(0.0, 0.0),
                type = SafetyLocationType.RedLightCamera,
            ),
        ),
    )
    requireNotNull(mockSafetyLocationElement)
    HorizonCard(horizonElement = mockSafetyLocationElement, isBottomCard = true)
}
