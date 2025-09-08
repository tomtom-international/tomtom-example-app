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

package com.example.demo.search.area

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.application.common.TOMTOM_AMSTERDAM_OFFICE
import com.example.application.common.extension.toPx
import com.example.application.common.ui.FixedHeightBottomSheet
import com.example.application.common.ui.PoiIconButton
import com.example.application.common.ui.getPinMarkerProperties
import com.example.application.common.ui.isDeviceInLandscape
import com.example.application.map.model.MapScreenUiState.ErrorState.SearchError
import com.example.application.search.poiOptions
import com.example.demo.DemoMap
import com.example.demo.DemoViewModel
import com.example.demo.search.area.PoiSearchAreaViewModel.Companion.GEO_POINT_KEY
import com.example.demo.search.area.PoiSearchAreaViewModel.Companion.SEARCH_FAILURE_KEY
import com.example.demo.search.area.PoiSearchAreaViewModel.Companion.SEARCH_KEY
import com.example.demo.search.area.PoiSearchAreaViewModel.Companion.SEARCH_SUCCESS_KEY
import com.tomtom.sdk.init.TomTomSdk
import com.tomtom.sdk.init.createSearch
import com.tomtom.sdk.location.poi.StandardCategoryId
import com.tomtom.sdk.map.display.camera.InitialCameraOptions
import com.tomtom.sdk.map.display.compose.model.MarkerData
import com.tomtom.sdk.map.display.compose.nodes.Marker
import com.tomtom.sdk.map.display.compose.state.rememberMapViewState
import com.tomtom.sdk.map.display.compose.state.rememberMarkerState
import com.tomtom.sdk.map.display.style.StyleMode
import com.tomtom.sdk.map.display.visualization.navigation.compose.NavigationVisualization
import com.tomtom.sdk.search.common.error.SearchFailure

/**
 * POI in area demo — searches for places around the visible map region or a fixed center.
 */
@Composable
fun PoiSearchAreaScreen(
    demoViewModel: DemoViewModel,
    modifier: Modifier = Modifier,
    viewModel: PoiSearchAreaViewModel = viewModel(
        factory = PoiSearchAreaViewModel.Factory,
        extras = MutableCreationExtras().apply {
            set(GEO_POINT_KEY, TOMTOM_AMSTERDAM_OFFICE)
            set(SEARCH_KEY, TomTomSdk.createSearch())
            set(SEARCH_SUCCESS_KEY) {
                demoViewModel.zoomToAllMarkers()
            }
            set(SEARCH_FAILURE_KEY) { _: SearchFailure ->
                demoViewModel.updateErrorState { SearchError }
            }
        },
    ),
) {
    val isDeviceInLandscape = isDeviceInLandscape()
    val mapDisplayInfrastructure by demoViewModel.mapDisplayInfrastructure.collectAsStateWithLifecycle()
    val mapUiState by demoViewModel.mapUiState.collectAsStateWithLifecycle()
    val navigationInfrastructure by demoViewModel.navigationInfrastructure.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val zoomToAllMarkers by demoViewModel.zoomToAllMarkers.collectAsStateWithLifecycle()

    val initialCameraOptions = InitialCameraOptions.LocationBased(position = TOMTOM_AMSTERDAM_OFFICE)

    val mapViewState = rememberMapViewState(initialCameraOptions = initialCameraOptions) {
        this.styleState.styleMode = StyleMode.MAIN
    }

    Box(modifier = modifier) {
        DemoMap(
            mapUiState = mapUiState,
            mapDisplayInfrastructure = mapDisplayInfrastructure,
            isDeviceInLandscape = isDeviceInLandscape,
            mapViewState = mapViewState,
            zoomToAllMarkers = zoomToAllMarkers,
            disableGestures = true,
        ) {
            NavigationVisualization(infrastructure = navigationInfrastructure)
            searchResults.forEach { searchResult ->
                Marker(
                    data = MarkerData(geoPoint = searchResult.placeDetails.place.coordinate),
                    properties = getPinMarkerProperties(searchResult.iconId),
                    state = rememberMarkerState(),
                )
            }
        }

        LaunchedEffect(Unit) {
            demoViewModel.updateSafeAreaTopPadding(0)
        }

        BottomPanel(
            onPoiCategoryClick = { categoryId -> viewModel.onPoiCategoryClick(categoryId) },
            onSafeAreaBottomPaddingUpdate = { bottomPadding ->
                demoViewModel.updateSafeAreaBottomPadding(bottomPadding)
            },
            isDeviceInLandscape = isDeviceInLandscape,
        )
    }
}

@Composable
private fun BottomPanel(
    onPoiCategoryClick: (StandardCategoryId) -> Unit,
    onSafeAreaBottomPaddingUpdate: (Int) -> Unit,
    isDeviceInLandscape: Boolean,
    modifier: Modifier = Modifier,
) {
    val sheetPeekHeight = remember { 208.dp }
    val localDensity = LocalDensity.current

    LaunchedEffect(Unit) {
        if (isDeviceInLandscape) {
            onSafeAreaBottomPaddingUpdate(0)
        } else {
            onSafeAreaBottomPaddingUpdate(sheetPeekHeight.toPx(localDensity))
        }
    }

    FixedHeightBottomSheet(
        sheetPeekHeight = sheetPeekHeight,
        isDeviceInLandscape = isDeviceInLandscape,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(top = 32.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
        ) {
            Text(
                text = stringResource(R.string.demo_search_poi_in_area_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 2.dp),
            )
            Text(
                text = stringResource(R.string.demo_search_poi_in_area_subtitle),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                poiOptions.forEach { poiOption ->
                    PoiIconButton(
                        imageVector = ImageVector.vectorResource(poiOption.value.imageVector),
                        contentDescription = stringResource(poiOption.value.contentDescription),
                        onClick = { onPoiCategoryClick(poiOption.key) },
                    )
                }
            }
        }
    }
}
