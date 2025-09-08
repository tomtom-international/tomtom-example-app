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
import com.tomtom.sdk.routing.common.BetaRoutePlanningOptionsApi
import com.tomtom.sdk.routing.createRoutePlanningOptions
import com.tomtom.sdk.routing.options.Itinerary
import com.tomtom.sdk.routing.options.RoutePlanningOptions
import com.tomtom.sdk.routing.route.Route
import com.tomtom.sdk.search.BetaSearchApi
import com.tomtom.sdk.search.BetaSearchFactoriesApi
import com.tomtom.sdk.search.Search
import com.tomtom.sdk.search.SearchOptions
import com.tomtom.sdk.search.SearchResponse
import com.tomtom.sdk.search.common.error.SearchFailure
import com.tomtom.sdk.search.createAlongRouteWithPoiCategorySearchOptions
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

@Suppress("detekt:LongParameterList")
@OptIn(BetaRoutePlanningOptionsApi::class)
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

    private val routePlanningOptions: RoutePlanningOptions = createRoutePlanningOptions(
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

    @OptIn(BetaSearchApi::class, BetaSearchFactoriesApi::class)
    fun onPoiCategoryClick(
        selectedRoute: Route,
        standardCategoryId: StandardCategoryId,
    ) {
        searchJob?.cancel()
        onSetIsLoading(true)
        searchJob = viewModelScope.launch {
            performSearch(
                createAlongRouteWithPoiCategorySearchOptions(
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
