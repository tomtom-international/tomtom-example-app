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

package com.example.demo.routing.routingwithwaypoints

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
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
import com.example.application.common.extension.formattedArrivalTime
import com.example.application.common.extension.formattedDistance
import com.example.application.common.extension.formattedDuration
import com.example.application.common.ui.FixedHeightBottomSheet
import com.example.application.common.ui.TextCheckBox
import com.example.application.common.ui.isDeviceInLandscape
import com.example.application.map.model.MapScreenUiState.ErrorState.RoutingError
import com.example.demo.DemoMap
import com.example.demo.DemoViewModel
import com.example.demo.routing.routingwithwaypoints.RoutingWithWaypointsViewModel.Companion.ON_SET_IS_LOADING_KEY
import com.example.demo.routing.routingwithwaypoints.RoutingWithWaypointsViewModel.Companion.ROUTE_PLANNER_KEY
import com.example.demo.routing.routingwithwaypoints.RoutingWithWaypointsViewModel.Companion.ROUTE_PLANNING_FAILURE_KEY
import com.example.demo.routing.routingwithwaypoints.RoutingWithWaypointsViewModel.Companion.ROUTE_PLANNING_SUCCESS_KEY
import com.example.demo.ui.LoadingOverlay
import com.tomtom.sdk.init.TomTomSdk
import com.tomtom.sdk.init.createRoutePlanner
import com.tomtom.sdk.map.display.camera.InitialCameraOptions
import com.tomtom.sdk.map.display.compose.state.rememberMapViewState
import com.tomtom.sdk.map.display.style.StyleMode
import com.tomtom.sdk.map.display.visualization.navigation.compose.NavigationVisualization
import com.tomtom.sdk.routing.RoutingFailure
import kotlinx.coroutines.flow.StateFlow

@Composable
fun RoutingWithWaypointsScreen(
    demoViewModel: DemoViewModel,
    modifier: Modifier = Modifier,
    viewModel: RoutingWithWaypointsViewModel = viewModel(
        factory = RoutingWithWaypointsViewModel.Factory,
        extras = MutableCreationExtras().apply {
            set(ROUTE_PLANNER_KEY, TomTomSdk.createRoutePlanner())
            set(ON_SET_IS_LOADING_KEY) { isLoading: Boolean -> demoViewModel.setIsLoading(isLoading) }
            set(ROUTE_PLANNING_SUCCESS_KEY) { response ->
                demoViewModel.updateRoutes(response.routes, response.routes.firstOrNull())
            }
            set(ROUTE_PLANNING_FAILURE_KEY) { _: RoutingFailure ->
                demoViewModel.updateErrorState { RoutingError }
            }
        },
    ),
) {
    val isDeviceInLandscape = isDeviceInLandscape()
    val mapUiState by demoViewModel.mapUiState.collectAsStateWithLifecycle()
    val mapDisplayInfrastructure by demoViewModel.mapDisplayInfrastructure.collectAsStateWithLifecycle()
    val navigationInfrastructure by demoViewModel.navigationInfrastructure.collectAsStateWithLifecycle()
    val selectedRoute by demoViewModel.selectedRoute.collectAsStateWithLifecycle()

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
            disableGestures = true,
        ) {
            NavigationVisualization(
                infrastructure = navigationInfrastructure,
            )
        }

        LaunchedEffect(Unit) {
            demoViewModel.updateSafeAreaTopPadding(0)
        }

        LoadingOverlay(isLoading = mapUiState.isLoading)

        selectedRoute?.let { route ->
            BottomPanel(
                eta = route.formattedArrivalTime(),
                isHagueSelectedFlow = viewModel.isHagueSelected,
                isUtrechtSelectedFlow = viewModel.isUtrechtSelected,
                isDordrechtSelectedFlow = viewModel.isDordrechtSelected,
                onHagueChange = { viewModel.setHagueSelected(it) },
                onUtrechtChange = { viewModel.setUtrechtSelected(it) },
                onDordrechtChange = { viewModel.setDordrechtSelected(it) },
                isDeviceInLandscape = isDeviceInLandscape,
                remainingDistance = route.formattedDistance(),
                remainingDuration = route.formattedDuration(),
                onSafeAreaBottomPaddingUpdate = { bottomPadding ->
                    demoViewModel.updateSafeAreaBottomPadding(bottomPadding)
                },
            )
        }
    }
}

