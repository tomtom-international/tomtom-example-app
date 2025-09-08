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

package com.example.application.arrival

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.application.common.NavigationBottomPanel
import com.example.application.common.PlaceDetails
import com.example.application.common.locationDetails
import com.example.application.common.name
import com.example.application.ui.RecenterMapButton
import com.example.application.ui.theme.NavSdkExampleTheme
import com.tomtom.sdk.location.Address
import com.tomtom.sdk.location.BetaLocationApi
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.Place

@Composable
fun BoxScope.ArrivalUiComponents(stateHolder: ArrivalStateHolder) {
    BackHandler { stateHolder.onArrivalButtonClick() }

    LaunchedEffect(Unit) {
        stateHolder.onSafeAreaTopPaddingUpdate(0)
    }

    if (stateHolder.isDeviceInLandscape) {
        Row(modifier = Modifier.align(Alignment.BottomStart)) {
            ArrivalPanel(stateHolder = stateHolder)

            RecenterMapButton(
                stateHolder = stateHolder.recenterMapStateHolder,
                modifier = Modifier
                    .align(Alignment.Bottom)
                    .padding(16.dp),
            )
        }
    } else {
        Column(modifier = Modifier.align(Alignment.BottomStart)) {
            RecenterMapButton(stateHolder = stateHolder.recenterMapStateHolder, modifier = Modifier.padding(16.dp))

            ArrivalPanel(stateHolder = stateHolder)
        }
    }
}

@Composable
private fun ArrivalPanel(stateHolder: ArrivalStateHolder) {
    ArrivalPanel(
        onArrivalButtonClick = stateHolder.onArrivalButtonClick,
        destinationDetails = stateHolder.destinationDetails,
        isDeviceInLandscape = stateHolder.isDeviceInLandscape,
        onSafeAreaBottomPaddingUpdate = stateHolder.onSafeAreaBottomPaddingUpdate,
    )
}

@Composable
private fun ArrivalPanel(
    onArrivalButtonClick: () -> Unit,
    destinationDetails: PlaceDetails?,
    isDeviceInLandscape: Boolean,
    onSafeAreaBottomPaddingUpdate: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBottomPanel(
        isDeviceInLandscape = isDeviceInLandscape,
        modifier = modifier
            .onGloballyPositioned { onSafeAreaBottomPaddingUpdate(if (isDeviceInLandscape) 0 else it.size.height) },
        header = {
            if (destinationDetails?.place?.address != null) {
                Text(
                    text = destinationDetails.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
        subheader = {
            if (destinationDetails?.place?.address != null) {
                Text(
                    text = destinationDetails.locationDetails,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                )
            }
        },
        body = {
            Button(
                onClick = onArrivalButtonClick,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(
                    text = stringResource(R.string.arrival_button_finish),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        },
    )
}

@OptIn(BetaLocationApi::class)
@PreviewLightDark
@Composable
private fun ArrivalPanelPreview() {
    NavSdkExampleTheme {
        ArrivalPanel(
            onArrivalButtonClick = {},
            destinationDetails = PlaceDetails(
                place = Place(
                    coordinate = GeoPoint(
                        latitude = 37.7749,
                        longitude = -122.4194,
                    ),
                    name = "Gas Station",
                    address = Address(
                        freeformAddress = "123 Main St, Springfield, USA",
                        countrySecondarySubdivision = "Springfield",
                        municipality = "Springfield",
                        countryCodeIso3 = "USA",
                    ),
                ),
            ),
            isDeviceInLandscape = false,
            onSafeAreaBottomPaddingUpdate = { _ -> },
            modifier = Modifier,
        )
    }
}
