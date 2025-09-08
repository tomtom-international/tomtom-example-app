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

package com.example.application.search

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.application.common.DEFAULT_DEBOUNCE_TIMEOUT
import com.tomtom.sdk.common.Result
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.location.poi.CategoryId
import com.tomtom.sdk.location.poi.StandardCategoryId
import com.tomtom.sdk.search.Search
import com.tomtom.sdk.search.SearchOptions
import com.tomtom.sdk.search.SearchResponse
import com.tomtom.sdk.search.buildPoiCategorySearchOptions
import com.tomtom.sdk.search.buildTextSearchOptions
import com.tomtom.sdk.search.common.error.SearchFailure
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Base ViewModel for search interactions.
 * Handles debounced text/POI searches, exposes results/state, and coordinates panel behavior.
 */
@Suppress("detekt:TooManyFunctions", "detekt:LongParameterList")
open class SearchViewModel(
    protected val search: Search,
    protected val locationProvider: LocationProvider,
    protected val onPoiSearchSuccess: (List<SearchResultItemContent>) -> Unit,
    protected val onSearchFailure: (SearchFailure) -> Unit,
    protected val cleanMap: () -> Unit,
    protected val toggleBottomSheet: (Boolean?) -> Unit,
    protected val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _searchPanelState: MutableStateFlow<SearchPanelState> =
        MutableStateFlow(SearchPanelState.Idle)
    val searchPanelState: StateFlow<SearchPanelState> = _searchPanelState.asStateFlow()

    private val _inputQuery: MutableStateFlow<String> = MutableStateFlow("")
    val inputQuery: StateFlow<String> = _inputQuery

    private var showingPoiResults: Boolean = false

    private val _searchResults: MutableStateFlow<SnapshotStateList<SearchResultItemContent>> =
        MutableStateFlow(mutableStateListOf())
    val searchResults: StateFlow<SnapshotStateList<SearchResultItemContent>> = _searchResults.asStateFlow()

    init {
        initializeSearchPanel()
    }

    protected fun setSearchPanelState(newState: SearchPanelState) {
        _searchPanelState.update { newState }
    }

    protected fun setSearchResults(newResults: List<SearchResultItemContent>) {
        _searchResults.value = mutableStateListOf<SearchResultItemContent>().apply {
            addAll(newResults)
        }
    }

    fun updateInputQuery(newInputQuery: String) {
        if (newInputQuery.isNotBlank()) {
            _inputQuery.update { newInputQuery }
            showingPoiResults = false

            if (inputQuery.value.isNotBlank()) {
                setSearchPanelState(SearchPanelState.Loading)
            }
        } else {
            clearSearch()
        }
    }

    fun newPoiInputQuery(
        categoryId: StandardCategoryId,
        poiCategory: String,
    ) {
        _inputQuery.update { poiCategory }
        showingPoiResults = true
        setSearchPanelState(SearchPanelState.Loading)
        performSearch(categoryId)
    }

    fun clearSearch() {
        setSearchPanelState(SearchPanelState.Idle)
        showingPoiResults = false
        _inputQuery.update { "" }
        cleanMap()
    }

    fun resetSearch() {
        clearSearch()
        toggleBottomSheet(false)
    }

    @OptIn(FlowPreview::class)
    fun initializeSearchPanel() {
        viewModelScope.launch {
            inputQuery.debounce(timeoutMillis = DEFAULT_DEBOUNCE_TIMEOUT).collectLatest { input ->

                if (input.isBlank()) {
                    setSearchPanelState(SearchPanelState.Idle)
                    return@collectLatest
                }

                if (showingPoiResults) {
                    return@collectLatest
                }

                performSearch(
                    buildTextSearchOptions(
                        query = input,
                        geoBias = locationProvider.lastKnownLocation?.position,
                    ),
                    onSearchSuccess = { },
                    onSearchFailure = onSearchFailure,
                )
            }
        }
    }

    fun performSearch(poiCategory: StandardCategoryId) {
        viewModelScope.launch {
            performSearch(
                buildPoiCategorySearchOptions(
                    geoBias = locationProvider.lastKnownLocation?.position,
                    categoryIds = setOf(CategoryId(poiCategory)),
                ),
                onSearchSuccess = onPoiSearchSuccess,
                onSearchFailure = onSearchFailure,
            )
        }
    }

    protected suspend fun performSearch(
        searchOptions: SearchOptions,
        onSearchSuccess: (List<SearchResultItemContent>) -> Unit,
        onSearchFailure: (SearchFailure) -> Unit,
    ) = withContext(ioDispatcher) {
        val searchResponse: Result<SearchResponse, SearchFailure> = search.search(searchOptions)

        if (searchResponse.isSuccess()) {
            val resultsProcessed = searchResponse.value().results
                .map { it.toSearchResultItemContent() }

            if (resultsProcessed.isEmpty()) {
                setSearchPanelState(SearchPanelState.NoResults)
            } else {
                setSearchPanelState(SearchPanelState.ResultsFetched)
            }

            setSearchResults(resultsProcessed)
            onSearchSuccess(searchResults.value)
        } else {
            Log.e(TAG, "Error performing search: ${searchResponse.failure()}")
            setSearchPanelState(SearchPanelState.Error)
            onSearchFailure(searchResponse.failure())
        }
    }

    companion object {
        private const val TAG = "SearchViewModel"
        val LOCATION_PROVIDER_KEY = object : CreationExtras.Key<LocationProvider> {}
        val POI_SEARCH_SUCCESS_KEY = object : CreationExtras.Key<(List<SearchResultItemContent>) -> Unit> {}
        val SEARCH_FAILURE_KEY = object : CreationExtras.Key<(SearchFailure) -> Unit> {}
        val SEARCH_KEY = object : CreationExtras.Key<Search> {}
        val CLEAN_MAP_KEY = object : CreationExtras.Key<() -> Unit> {}
        val TOGGLE_BOTTOM_SHEET_KEY = object : CreationExtras.Key<(Boolean?) -> Unit> {}

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SearchViewModel(
                    locationProvider = this[LOCATION_PROVIDER_KEY] as LocationProvider,
                    search = this[SEARCH_KEY] as Search,
                    onPoiSearchSuccess = this[POI_SEARCH_SUCCESS_KEY] as (List<SearchResultItemContent>) -> Unit,
                    onSearchFailure = this[SEARCH_FAILURE_KEY] as (SearchFailure) -> Unit,
                    cleanMap = this[CLEAN_MAP_KEY] as () -> Unit,
                    toggleBottomSheet = this[TOGGLE_BOTTOM_SHEET_KEY] as (Boolean?) -> Unit,
                )
            }
        }
    }
}
