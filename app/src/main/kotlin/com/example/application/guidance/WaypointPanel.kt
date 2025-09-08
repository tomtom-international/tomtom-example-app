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

package com.example.application.guidance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.R
import com.example.application.common.NavigationBottomPanel
import com.example.application.common.PlaceDetails
import com.example.application.common.locationDetails
import com.example.application.common.name
import com.example.application.ui.CloseButton
import com.example.application.ui.theme.NavSdkExampleTheme
import com.tomtom.sdk.location.Address
import com.tomtom.sdk.location.BetaLocationApi
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.Place

@Composable
fun WaypointPanel(
    isAddingStop: Boolean,
    onButtonClick: () -> Unit,
    onCloseWaypointPanelButtonClick: () -> Unit,
    placeDetails: PlaceDetails?,
    isDeviceInLandscape: Boolean,
    onSafeAreaBottomPaddingUpdate: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBottomPanel(
        isDeviceInLandscape = isDeviceInLandscape,
        modifier = modifier
            .onGloballyPositioned { onSafeAreaBottomPaddingUpdate(if (isDeviceInLandscape) 0 else it.size.height) },
        header = {
            if (placeDetails != null) {
                Text(
                    text = placeDetails.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
        subheader = {
            if (placeDetails != null) {
                Text(
                    text = placeDetails.locationDetails,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        rightSideColumn = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxHeight(),
            ) {
                CloseButton(
                    iconTint = MaterialTheme.colorScheme.background,
                    backgroundColor = MaterialTheme.colorScheme.primary,
                    onClick = onCloseWaypointPanelButtonClick,
                )

                Button(onClick = onButtonClick) {
                    Text(
                        text = if (isAddingStop) {
                            stringResource(R.string.guidance_button_add_stop)
                        } else {
                            stringResource(R.string.guidance_button_remove_stop)
                        },
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        },
    )
}

@OptIn(BetaLocationApi::class)
@PreviewLightDark
@Composable
private fun WaypointPanelPreview() {
    NavSdkExampleTheme {
        Column {
            WaypointPanel(
                isAddingStop = true,
                onButtonClick = {},
                onCloseWaypointPanelButtonClick = {},
                placeDetails = PlaceDetails(
                    place = Place(
                        coordinate = GeoPoint(
                            latitude = 37.7749,
                            longitude = -122.4194,
                        ),
                        name = "Gas Station",
                        address = Address(
                            freeformAddress = "123 A very long street name a really long one!, Springfield",
                            countrySecondarySubdivision = "Springfield",
                            municipality = "Springfield",
                            countryCodeIso3 = "USA",
                        ),
                    ),
                ),
                onSafeAreaBottomPaddingUpdate = { _ -> },
                isDeviceInLandscape = false,
                modifier = Modifier,
            )
        }
    }
}
