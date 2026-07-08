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

package com.example.demo.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.Destination
import com.example.Destination.ChildActivityDestination.ManualMapManagementDestination
import com.example.DestinationCard
import com.example.PaddedLazyColumn
import com.example.R
import com.tomtom.sdk.annotations.BetaNdsStoreApi
import com.tomtom.sdk.datamanagement.nds.update.BetaRegionStoreAliasApi
import com.tomtom.sdk.init.TomTomSdk

@OptIn(BetaNdsStoreApi::class, BetaRegionStoreAliasApi::class)
@Composable
fun MapListScreen(
    onNavigateToDestination: (Destination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isOfflineStoreAvailable = remember {
        try {
            TomTomSdk.getOfflineRegionStore()
            true
        } catch (e: IllegalStateException) {
            false
        }
    }

    PaddedLazyColumn(modifier = modifier) {
        item {
            DestinationCard(
                onClick = { onNavigateToDestination(ManualMapManagementDestination) },
                enabled = isOfflineStoreAvailable,
                title = stringResource(R.string.demo_manual_map_management_title),
                subtitle = if (isOfflineStoreAvailable) {
                    "${stringResource(R.string.demo_manual_map_management_subtitle)}" +
                        "\n\n${stringResource(R.string.demo_manual_map_management_evaluation_note)}"
                } else {
                    stringResource(R.string.demo_manual_map_management_not_available_online_only)
                },
            )
        }
    }
}
