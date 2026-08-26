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
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.application.common.name
import com.example.application.common.powerAvailability
import com.example.application.search.SearchResultItemContent
import com.tomtom.sdk.location.poi.ev.Status

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
            imageVector = ImageVector.vectorResource(R.drawable.tt_asset_icon_evcharger_fill_32),
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
                        imageVector = ImageVector.vectorResource(id = R.drawable.tt_asset_icon_chargeslow_fill_32),
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
