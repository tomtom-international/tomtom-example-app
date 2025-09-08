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
import com.example.application.common.FixedHeightBottomSheet
import com.example.application.common.TOMTOM_AMSTERDAM_OFFICE
import com.example.application.common.getPinMarkerProperties
import com.example.application.common.isDeviceInLandscape
import com.example.application.extension.toPx
import com.example.application.map.MapScreenUiState.ErrorState.SearchError
import com.example.application.search.poiOptions
import com.example.application.ui.PoiIconButton
import com.example.demo.DemoMap
import com.example.demo.DemoViewModel
import com.example.demo.search.area.PoiSearchAreaViewModel.Companion.GEO_POINT_KEY
import com.example.demo.search.area.PoiSearchAreaViewModel.Companion.SEARCH_FAILURE_KEY
import com.example.demo.search.area.PoiSearchAreaViewModel.Companion.SEARCH_KEY
import com.example.demo.search.area.PoiSearchAreaViewModel.Companion.SEARCH_SUCCESS_KEY
import com.tomtom.sdk.annotations.AlphaSdkInitializationApi
import com.tomtom.sdk.entrypoint.TomTomSdk
import com.tomtom.sdk.location.poi.StandardCategoryId
import com.tomtom.sdk.map.display.annotation.BetaInitialCameraOptionsApi
import com.tomtom.sdk.map.display.camera.InitialCameraOptions
import com.tomtom.sdk.map.display.compose.BetaMapComposableApi
import com.tomtom.sdk.map.display.compose.model.MarkerData
import com.tomtom.sdk.map.display.compose.nodes.Marker
import com.tomtom.sdk.map.display.compose.state.rememberMapViewState
import com.tomtom.sdk.map.display.compose.state.rememberMarkerState
import com.tomtom.sdk.map.display.style.StyleMode
import com.tomtom.sdk.map.display.visualization.navigation.compose.NavigationVisualization
import com.tomtom.sdk.search.common.error.SearchFailure

@OptIn(
    AlphaSdkInitializationApi::class,
    BetaMapComposableApi::class,
    BetaInitialCameraOptionsApi::class,
)
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
    val mapInfrastructure by demoViewModel.mapInfrastructure.collectAsStateWithLifecycle()
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
            mapInfrastructure = mapInfrastructure,
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
