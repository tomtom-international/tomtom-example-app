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

package com.example.demo.routing.routewithtiming

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.application.common.extension.asFormattedTime
import com.example.application.common.extension.formattedArrivalTimeWithDate
import com.example.application.common.extension.formattedDepartureTimeWithDate
import com.example.application.common.extension.formattedDistance
import com.example.application.common.extension.formattedDuration
import com.example.application.common.ui.FixedHeightDemoBottomSheet
import com.example.application.common.ui.TextRadioButton
import com.example.application.common.ui.isDeviceInLandscape
import com.example.application.map.model.MapScreenUiState.ErrorState.RoutingError
import com.example.demo.DemoMap
import com.example.demo.DemoViewModel
import com.example.demo.routing.routewithtiming.RouteWithTimingViewModel.Companion.ON_SET_IS_LOADING_KEY
import com.example.demo.routing.routewithtiming.RouteWithTimingViewModel.Companion.ROUTE_PLANNER_KEY
import com.example.demo.routing.routewithtiming.RouteWithTimingViewModel.Companion.ROUTE_PLANNING_FAILURE_KEY
import com.example.demo.routing.routewithtiming.RouteWithTimingViewModel.Companion.ROUTE_PLANNING_SUCCESS_KEY
import com.example.demo.routing.routewithtiming.RouteWithTimingViewModel.Companion.SELECT_ROUTE_KEY
import com.example.demo.ui.LoadingOverlay
import com.tomtom.sdk.init.TomTomSdk
import com.tomtom.sdk.init.createRoutePlanner
import com.tomtom.sdk.map.display.camera.InitialCameraOptions
import com.tomtom.sdk.map.display.compose.state.rememberMapViewState
import com.tomtom.sdk.map.display.style.StyleMode
import com.tomtom.sdk.map.display.visualization.navigation.compose.NavigationVisualization
import com.tomtom.sdk.routing.RoutingFailure
import com.tomtom.sdk.routing.route.Route
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private val BOTTOM_SHEET_PEEK_HEIGHT = 330.dp

@Composable
fun RouteWithTimingScreen(
    demoViewModel: DemoViewModel,
    modifier: Modifier = Modifier,
    viewModel: RouteWithTimingViewModel = viewModel(
        factory = RouteWithTimingViewModel.Factory,
        extras = MutableCreationExtras().apply {
            set(ROUTE_PLANNER_KEY, TomTomSdk.createRoutePlanner())
            set(ON_SET_IS_LOADING_KEY) { isLoading: Boolean -> demoViewModel.setIsLoading(isLoading) }
            set(ROUTE_PLANNING_SUCCESS_KEY) { response ->
                demoViewModel.updateRoutes(response.routes, response.routes.firstOrNull())
            }
            set(ROUTE_PLANNING_FAILURE_KEY) { _: RoutingFailure ->
                demoViewModel.updateErrorState { RoutingError }
            }
            set(SELECT_ROUTE_KEY) { routeId -> demoViewModel.selectRoute(routeId) }
        },
    ),
) {
    val isDeviceInLandscape = isDeviceInLandscape()
    val mapUiState by demoViewModel.mapUiState.collectAsStateWithLifecycle()
    val mapDisplayInfrastructure by demoViewModel.mapDisplayInfrastructure.collectAsStateWithLifecycle()
    val navigationInfrastructure by demoViewModel.navigationInfrastructure.collectAsStateWithLifecycle()
    val selectedRoute by demoViewModel.selectedRoute.collectAsStateWithLifecycle()
    val isEvCar by viewModel.isEvCar.collectAsStateWithLifecycle()
    val isArrivalTimeSelected by viewModel.isArrivalTimeSelected.collectAsStateWithLifecycle()
    val isDepartureTimeSelected by viewModel.isDepartureTimeSelected.collectAsStateWithLifecycle()
    val planningDateTime by viewModel.planningDateTime.collectAsStateWithLifecycle()

    val initialCameraOptions = InitialCameraOptions.LocationBased(position = TOMTOM_AMSTERDAM_OFFICE)

    val mapViewState = rememberMapViewState(initialCameraOptions = initialCameraOptions) {
        this.styleState.styleMode = StyleMode.MAIN
    }

    val localDensity = LocalDensity.current
    val statusBarTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

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
                onRouteClick = { viewModel.onRouteClick(it) },
            )
        }

        LaunchedEffect(Unit) {
            val topPadding = localDensity.run {
                statusBarTopPadding.toPx().toInt()
            }
            demoViewModel.updateSafeAreaTopPadding(topPadding)

            val bottomPadding = if (isDeviceInLandscape) {
                0
            } else {
                localDensity.run {
                    BOTTOM_SHEET_PEEK_HEIGHT.toPx().toInt()
                }
            }
            demoViewModel.updateSafeAreaBottomPadding(bottomPadding)
        }

        LoadingOverlay(isLoading = mapUiState.isLoading)

        selectedRoute?.let { route ->
            BottomPanel(
                route = route,
                isEvCar = isEvCar,
                isArrivalTimeSelected = isArrivalTimeSelected,
                isDepartureTimeSelected = isDepartureTimeSelected,
                planningDateTime = planningDateTime,
                onCarTypeChange = { viewModel.setCarType(it) },
                onTimingPreferenceChange = { isArrival, isDeparture, dateTime ->
                    viewModel.setTimingPreference(
                        isArrival,
                        isDeparture,
                        dateTime,
                    )
                },
                isDeviceInLandscape = isDeviceInLandscape,
            )
        }
    }
}

