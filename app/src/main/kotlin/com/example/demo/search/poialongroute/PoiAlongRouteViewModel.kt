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

package com.example.demo.search.poialongroute

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.application.common.ROTTERDAM
import com.example.application.common.TOMTOM_AMSTERDAM_OFFICE
import com.example.application.search.SearchResultItemContent
import com.example.application.search.toSearchResultItemContent
import com.tomtom.sdk.common.Result
import com.tomtom.sdk.location.poi.CategoryId
import com.tomtom.sdk.location.poi.StandardCategoryId
import com.tomtom.sdk.routing.RoutePlanner
import com.tomtom.sdk.routing.RoutePlanningCallback
import com.tomtom.sdk.routing.RoutePlanningResponse
import com.tomtom.sdk.routing.RoutingFailure
import com.tomtom.sdk.routing.buildRoutePlanningOptions
import com.tomtom.sdk.routing.options.Itinerary
import com.tomtom.sdk.routing.options.RoutePlanningOptions
import com.tomtom.sdk.routing.route.Route
import com.tomtom.sdk.search.Search
import com.tomtom.sdk.search.SearchOptions
import com.tomtom.sdk.search.SearchResponse
import com.tomtom.sdk.search.buildAlongRouteWithPoiCategorySearchOptions
import com.tomtom.sdk.search.common.error.SearchFailure
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.minutes

const val MAX_DETOUR_DURATION = 5

/**
 * ViewModel for "POI along route" searches.
 * Plans a base route then runs along-route searches, displaying resulting POIs along the route.
 */
@Suppress("detekt:LongParameterList")
class PoiAlongRouteViewModel(
    private val routePlanner: RoutePlanner,
    private val onRoutePlanningSuccess: (RoutePlanningResponse) -> Unit,
    private val onRoutePlanningFailure: (RoutingFailure) -> Unit,
    private val onSearchFailure: (SearchFailure) -> Unit,
    private val onSetIsLoading: (Boolean) -> Unit,
    private val search: Search,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _searchResults: MutableStateFlow<SnapshotStateList<SearchResultItemContent>> =
        MutableStateFlow(mutableStateListOf())
    val searchResults: StateFlow<SnapshotStateList<SearchResultItemContent>> = _searchResults.asStateFlow()

    private var searchJob: Job? = null

    private val routePlanningOptions: RoutePlanningOptions = buildRoutePlanningOptions(
        itinerary = Itinerary(TOMTOM_AMSTERDAM_OFFICE, ROTTERDAM),
    )

    init {
        onSetIsLoading(true)
        routePlanner.planRoute(
            routePlanningOptions = routePlanningOptions,
            object : RoutePlanningCallback {
                override fun onSuccess(result: RoutePlanningResponse) {
                    onRoutePlanningSuccess(result)
                }

                override fun onFailure(failure: RoutingFailure) {
                    onRoutePlanningFailure(failure)
                }
            },
        )
    }

    fun onPoiCategoryClick(
        selectedRoute: Route,
        standardCategoryId: StandardCategoryId,
    ) {
        searchJob?.cancel()
        onSetIsLoading(true)
        searchJob = viewModelScope.launch {
            performSearch(
                buildAlongRouteWithPoiCategorySearchOptions(
                    polyline = selectedRoute.routePoints.map { it.coordinate },
                    categoryIds = setOf(CategoryId(standardCategoryId)),
                    maxDetourDuration = MAX_DETOUR_DURATION.minutes,
                ),
            )
        }
    }

    private suspend fun performSearch(searchOptions: SearchOptions) = withContext(ioDispatcher) {
        val searchResponse: Result<SearchResponse, SearchFailure> = search.search(searchOptions)

        if (searchResponse.isSuccess()) {
            val resultsProcessed = searchResponse.value().results
                .map { it.toSearchResultItemContent() }

            _searchResults.value = mutableStateListOf<SearchResultItemContent>().apply {
                addAll(resultsProcessed)
            }
            onSetIsLoading(false)
        } else {
            Log.e(TAG, "Error performing search: ${searchResponse.failure()}")
            onSearchFailure(searchResponse.failure())
            onSetIsLoading(false)
        }
    }

    companion object {
        private const val TAG = "PoiAlongRouteViewModel"
        val ROUTE_PLANNER_KEY = object : CreationExtras.Key<RoutePlanner> {}
        val ROUTE_PLANNING_SUCCESS_KEY =
            object : CreationExtras.Key<(RoutePlanningResponse) -> Unit> {}
        val ROUTE_PLANNING_FAILURE_KEY = object : CreationExtras.Key<(RoutingFailure) -> Unit> {}
        val SEARCH_KEY = object : CreationExtras.Key<Search> {}
        val SEARCH_FAILURE_KEY = object : CreationExtras.Key<(SearchFailure) -> Unit> {}
        val ON_SET_IS_LOADING_KEY = object : CreationExtras.Key<(Boolean) -> Unit> {}

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                PoiAlongRouteViewModel(
                    routePlanner = this[ROUTE_PLANNER_KEY] as RoutePlanner,
                    onRoutePlanningSuccess = this[ROUTE_PLANNING_SUCCESS_KEY] as (RoutePlanningResponse) -> Unit,
                    onRoutePlanningFailure = this[ROUTE_PLANNING_FAILURE_KEY] as (RoutingFailure) -> Unit,
                    onSearchFailure = this[SEARCH_FAILURE_KEY] as (SearchFailure) -> Unit,
                    onSetIsLoading = this[ON_SET_IS_LOADING_KEY] as (Boolean) -> Unit,
                    search = this[SEARCH_KEY] as Search,
                )
            }
        }
    }
}
