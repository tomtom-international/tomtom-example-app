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
import com.tomtom.sdk.common.Result
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.poi.CategoryId
import com.tomtom.sdk.location.poi.StandardCategoryId
import com.tomtom.sdk.search.Search
import com.tomtom.sdk.search.SearchOptions
import com.tomtom.sdk.search.SearchResponse
import com.tomtom.sdk.search.buildPoiCategorySearchOptions
import com.tomtom.sdk.search.common.error.SearchFailure
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for "POI in area" searches.
 * Runs category-in-area queries around a given point and displays resulting POIs in the area.
 */
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

    fun onPoiCategoryClick(standardCategoryId: StandardCategoryId) {
        geoPoint?.let {
            searchJob?.cancel()
            searchJob = viewModelScope.launch {
                performSearch(
                    buildPoiCategorySearchOptions(
                        categoryIds = setOf(CategoryId(standardCategoryId)),
                        geoBias = it,
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
