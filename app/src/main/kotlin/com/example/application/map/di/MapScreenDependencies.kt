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

package com.example.application.map.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.application.common.tts.TextToSpeechEngine
import com.example.application.map.MapScreenViewModel
import com.example.application.map.RoutesViewModel
import com.example.application.map.model.MapScreenAction
import com.example.application.map.model.MapScreenAction.ClearSearch
import com.example.application.map.model.MapScreenAction.ShowPoiCategorySearchResultFocus
import com.example.application.map.model.MapScreenAction.ShowSearchFailure
import com.example.application.map.scenarios.freedriving.FreeDrivingManager
import com.example.application.search.SearchViewModel
import com.example.application.settings.data.SettingsRepository
import com.tomtom.sdk.init.TomTomSdk
import com.tomtom.sdk.init.createReverseGeocoder
import com.tomtom.sdk.init.createRoutePlanner
import com.tomtom.sdk.init.createSearch
import com.tomtom.sdk.search.common.error.SearchFailure

/**
 * Creates and remembers the ViewModels consumed by MapScreen.
 *
 * Single place to assemble SDK extras and callbacks so the UI stays free of wiring.
 */
@Composable
fun rememberMapScreenViewModels(
    settingsRepository: SettingsRepository,
    onCheckLocationPermission: () -> Boolean,
): MapScreenDependencies {
    val context = LocalContext.current

    val routesViewModel: RoutesViewModel = viewModel(
        factory = RoutesViewModel.Factory,
        extras = MutableCreationExtras().apply {
            set(RoutesViewModel.ROUTE_PLANNER_KEY, TomTomSdk.createRoutePlanner())
            set(RoutesViewModel.NAVIGATION_KEY, TomTomSdk.navigation)
        },
    )

    val mapScreenViewModel: MapScreenViewModel = viewModel(
        factory = MapScreenViewModel.Factory,
        extras = MutableCreationExtras().apply {
            set(
                MapScreenViewModel.SDK_CONTEXT,
                TomTomSdk.sdkContext,
            )
            set(MapScreenViewModel.DEFAULT_LOCATION_PROVIDER_KEY, TomTomSdk.locationProvider)
            set(MapScreenViewModel.REVERSE_GEOCODER_KEY, TomTomSdk.createReverseGeocoder())
            set(MapScreenViewModel.NAVIGATION_KEY, TomTomSdk.navigation)
            set(MapScreenViewModel.FREE_DRIVING_MANAGER_KEY, FreeDrivingManager())
            set(MapScreenViewModel.SETTINGS_REPOSITORY_KEY, settingsRepository)
            set(MapScreenViewModel.ON_CLEAR_MAP_KEY) { routesViewModel.clearRoutes() }
            set(MapScreenViewModel.ON_CHECK_LOCATION_PERMISSION, onCheckLocationPermission)
            set(MapScreenViewModel.TEXT_TO_SPEECH_ENGINE_KEY, TextToSpeechEngine(context))
        },
    )

    val searchViewModel: SearchViewModel = viewModel(
        factory = SearchViewModel.Factory,
        extras = MutableCreationExtras().apply {
            set(SearchViewModel.LOCATION_PROVIDER_KEY, TomTomSdk.locationProvider)
            set(SearchViewModel.SEARCH_KEY, TomTomSdk.createSearch())
            set(SearchViewModel.SEARCH_FAILURE_KEY) { failure: SearchFailure ->
                mapScreenViewModel.dispatchAction(ShowSearchFailure(failure))
            }
            set(SearchViewModel.POI_SEARCH_SUCCESS_KEY) { searchResults ->
                mapScreenViewModel.dispatchAction(ShowPoiCategorySearchResultFocus(searchResults))
            }
            set(SearchViewModel.CLEAN_MAP_KEY) { mapScreenViewModel.dispatchAction(ClearSearch) }
            set(SearchViewModel.TOGGLE_BOTTOM_SHEET_KEY) { toggleBottomSheetValue ->
                mapScreenViewModel.dispatchAction(MapScreenAction.ToggleBottomSheet(toggleBottomSheetValue))
            }
        },
    )

    return remember(mapScreenViewModel, routesViewModel, searchViewModel) {
        MapScreenDependencies(mapScreenViewModel, routesViewModel, searchViewModel)
    }
}

/**
 * Holder for MapScreen ViewModel dependencies.
 */
@Immutable
data class MapScreenDependencies(
    val mapScreenViewModel: MapScreenViewModel,
    val routesViewModel: RoutesViewModel,
    val searchViewModel: SearchViewModel,
)
