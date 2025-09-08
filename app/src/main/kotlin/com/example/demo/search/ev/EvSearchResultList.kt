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

package com.example.demo.search.ev

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.application.common.PlaceDetails
import com.example.application.common.name
import com.example.application.common.powerAvailability
import com.example.application.search.SearchResultItemContent
import com.example.application.ui.theme.NavSdkExampleTheme
import com.tomtom.quantity.Power
import com.tomtom.sdk.location.Address
import com.tomtom.sdk.location.BetaLocationApi
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.Place
import com.tomtom.sdk.location.poi.CategoryId
import com.tomtom.sdk.location.poi.Poi
import com.tomtom.sdk.location.poi.StandardCategoryId
import com.tomtom.sdk.location.poi.ev.AccessType
import com.tomtom.sdk.location.poi.ev.ChargingPark
import com.tomtom.sdk.location.poi.ev.ChargingPoint
import com.tomtom.sdk.location.poi.ev.ChargingStation
import com.tomtom.sdk.location.poi.ev.Connector
import com.tomtom.sdk.location.poi.ev.Status
import com.tomtom.sdk.location.poi.time.OpeningHours
import com.tomtom.sdk.vehicle.ConnectorDetails
import com.tomtom.sdk.vehicle.ConnectorType
import java.util.Date

@Composable
fun EvSearchResultList(
    searchResults: List<SearchResultItemContent>,
    onSearchResultClick: (SearchResultItemContent) -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
) {
    LazyColumn(
        state = lazyListState,
        modifier = modifier.padding(top = 16.dp),
    ) {
        items(items = searchResults) { searchResult ->
            EvSearchResultItem(
                item = searchResult,
                onSearchResultClick = onSearchResultClick,
            )
        }
    }
}

@Composable
private fun EvSearchResultItem(
    item: SearchResultItemContent,
    onSearchResultClick: (SearchResultItemContent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = modifier
            .padding(16.dp)
            .clickable(
                onClick = { onSearchResultClick(item) },
            ),
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.outline_ev_station_24px),
            contentDescription = stringResource(id = R.string.search_content_description_search_result),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(end = 16.dp)
                .size(32.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.placeDetails.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            item.placeDetails.powerAvailability.forEach { (power, statuses) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .height(IntrinsicSize.Min),
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.bolt_24px),
                        contentDescription = stringResource(id = R.string.demo_search_ev_content_description_bolt),
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(end = 4.dp),
                    )

                    Text(
                        textAlign = TextAlign.Right,
                        text = stringResource(
                            R.string.demo_search_ev_format_power_in_kw,
                            power.inKilowatts(),
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(end = 4.dp),
                    )

                    VerticalDivider(
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )

                    Text(
                        text = stringResource(
                            R.string.demo_search_ev_format_charge_points_available,
                            statuses.count { it == Status.Available },
                            statuses.size,
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (statuses.count { it == Status.Available } > 0) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                    )
                }
            }
        }
        Text(
            textAlign = TextAlign.Right,
            text = item.distance,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

@Suppress("detekt:MagicNumber")
@PreviewLightDark
@OptIn(BetaLocationApi::class)
@Composable
private fun EvSearchResultItemPreview() {
    val searchResultItemContentList = List(4) {
        SearchResultItemContent(
            iconId = R.drawable.outline_ev_station_24px,
            placeDetails = PlaceDetails(
                place = Place(
                    coordinate = GeoPoint(
                        latitude = 37.7749,
                        longitude = -122.4194,
                    ),
                    name = "Ev Charging Station",
                    address = Address(
                        freeformAddress = "123 Main St, Springfield, USA",
                        countrySecondarySubdivision = "Springfield",
                        municipality = "Springfield",
                        countryCodeIso3 = "USA",
                    ),
                ),
                poi = Poi(
                    names = setOf("Ev Charging Station"),
                    categoryIds = setOf(CategoryId(StandardCategoryId.ElectricVehicleChargingStation)),
                    urls = setOf("https://example.com/gas-station"),
                    openingHours = OpeningHours(
                        ranges = listOf(
                            Date(1719878400000L)..Date(1719907200000L),
                            Date(1719910800000L)..Date(1719936000000L),
                        ),
                    ),
                    chargingPark = ChargingPark(
                        chargingStations = listOf(
                            ChargingStation(
                                id = ChargingStation.Id("station-123"),
                                chargingPoints = listOf(
                                    ChargingPoint(
                                        evseId = "123",
                                        status = Status.Available,
                                        connectors = listOf(
                                            Connector(
                                                id = Connector.Id("connector-1"),
                                                connectorDetails = ConnectorDetails(
                                                    connectorType = ConnectorType.Iec62196Type2CableAttached,
                                                    ratedPower = Power.kilowatts(50),
                                                ),
                                            ),
                                        ),
                                    ),
                                    ChargingPoint(
                                        evseId = "123",
                                        status = Status.Available,
                                        connectors = listOf(
                                            Connector(
                                                id = Connector.Id("connector-2"),
                                                connectorDetails = ConnectorDetails(
                                                    connectorType = ConnectorType.Iec62196Type2CableAttached,
                                                    ratedPower = Power.kilowatts(50),
                                                ),
                                            ),
                                        ),
                                    ),
                                ),
                            ),
                            ChargingStation(
                                id = ChargingStation.Id("station-123"),
                                chargingPoints = listOf(
                                    ChargingPoint(
                                        evseId = "123",
                                        status = Status.Occupied,
                                        connectors = listOf(
                                            Connector(
                                                id = Connector.Id("connector-1"),
                                                connectorDetails = ConnectorDetails(
                                                    connectorType = ConnectorType.Iec62196Type2CableAttached,
                                                    ratedPower = Power.kilowatts(150),
                                                ),
                                            ),
                                        ),
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
                accessType = AccessType.Public,
                nearbyPoiCategories = setOf(
                    StandardCategoryId.Restaurant,
                    StandardCategoryId.HotelMotel,
                    StandardCategoryId.CafePub,
                ),
            ),
            distance = "2.5 km",
        )
    }

    NavSdkExampleTheme {
        EvSearchResultList(
            searchResults = searchResultItemContentList,
            onSearchResultClick = {},
            modifier = Modifier,
        )
    }
}
