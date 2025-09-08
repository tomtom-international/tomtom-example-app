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

@file:Suppress("detekt:TooManyFunctions")

package com.example.demo.search.ev

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.application.common.BOTTOMSHEET_PADDING_TOP
import com.example.application.common.EV_POI_BOTTOMSHEET_PEEK_HEIGHT
import com.example.application.common.PlaceDetails
import com.example.application.common.chargePointAvailability
import com.example.application.common.extension.fadingEdge
import com.example.application.common.locationDetails
import com.example.application.common.name
import com.example.application.common.ui.BottomSheet
import com.example.application.common.ui.fillMaxWidthByOrientation
import com.example.application.search.ev.model.EvAccessType
import com.example.application.search.ev.model.EvConnectorType
import com.example.application.search.ev.model.EvNearbyPoiCategory
import com.tomtom.quantity.Power
import com.tomtom.sdk.location.poi.ev.Status
import com.tomtom.sdk.vehicle.ConnectorType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvPoiFocusBottomSheet(
    placeDetails: PlaceDetails,
    isExpanded: Boolean,
    isDeviceInLandscape: Boolean,
    onBottomSheetExpand: () -> Unit,
    onBottomSheetPartialExpand: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BottomSheet(
        isExpanded = isExpanded,
        sheetPeekHeight = (EV_POI_BOTTOMSHEET_PEEK_HEIGHT).dp,
        onBottomSheetExpand = onBottomSheetExpand,
        onBottomSheetPartialExpand = onBottomSheetPartialExpand,
        isDeviceInLandscape = isDeviceInLandscape,
        modifier = modifier.fillMaxWidthByOrientation(isDeviceInLandscape),
    ) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(
                    min = evPoiFocusBottomSheetHeight,
                    max = evPoiFocusBottomSheetHeight,
                )
                .verticalScroll(state = scrollState),
        ) {
            EvPoiFocusBottomSheetHeader(
                placeDetails = placeDetails,
                isExpanded = isExpanded,
            )

            if (isExpanded) {
                NearbyPoiList(
                    placeDetails = placeDetails,
                    modifier = Modifier,
                )
                EvChargePointsList(
                    placeDetails = placeDetails,
                    modifier = Modifier,
                )
            }
        }
    }
}

private val evPoiFocusBottomSheetHeight
    @Composable
    get() = LocalDensity.current.run { LocalWindowInfo.current.containerSize.height.dp } -
        WindowInsets.statusBars.asPaddingValues().calculateTopPadding() -
        (BOTTOMSHEET_PADDING_TOP).dp

@Suppress("detekt:MagicNumber")
@Composable
private fun EvPoiFocusBottomSheetHeader(
    placeDetails: PlaceDetails,
    isExpanded: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(start = 16.dp)
            .fillMaxWidth(),
    ) {
        Text(
            text = stringResource(R.string.demo_search_ev_label_charger),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.inversePrimary,
            modifier = Modifier.padding(vertical = 2.dp),
        )
        Text(
            text = placeDetails.name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 2.dp),
        )
        Text(
            text = placeDetails.locationDetails,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(vertical = 2.dp),
        )

        placeDetails.accessType?.let {
            Text(
                text = stringResource(
                    R.string.common_format_two_words_with_space,
                    stringResource(EvAccessType.fromType(it).accessTypeDescription),
                    stringResource(R.string.demo_search_ev_label_charger),
                ),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .then(
                        if (!isExpanded) {
                            Modifier.fadingEdge(
                                Brush.verticalGradient(
                                    0.1f to Color.Red,
                                    1f to Color.Transparent,
                                ),
                            )
                        } else {
                            Modifier
                        },
                    ),
            )
        }
    }
}

@Composable
private fun NearbyPoiList(
    placeDetails: PlaceDetails,
    modifier: Modifier,
) {
    var showContent by rememberSaveable { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { showContent = !showContent },
        ) {
            Text(
                text = stringResource(R.string.demo_search_ev_label_nearby_poi),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary,
            )

            HorizontalDivider(
                modifier = Modifier
                    .padding(start = 16.dp, end = 8.dp)
                    .weight(1f),
                color = MaterialTheme.colorScheme.secondary,
            )

            Icon(
                imageVector = if (showContent) {
                    Icons.Default.KeyboardArrowUp
                } else {
                    Icons.Default.KeyboardArrowDown
                },
                contentDescription = stringResource(R.string.demo_search_ev_content_description_nearby_poi_arrow),
                tint = MaterialTheme.colorScheme.secondary,
            )
        }

        if (showContent) {
            FlowRow(
                modifier = Modifier.padding(vertical = 8.dp),
            ) {
                placeDetails.nearbyPoiCategories.forEach {
                    Icon(
                        imageVector = ImageVector.vectorResource(EvNearbyPoiCategory.fromType(it).imageVector),
                        contentDescription = stringResource(EvNearbyPoiCategory.fromType(it).nearbyCategoryDescription),
                        modifier = Modifier
                            .size(32.dp)
                            .align(Alignment.CenterVertically),
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                    VerticalDivider(
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .height(40.dp)
                            .align(Alignment.CenterVertically),
                    )
                }
            }
        }
    }
}

@Composable
private fun EvChargePointsList(
    placeDetails: PlaceDetails,
    modifier: Modifier,
) {
    var showContent by rememberSaveable { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { showContent = !showContent },
        ) {
            Text(
                text = stringResource(R.string.demo_search_ev_label_charge_points),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary,
            )

            HorizontalDivider(
                modifier = Modifier
                    .padding(start = 16.dp, end = 8.dp)
                    .weight(1f),
                color = MaterialTheme.colorScheme.secondary,
            )

            Icon(
                imageVector = if (showContent) {
                    Icons.Default.KeyboardArrowUp
                } else {
                    Icons.Default.KeyboardArrowDown
                },
                contentDescription = stringResource(R.string.demo_search_ev_content_description_charge_points_arrow),
                tint = MaterialTheme.colorScheme.secondary,
            )
        }

        if (showContent) {
            placeDetails.chargePointAvailability.forEach { (connectorType, powerMap) ->
                EvChargePoint(
                    connectorType = connectorType,
                    powerAvailability = powerMap,
                )
            }
        }
    }
}

@Composable
private fun EvChargePoint(
    connectorType: ConnectorType,
    powerAvailability: Map<Power, List<Status?>>,
) {
    val connector = EvConnectorType.fromType(connectorType)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(top = 16.dp)
            .height(IntrinsicSize.Min),
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = connector.imageVector),
            contentDescription = stringResource(id = connector.connectorDescription),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(72.dp)
                .padding(horizontal = 12.dp, vertical = 12.dp),
        )

        Column(modifier = Modifier) {
            Text(
                text = stringResource(connector.connectorDescription),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )

            powerAvailability.forEach { (power, statuses) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .height(IntrinsicSize.Min)
                        .padding(top = 4.dp),
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.tt_asset_icon_chargeslow_fill_32),
                        contentDescription = stringResource(id = R.string.demo_search_ev_content_description_bolt),
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(end = 6.dp),
                    )
                    Text(
                        text = stringResource(
                            R.string.demo_search_ev_format_power_in_kw,
                            power.inKilowatts(),
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    VerticalDivider(
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                    )
                    Text(
                        text = stringResource(
                            R.string.demo_search_ev_format_charge_points_available,
                            statuses.count { it == Status.Available },
                            statuses.size,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.inversePrimary,
                    )
                }
            }
        }
    }
}