@Composable
private fun BottomPanel(
    route: Route,
    isEvCar: Boolean,
    isArrivalTimeSelected: Boolean,
    isDepartureTimeSelected: Boolean,
    planningDateTime: ZonedDateTime?,
    onCarTypeChange: (Boolean) -> Unit,
    onTimingPreferenceChange: (Boolean, Boolean, ZonedDateTime?) -> Unit,
    isDeviceInLandscape: Boolean,
    modifier: Modifier = Modifier,
) {
    FixedHeightDemoBottomSheet(
        sheetPeekHeight = BOTTOM_SHEET_PEEK_HEIGHT,
        isDeviceInLandscape = isDeviceInLandscape,
        modifier = modifier,
    ) {
        ArrivalDepartureTimeRow(route = route)
        RouteInfoRow(route = route, isEvCar = isEvCar)
        CarTypeRow(
            isEvCar = isEvCar,
            onCarTypeChange = onCarTypeChange,
            modifier = Modifier.fillMaxWidth(),
        )
        TimingPreferenceRow(
            isArrivalTimeSelected = isArrivalTimeSelected,
            isDepartureTimeSelected = isDepartureTimeSelected,
            planningDateTime = planningDateTime,
            onTimingPreferenceChange = onTimingPreferenceChange,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ArrivalDepartureTimeRow(
    route: Route,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.tt_asset_graphic_finish_64),
            contentDescription = stringResource(id = R.string.common_content_description_arrival_time),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(40.dp)
                .padding(4.dp),
        )
        Text(
            text = route.formattedArrivalTimeWithDate(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
    }

    Row(
        modifier = modifier.padding(start = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(
                R.string.demo_route_timing_departure_time,
                route.formattedDepartureTimeWithDate(),
            ),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Composable
private fun RouteInfoRow(
    route: Route,
    isEvCar: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.padding(start = 8.dp)) {
        Text(
            text = route.formattedDistance(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary,
            maxLines = 1,
        )
        if (route.formattedDuration() != null) {
            VerticalDivider(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .height(MaterialTheme.typography.titleMedium.fontSize.value.dp)
                    .padding(start = 4.dp, end = 4.dp),
                thickness = 2.dp,
            )
        }
        Text(
            text = route.formattedDuration() ?: "",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary,
            maxLines = 1,
        )
        if (isEvCar) {
            val stops = maxOf(0, route.legs.size - 1)
            VerticalDivider(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .height(MaterialTheme.typography.titleMedium.fontSize.value.dp)
                    .padding(start = 4.dp, end = 4.dp),
                thickness = 2.dp,
            )
            Text(
                text = stringResource(R.string.demo_route_timing_stops, stops),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@Composable
private fun CarTypeRow(
    isEvCar: Boolean,
    onCarTypeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(id = R.string.demo_route_timing_car_type_label),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(start = 8.dp, top = 16.dp, bottom = 8.dp),
    )
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextRadioButton(
            key = false,
            stringResource(R.string.demo_route_timing_car_fuel),
            selected = !isEvCar,
            onOptionSelected = { onCarTypeChange(it as Boolean) },
        )
        TextRadioButton(
            key = true,
            stringResource(R.string.demo_route_timing_car_ev),
            selected = isEvCar,
            onOptionSelected = { onCarTypeChange(it as Boolean) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimingPreferenceRow(
    isArrivalTimeSelected: Boolean,
    isDepartureTimeSelected: Boolean,
    planningDateTime: ZonedDateTime?,
    onTimingPreferenceChange: (Boolean, Boolean, ZonedDateTime?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState(is24Hour = true)

    Text(
        text = stringResource(id = R.string.demo_route_timing_timing_label),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(start = 8.dp, top = 16.dp, bottom = 8.dp),
    )
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextRadioButton(
            key = Unit,
            stringResource(R.string.demo_route_timing_arrive_at),
            selected = isArrivalTimeSelected,
            onOptionSelected = {
                onTimingPreferenceChange(true, false, planningDateTime)
            },
        )
        TextRadioButton(
            key = Unit,
            stringResource(R.string.demo_route_timing_depart_at),
            selected = isDepartureTimeSelected,
            onOptionSelected = {
                onTimingPreferenceChange(false, true, planningDateTime)
            },
        )
    }

    OutlinedButton(
        onClick = { showDatePicker = true },
        modifier = modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp),
    ) {
        Text(
            text = planningDateTime?.formattedForDisplay()
                ?: stringResource(R.string.demo_route_timing_select_date_time),
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                        showTimePicker = true
                    },
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showTimePicker = false
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            val localDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            val zonedDateTime = localDate
                                .atTime(timePickerState.hour, timePickerState.minute)
                                .atZone(ZoneId.systemDefault())
                            onTimingPreferenceChange(
                                isArrivalTimeSelected,
                                isDepartureTimeSelected,
                                zonedDateTime,
                            )
                        }
                    },
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
            text = {
                TimePicker(state = timePickerState)
            },
        )
    }
}

private fun ZonedDateTime.formattedForDisplay(): String {
    val date = format(DateTimeFormatter.ofPattern("dd MMM"))
    val time = toInstant().toEpochMilli().asFormattedTime()
    return "$date, $time"
}
