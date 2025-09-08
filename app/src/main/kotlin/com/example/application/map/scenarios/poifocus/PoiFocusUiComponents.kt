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

package com.example.application.map.scenarios.poifocus

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.application.common.PlaceDetails
import com.example.application.common.locationDetails
import com.example.application.common.name
import com.example.application.common.ui.CloseButton
import com.example.application.common.ui.NavigationBottomPanel
import com.example.application.common.ui.RecenterMapButton
import com.example.application.map.model.MapScreenUiState
import com.example.application.ui.theme.NavSdkExampleTheme
import com.tomtom.sdk.location.Address
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.Place
import com.tomtom.sdk.map.display.camera.CameraOptions

/**
 * Renders the POI Focus UI with a close button, place details panel, and recenter control,
 * animating the camera to the POI and adapting layout to orientation and safe‑areas.
 */
@Composable
fun BoxScope.PoiFocusUiComponents(stateHolder: PoiFocusStateHolder) {
    BackHandler { stateHolder.onClearClick() }

    LaunchedEffect(Unit) {
        stateHolder.onSafeAreaTopPaddingUpdate(0)
    }

    LaunchedEffect(stateHolder.placeDetails) {
        stateHolder.onAnimateCamera(
            CameraOptions(
                zoom = MapScreenUiState.POI_CAMERA_ZOOM,
                tilt = MapScreenUiState.DEFAULT_TILT,
                rotation = MapScreenUiState.DEFAULT_ROTATION,
                position = stateHolder.placeDetails?.place?.coordinate,
            ),
        )
    }

    CloseButton(
        onClick = stateHolder.onClearClick,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                end = 16.dp,
            ),
    )

    if (stateHolder.isDeviceInLandscape) {
        Row(modifier = Modifier.align(Alignment.BottomStart)) {
            PoiFocusPanel(stateHolder = stateHolder)

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

            PoiFocusPanel(stateHolder = stateHolder)
        }
    }
}

@Composable
private fun PoiFocusPanel(stateHolder: PoiFocusStateHolder) {
    PoiFocusPanel(
        onRouteButtonClick = stateHolder.onRouteButtonClick,
        placeDetails = stateHolder.placeDetails,
        isDeviceInLandscape = stateHolder.isDeviceInLandscape,
        onSafeAreaBottomPaddingUpdate = stateHolder.onSafeAreaBottomPaddingUpdate,
    )
}

@Composable
private fun PoiFocusPanel(
    onRouteButtonClick: () -> Unit,
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
            if (placeDetails?.place?.address != null) {
                Text(
                    text = placeDetails.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
        subheader = {
            if (placeDetails?.place?.address != null) {
                Text(
                    text = placeDetails.locationDetails,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        body = {
            Button(
                onClick = onRouteButtonClick,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(
                    text = stringResource(R.string.poifocus_button_route),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        },
    )
}

@PreviewLightDark
@Composable
private fun PoiFocusPanelPreview() {
    NavSdkExampleTheme {
        PoiFocusPanel(
            onRouteButtonClick = {},
            placeDetails = PlaceDetails(
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
