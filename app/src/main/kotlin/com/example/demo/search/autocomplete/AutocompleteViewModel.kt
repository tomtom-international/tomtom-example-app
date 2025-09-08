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

package com.example.demo.search.autocomplete

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
import com.example.application.common.TOMTOM_AMSTERDAM_OFFICE
import com.example.application.search.SearchResultItemContent
import com.example.application.search.toSearchResultItemContent
import com.tomtom.quantity.Distance
import com.tomtom.sdk.common.Result
import com.tomtom.sdk.search.BetaSearchApi
import com.tomtom.sdk.search.BetaSearchFactoriesApi
import com.tomtom.sdk.search.Search
import com.tomtom.sdk.search.SearchOptions
import com.tomtom.sdk.search.SearchResponse
import com.tomtom.sdk.search.autocomplete.AutocompleteOptions
import com.tomtom.sdk.search.autocomplete.AutocompleteResponse
import com.tomtom.sdk.search.common.error.SearchFailure
import com.tomtom.sdk.search.createAutocompleteOptions
import com.tomtom.sdk.search.createBrandSearchOptions
import com.tomtom.sdk.search.createPoiCategorySearchOptions
import com.tomtom.sdk.search.createTextSearchOptions
import com.tomtom.sdk.search.model.result.AutocompleteSegment
import com.tomtom.sdk.search.model.result.AutocompleteSegmentBrand
import com.tomtom.sdk.search.model.result.AutocompleteSegmentPlainText
import com.tomtom.sdk.search.model.result.AutocompleteSegmentPoiCategory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(FlowPreview::class, BetaSearchApi::class, BetaSearchFactoriesApi::class)
class AutocompleteViewModel(
    private val search: Search,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _searchResults: MutableStateFlow<SnapshotStateList<SearchResultItemContent>> =
        MutableStateFlow(mutableStateListOf())
    val searchResults: StateFlow<SnapshotStateList<SearchResultItemContent>> = _searchResults.asStateFlow()

    private val _autocompleteResults: MutableStateFlow<SnapshotStateList<AutocompleteResultItemContent>> =
        MutableStateFlow(mutableStateListOf())
    val autocompleteResults: StateFlow<SnapshotStateList<AutocompleteResultItemContent>> =
        _autocompleteResults.asStateFlow()

    private val _inputQuery: MutableStateFlow<String> = MutableStateFlow("")
    val inputQuery: StateFlow<String> = _inputQuery

    private var isSelectingAutocompleteOption = false

    init {
        startListeningToInputQueryChanges()
    }

    fun setInputQuery(query: String) {
        _inputQuery.value = query
    }

    fun clearAutocompleteResults() {
        _autocompleteResults.value.clear()
    }

    fun selectAutocompleteOption(
        segment: AutocompleteSegment,
        onSearchSuccess: () -> Unit,
        onSearchFailure: (SearchFailure) -> Unit,
    ) {
        isSelectingAutocompleteOption = true
        clearAutocompleteResults()

        viewModelScope.launch {
            val searchOptions = when (segment) {
                is AutocompleteSegmentBrand -> {
                    createBrandSearchOptions(segment.brand, geoBias = TOMTOM_AMSTERDAM_OFFICE)
                }

                is AutocompleteSegmentPoiCategory -> {
                    createPoiCategorySearchOptions(
                        categoryIds = setOf(segment.poiCategory.id),
                        geoBias = TOMTOM_AMSTERDAM_OFFICE,
                    )
                }

                is AutocompleteSegmentPlainText -> {
                    createTextSearchOptions(segment.plainText, geoBias = TOMTOM_AMSTERDAM_OFFICE)
                }

                else -> null
            }

            searchOptions?.let {
                performSearch(searchOptions = it, onSearchSuccess = onSearchSuccess, onSearchFailure = onSearchFailure)
            }
        }
    }

    fun performSearch(
        query: String,
        onSearchSuccess: () -> Unit,
        onSearchFailure: (SearchFailure) -> Unit,
    ) {
        val searchOptions = createTextSearchOptions(query, geoBias = TOMTOM_AMSTERDAM_OFFICE)
        viewModelScope.launch {
            performSearch(searchOptions, onSearchSuccess, onSearchFailure)
        }
    }

    private fun startListeningToInputQueryChanges() {
        viewModelScope.launch {
            inputQuery.debounce(timeoutMillis = DEFAULT_DEBOUNCE_TIMEOUT).collectLatest { input ->
                if (isSelectingAutocompleteOption) {
                    isSelectingAutocompleteOption = false
                    return@collectLatest
                }

                if (input.isBlank()) {
                    _autocompleteResults.value.clear()
                } else {
                    autocomplete(
                        createAutocompleteOptions(
                            query = input,
                            geoBias = TOMTOM_AMSTERDAM_OFFICE,
                        ),
                    )
                }
            }
        }
    }

    private suspend fun autocomplete(autocompleteOptions: AutocompleteOptions) = withContext(ioDispatcher) {
        val autocompleteResponse: Result<AutocompleteResponse, SearchFailure> =
            search.autocompleteSearch(autocompleteOptions)

        if (autocompleteResponse.isSuccess()) {
            val resultsProcessed =
                autocompleteResponse.value().results.flatMap {
                    it.segments.map { segment ->
                        segment.toAutocompleteResultItemContent()
                    }
                }
            _autocompleteResults.value = mutableStateListOf<AutocompleteResultItemContent>().apply {
                addAll(resultsProcessed)
            }
        } else {
            Log.e(TAG, "Error performing autocomplete: ${autocompleteResponse.failure()}")
        }
    }

    private suspend fun performSearch(
        searchOptions: SearchOptions,
        onSearchSuccess: () -> Unit,
        onSearchFailure: (SearchFailure) -> Unit,
    ) = withContext(ioDispatcher) {
        val searchResponse: Result<SearchResponse, SearchFailure> = search.search(searchOptions)
        if (searchResponse.isSuccess()) {
            val resultsProcessed = searchResponse.value().results.sortedBy { evResult ->
                searchOptions.geoBias?.distanceTo(evResult.place.coordinate) ?: Distance.meters(0)
            }.map {
                it.toSearchResultItemContent()
            }

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
        private const val TAG = "AutocompleteViewModel"
        val SEARCH_KEY = object : CreationExtras.Key<Search> {}

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AutocompleteViewModel(
                    search = this[SEARCH_KEY] as Search,
                )
            }
        }
    }
}