@Composable
private fun BottomPanel(
    eta: String,
    isHagueSelectedFlow: StateFlow<Boolean>,
    isUtrechtSelectedFlow: StateFlow<Boolean>,
    isDordrechtSelectedFlow: StateFlow<Boolean>,
    onHagueChange: (Boolean) -> Unit,
    onUtrechtChange: (Boolean) -> Unit,
    onDordrechtChange: (Boolean) -> Unit,
    isDeviceInLandscape: Boolean,
    modifier: Modifier = Modifier,
    remainingDistance: String? = null,
    remainingDuration: String? = null,
    onSafeAreaBottomPaddingUpdate: (Int) -> Unit,
) {
    val sheetPeekHeight = remember { 192.dp }
    val localDensity = LocalDensity.current
    LaunchedEffect(Unit) {
        if (isDeviceInLandscape) {
            onSafeAreaBottomPaddingUpdate(localDensity.run { 0.dp.toPx().toInt() })
        } else {
            onSafeAreaBottomPaddingUpdate(localDensity.run { sheetPeekHeight.toPx().toInt() })
        }
    }

    FixedHeightBottomSheet(
        sheetPeekHeight = sheetPeekHeight,
        isDeviceInLandscape = isDeviceInLandscape,
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(top = 32.dp, start = 16.dp, end = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.tt_asset_graphic_finish_64),
                    contentDescription = stringResource(id = R.string.common_content_description_arrival_time),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(40.dp)
                        .padding(4.dp),
                )
                Text(
                    text = eta,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Row(modifier = Modifier.padding(start = 8.dp)) {
                Text(
                    text = remainingDistance ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                )
                if (remainingDistance != null && remainingDuration != null) {
                    VerticalDivider(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .height(MaterialTheme.typography.titleMedium.fontSize.value.dp)
                            .padding(start = 4.dp, end = 4.dp),
                        thickness = 2.dp,
                    )
                }
                Text(
                    text = remainingDuration ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                )
            }

            val isHagueSelected by isHagueSelectedFlow.collectAsStateWithLifecycle()
            val isUtrechtSelected by isUtrechtSelectedFlow.collectAsStateWithLifecycle()
            val isDordrechtSelected by isDordrechtSelectedFlow.collectAsStateWithLifecycle()

            WaypointsRow(
                isHagueSelected = isHagueSelected,
                isUtrechtSelected = isUtrechtSelected,
                isDordrechtSelected = isDordrechtSelected,
                onHagueChange = onHagueChange,
                onUtrechtChange = onUtrechtChange,
                onDordrechtChange = onDordrechtChange,
            )
        }
    }
}

@Composable
private fun WaypointsRow(
    isHagueSelected: Boolean,
    isUtrechtSelected: Boolean,
    isDordrechtSelected: Boolean,
    onHagueChange: (Boolean) -> Unit,
    onUtrechtChange: (Boolean) -> Unit,
    onDordrechtChange: (Boolean) -> Unit,
) {
    Text(
        text = stringResource(id = R.string.demo_routing_waypoints_label_waypoints),
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 16.dp),
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextCheckBox(
            text = stringResource(id = R.string.demo_routing_waypoints_label_waypoints_hague),
            checked = isHagueSelected,
            onCheckedChange = onHagueChange,
        )

        TextCheckBox(
            text = stringResource(id = R.string.demo_routing_waypoints_label_waypoints_utrecht),
            checked = isUtrechtSelected,
            onCheckedChange = onUtrechtChange,
        )

        TextCheckBox(
            text = stringResource(id = R.string.demo_routing_waypoints_label_waypoints_dordrecht),
            checked = isDordrechtSelected,
            onCheckedChange = onDordrechtChange,
        )
    }
}
