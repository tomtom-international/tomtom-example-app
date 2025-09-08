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

package com.example.demo.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.Destination
import com.example.Destination.ChildActivityDestination.CustomMapStyleDestination
import com.example.DestinationCard
import com.example.PaddedLazyColumn
import com.example.R

@Composable
fun MapListScreen(
    onNavigateToDestination: (Destination) -> Unit,
    modifier: Modifier = Modifier,
) {
    PaddedLazyColumn(modifier = modifier) {
        item {
            DestinationCard(
                onClick = { onNavigateToDestination(CustomMapStyleDestination) },
                title = stringResource(R.string.demo_custom_map_style_title),
                subtitle = stringResource(R.string.demo_custom_map_style_subtitle),
            )
        }
    }
}
