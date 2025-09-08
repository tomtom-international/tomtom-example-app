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

@file:Suppress("detekt:TooManyFunctions")

package com.example.application.search

import android.util.Log
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.application.common.EV_SEARCH_DEFAULT_RADIUS_KM
import com.example.application.common.PlaceDetails
import com.tomtom.quantity.Distance
import com.tomtom.sdk.common.Result
import com.tomtom.sdk.location.BetaLocationApi
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.search.AlphaEvSearchOptionsWithBuildersApi
import com.tomtom.sdk.search.BetaEvSearchApi
import com.tomtom.sdk.search.Search
import com.tomtom.sdk.search.common.error.SearchFailure
import com.tomtom.sdk.search.ev.EvSearchOptions
import com.tomtom.sdk.search.ev.EvSearchResponse
import com.tomtom.sdk.search.ev.evSearchOptionsForNearby
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class EvSearchViewModel(
    private val geoPoint: GeoPoint? = null,
    private val onSearchSuccess: (List<SearchResultItemContent>) -> Unit,
    onSearchFailure: (SearchFailure) -> Unit,
    search: Search,
    locationProvider: LocationProvider,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : SearchViewModel(
        search = search,
        locationProvider = locationProvider,
        ioDispatcher = ioDispatcher,
        onSearchFailure = onSearchFailure,
        onPoiSearchSuccess = {},
        cleanMap = {},
        toggleBottomSheet = {},
    ) {
    private val _evSearchUiState: MutableStateFlow<EvSearchUiState> = MutableStateFlow(EvSearchUiState())
    val evSearchUiState: StateFlow<EvSearchUiState> = _evSearchUiState.asStateFlow()

    fun setSelectedFilterCategory(category: EvFilterCategory?) {
        _evSearchUiState.update { currentState ->
            currentState.copy(selectedFilterCategory = category)
        }
    }

    fun setEvPoiFocusDetails(evPoiFocusDetails: PlaceDetails?) {
        _evSearchUiState.update { currentState ->
            currentState.copy(evPoiFocusDetails = evPoiFocusDetails)
        }
    }

    private val _activeFilters: MutableStateFlow<SnapshotStateMap<EvFilterCategory, Set<EvFilterOption>>> =
        MutableStateFlow(SnapshotStateMap())
    val activeFilters: StateFlow<SnapshotStateMap<EvFilterCategory, Set<EvFilterOption>>> = _activeFilters.asStateFlow()

    private fun getActiveFilters(evFilter: EvFilter): Set<EvFilterOption>? =
        _activeFilters.value[evFilterCategories[evFilter]]

    fun resetActiveFilters() {
        _activeFilters.update { SnapshotStateMap() }
        performEvSearch()
    }

    fun addActiveFilter(
        category: EvFilterCategory,
        filterOption: EvFilterOption,
    ) {
        if (category.allowsMultipleOptions) {
            _activeFilters.update { currentFilters ->
                val currentOptions = currentFilters[category] ?: emptySet()
                currentFilters[category] = currentOptions + filterOption
                currentFilters
            }
        } else {
            _activeFilters.update { currentFilters ->
                currentFilters[category] = setOf(filterOption)
                currentFilters
            }
        }
        performEvSearch()
    }

    fun removeActiveFilter(
        category: EvFilterCategory,
        filterOption: EvFilterOption,
    ) {
        if (category.allowsMultipleOptions) {
            _activeFilters.update { currentFilters ->
                val currentOptions = currentFilters[category] ?: emptySet()
                if (currentOptions.isEmpty() || (currentOptions - filterOption).isEmpty()) {
                    currentFilters.remove(category)
                } else {
                    currentFilters[category] = currentOptions - filterOption
                }
                currentFilters
            }
        } else {
            _activeFilters.update { currentFilters ->
                currentFilters.remove(category)
                currentFilters
            }
        }
        performEvSearch()
    }

    @OptIn(BetaEvSearchApi::class, BetaLocationApi::class, AlphaEvSearchOptionsWithBuildersApi::class)
    fun performEvSearch() {
        val powerFilter = getActiveFilters(EvFilter.CHARGING_SPEED)?.first() as? ChargingSpeedEvFilterOption
        val statusFilter = getActiveFilters(EvFilter.STATE)?.first() as? StatusEvFilterOption
        val connectorsFilter = getActiveFilters(EvFilter.CONNECTOR_TYPE)?.map {
            (it as ConnectorTypeEvFilterOption).connectorType
        }?.toList() ?: emptyList()
        val accessTypesFilter = getActiveFilters(EvFilter.ACCESS_TYPE)?.map {
            (it as AccessTypeEvFilterOption).accessType
        }?.toList() ?: emptyList()

        val geoBias = geoPoint ?: locationProvider.lastKnownLocation?.position
        geoBias?.let {
            viewModelScope.launch {
                performEvSearch(
                    searchApi = search,
                    evSearchOptions = evSearchOptionsForNearby(
                        geoBias = it,
                        init = {
                            radius = Distance.kilometers(EV_SEARCH_DEFAULT_RADIUS_KM)
                            minPower = powerFilter?.minPower
                            maxPower = powerFilter?.maxPower
                            connectors = connectorsFilter
                            status = statusFilter?.status
                            accessTypes = accessTypesFilter
                        },
                    ),
                )
            }
        }
    }

    @OptIn(BetaEvSearchApi::class)
    private suspend fun performEvSearch(
        searchApi: Search,
        evSearchOptions: EvSearchOptions,
    ) = withContext(ioDispatcher) {
        try {
            val searchResponse: Result<EvSearchResponse, SearchFailure> = searchApi.evSearch(evSearchOptions)

            if (searchResponse.isSuccess()) {
                val resultsProcessed = searchResponse.value().results
                    .map { it.toSearchResultItemContent(evSearchOptions.geoBias) }

                if (resultsProcessed.isEmpty()) {
                    setSearchPanelState(SearchPanelState.NoResults)
                } else {
                    setSearchPanelState(SearchPanelState.ResultsFetched)
                }
                setSearchResults(resultsProcessed)
                onSearchSuccess(resultsProcessed)
            } else {
                setSearchPanelState(SearchPanelState.Error)
                onSearchFailure(searchResponse.failure())
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error performing search", e)
            setSearchPanelState(SearchPanelState.Error)
        }
    }

    companion object {
        val LOCATION_PROVIDER_KEY = object : CreationExtras.Key<LocationProvider> {}
        val SEARCH_KEY = object : CreationExtras.Key<Search> {}
        val GEO_POINT_KEY = object : CreationExtras.Key<GeoPoint> {}
        val SEARCH_SUCCESS_KEY = object : CreationExtras.Key<(List<SearchResultItemContent>) -> Unit> {}
        val SEARCH_FAILURE_KEY = object : CreationExtras.Key<(SearchFailure) -> Unit> {}

        private const val TAG = "EvSearchViewModel"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                EvSearchViewModel(
                    locationProvider = this[LOCATION_PROVIDER_KEY] as LocationProvider,
                    search = this[SEARCH_KEY] as Search,
                    geoPoint = this[GEO_POINT_KEY] as GeoPoint,
                    onSearchSuccess = this[SEARCH_SUCCESS_KEY] as (List<SearchResultItemContent>) -> Unit,
                    onSearchFailure = this[SEARCH_FAILURE_KEY] as (SearchFailure) -> Unit,
                )
            }
        }
    }
}
