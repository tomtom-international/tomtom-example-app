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

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.application.search.SearchResultItemContent
import com.example.application.search.toSearchResultItemContent
import com.tomtom.quantity.Distance
import com.tomtom.sdk.common.Result
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.poi.CategoryId
import com.tomtom.sdk.location.poi.StandardCategoryId
import com.tomtom.sdk.search.Area
import com.tomtom.sdk.search.BetaSearchApi
import com.tomtom.sdk.search.BetaSearchFactoriesApi
import com.tomtom.sdk.search.Search
import com.tomtom.sdk.search.SearchOptions
import com.tomtom.sdk.search.SearchResponse
import com.tomtom.sdk.search.common.error.SearchFailure
import com.tomtom.sdk.search.createPoiCategoryInAreaSearchOptions
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val DEFAULT_RADIUS_METERS = 500

class PoiSearchAreaViewModel(
    private val geoPoint: GeoPoint? = null,
    private val search: Search,
    private val onSearchSuccess: () -> Unit,
    private val onSearchFailure: (SearchFailure) -> Unit,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _searchResults: MutableStateFlow<SnapshotStateList<SearchResultItemContent>> =
        MutableStateFlow(mutableStateListOf())
    val searchResults: StateFlow<SnapshotStateList<SearchResultItemContent>> = _searchResults.asStateFlow()

    private var searchJob: Job? = null

    @OptIn(BetaSearchApi::class, BetaSearchFactoriesApi::class)
    fun onPoiCategoryClick(standardCategoryId: StandardCategoryId) {
        geoPoint?.let {
            searchJob?.cancel()
            searchJob = viewModelScope.launch {
                performSearch(
                    createPoiCategoryInAreaSearchOptions(
                        categoryIds = setOf(CategoryId(standardCategoryId)),
                        area = Area.Circle(it, Distance.meters(DEFAULT_RADIUS_METERS)),
                    ),
                )
            }
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

            onSearchSuccess()
        } else {
            Log.e(TAG, "Error performing search: ${searchResponse.failure()}")
            onSearchFailure(searchResponse.failure())
        }
    }

    companion object {
        private const val TAG = "PoiSearchAreaViewModel"
        val GEO_POINT_KEY = object : CreationExtras.Key<GeoPoint> {}
        val SEARCH_KEY = object : CreationExtras.Key<Search> {}
        val SEARCH_SUCCESS_KEY = object : CreationExtras.Key<() -> Unit> {}
        val SEARCH_FAILURE_KEY = object : CreationExtras.Key<(SearchFailure) -> Unit> {}

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                PoiSearchAreaViewModel(
                    geoPoint = this[GEO_POINT_KEY] as GeoPoint,
                    search = this[SEARCH_KEY] as Search,
                    onSearchSuccess = this[SEARCH_SUCCESS_KEY] as () -> Unit,
                    onSearchFailure = this[SEARCH_FAILURE_KEY] as (SearchFailure) -> Unit,
                )
            }
        }
    }
}
