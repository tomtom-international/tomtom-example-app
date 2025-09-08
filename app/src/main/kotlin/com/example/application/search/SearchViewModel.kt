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
import com.tomtom.sdk.search.BetaSearchApi
import com.tomtom.sdk.search.Search
import com.tomtom.sdk.search.SearchOptions
import com.tomtom.sdk.search.SearchResponse
import com.tomtom.sdk.search.common.error.SearchFailure
import com.tomtom.sdk.search.createPoiCategorySearchOptions
import com.tomtom.sdk.search.createTextSearchOptions
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

    @OptIn(FlowPreview::class, BetaSearchApi::class)
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
                    createTextSearchOptions(
                        query = input,
                        geoBias = locationProvider.lastKnownLocation?.position,
                    ),
                    onSearchSuccess = { },
                    onSearchFailure = onSearchFailure,
                )
            }
        }
    }

    @OptIn(BetaSearchApi::class)
    fun performSearch(poiCategory: StandardCategoryId) {
        viewModelScope.launch {
            performSearch(
                createPoiCategorySearchOptions(
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
