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
import com.example.application.common.DEFAULT_DEBOUNCE_TIMEOUT
import com.tomtom.sdk.common.Result
import com.tomtom.sdk.location.Address
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.location.Place
import com.tomtom.sdk.location.poi.CategoryId
import com.tomtom.sdk.location.poi.Poi
import com.tomtom.sdk.location.poi.Source
import com.tomtom.sdk.location.poi.StandardCategoryId
import com.tomtom.sdk.search.Search
import com.tomtom.sdk.search.SearchResponse
import com.tomtom.sdk.search.common.error.SearchFailure
import com.tomtom.sdk.search.model.SearchResultType
import com.tomtom.sdk.search.model.result.SearchResult
import com.tomtom.sdk.search.model.result.SearchResultId
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

// Delay used to wait the debounce timeout, after updating the input query
private const val DELAY_DEBOUNCE_TIMEOUT = 400L

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    private lateinit var searchApi: Search

    @MockK
    private lateinit var locationProvider: LocationProvider

    private lateinit var viewModel: SearchViewModel

    @Before
    fun setup() {
        every { searchApi.search(any()) } returns Result.success(
            mockk<SearchResponse>().apply {
                every { results } returns emptyList()
            },
        )
        every { locationProvider.lastKnownLocation } returns null

        val dispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(dispatcher)

        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0

        viewModel = SearchViewModel(
            search = searchApi,
            locationProvider = locationProvider,
            ioDispatcher = dispatcher,
            onPoiSearchSuccess = { },
            onSearchFailure = { },
            cleanMap = { },
            toggleBottomSheet = { },
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when a new input query is received, inputQuery is updated and the search panel state is set to Loading`() {
        val newInputQuery = "test query"
        viewModel.updateInputQuery(newInputQuery)

        assertEquals(newInputQuery, viewModel.inputQuery.value)
        assertEquals(SearchPanelState.Loading, viewModel.searchPanelState.value)
    }

    @Test
    fun `when clearSearch is called, searchViewModel is updated accordingly`() {
        val newInputQuery = "test query"

        viewModel.updateInputQuery(newInputQuery)

        assertEquals(newInputQuery, viewModel.inputQuery.value)
        assertEquals(SearchPanelState.Loading, viewModel.searchPanelState.value)

        viewModel.clearSearch()
        assertEquals("", viewModel.inputQuery.value)
        assertEquals(SearchPanelState.Idle, viewModel.searchPanelState.value)
    }

    @Test
    fun `when input query is empty, search panel state is set to Idle`() {
        viewModel.updateInputQuery("")

        assertEquals("", viewModel.inputQuery.value)
        assertEquals(SearchPanelState.Idle, viewModel.searchPanelState.value)
    }

    @Test
    fun `when search is performed and results are empty, search panel state is set to NoResults`() = runTest {
        every { locationProvider.lastKnownLocation } returns null

        backgroundScope.launch { viewModel.initializeSearchPanel() }

        viewModel.updateInputQuery("test query")
        advanceTimeBy(DEFAULT_DEBOUNCE_TIMEOUT + DELAY_DEBOUNCE_TIMEOUT)

        assertEquals(SearchPanelState.NoResults, viewModel.searchPanelState.value)
    }

    @Test
    fun `when search is performed and there is an error, search panel state is set to Error`() = runTest {
        every { searchApi.search(any()) } returns Result.failure(SearchFailure.NetworkFailure("test error"))
        every { locationProvider.lastKnownLocation } returns null

        backgroundScope.launch { viewModel.initializeSearchPanel() }

        viewModel.updateInputQuery("test query")
        advanceTimeBy(DEFAULT_DEBOUNCE_TIMEOUT + DELAY_DEBOUNCE_TIMEOUT)

        assertEquals(SearchPanelState.Error, viewModel.searchPanelState.value)
    }

    @Test
    fun `when an empty query is received, search panel state is set to idle`() = runTest {
        every { searchApi.search(any()) } returns Result.failure(mockk<SearchFailure>())
        every { locationProvider.lastKnownLocation } returns null

        backgroundScope.launch { viewModel.initializeSearchPanel() }

        viewModel.updateInputQuery("")
        advanceTimeBy(DEFAULT_DEBOUNCE_TIMEOUT + DELAY_DEBOUNCE_TIMEOUT)

        assertEquals(SearchPanelState.Idle, viewModel.searchPanelState.value)
    }

    @Test
    fun `when search is received, search panel state is set to Loading`() = runTest {
        every { locationProvider.lastKnownLocation } returns null

        backgroundScope.launch { viewModel.initializeSearchPanel() }

        viewModel.updateInputQuery("test query")
        advanceTimeBy(DEFAULT_DEBOUNCE_TIMEOUT - DELAY_DEBOUNCE_TIMEOUT)

        assertEquals(SearchPanelState.Loading, viewModel.searchPanelState.value)
    }

    @Test
    fun `when search is performed and results are received, search panel state is set to ResultsFetched`() = runTest {
        val searchQuery = "test query"
        every { locationProvider.lastKnownLocation } returns null
        every { searchApi.search(any()) } returns Result.success(
            mockk<SearchResponse>().apply {
                every { results } returns listOf(
                    SearchResult(
                        type = SearchResultType.Address,
                        searchResultId = SearchResultId("1", Source.Online),
                        place = Place(
                            coordinate = mockk<GeoPoint>(),
                            address = Address(
                                countryCodeIso3 = "ESP",
                                freeformAddress = "Avila, Spain",
                            ),
                        ),
                    ),
                    SearchResult(
                        type = SearchResultType.Address,
                        searchResultId = SearchResultId("1", Source.Online),
                        place = Place(
                            coordinate = mockk<GeoPoint>(),
                            address = Address(
                                countryCodeIso3 = "ITA",
                                freeformAddress = "Napoli, Italy",
                            ),
                        ),
                    ),
                )
            },
        )

        backgroundScope.launch { viewModel.initializeSearchPanel() }

        viewModel.updateInputQuery(searchQuery)
        advanceTimeBy(DEFAULT_DEBOUNCE_TIMEOUT + DELAY_DEBOUNCE_TIMEOUT)

        assertEquals(2, viewModel.searchResults.value.size)
        assertEquals(SearchPanelState.ResultsFetched, viewModel.searchPanelState.value)
    }

    @Test
    fun `when POI search is performed and results are empty, search panel state is set to NoResults`() = runTest {
        val newInputQuery = "test query"
        every { locationProvider.lastKnownLocation } returns null

        viewModel.newPoiInputQuery(StandardCategoryId.GasStation, newInputQuery)
        viewModel.performSearch(StandardCategoryId.GasStation)

        assertEquals(SearchPanelState.NoResults, viewModel.searchPanelState.value)
        assertEquals(newInputQuery, viewModel.inputQuery.value)
    }

    @Test
    fun `when POI search is performed and there is an error, search panel state is set to Error`() = runTest {
        every { searchApi.search(any()) } returns Result.failure(mockk<SearchFailure>())
        every { locationProvider.lastKnownLocation } returns null

        backgroundScope.launch { viewModel.initializeSearchPanel() }

        viewModel.newPoiInputQuery(StandardCategoryId.GasStation, "Gas station")
        viewModel.performSearch(StandardCategoryId.GasStation)

        advanceTimeBy(DEFAULT_DEBOUNCE_TIMEOUT + DELAY_DEBOUNCE_TIMEOUT)

        assertEquals(SearchPanelState.Error, viewModel.searchPanelState.value)
    }

    @Test
    fun `when POI search is performed and results are received, panel state is set to ResultsFetched`() = runTest {
        val searchQuery = "test query"
        every { locationProvider.lastKnownLocation } returns null
        every { searchApi.search(any()) } returns Result.success(
            mockk<SearchResponse>().apply {
                every { results } returns listOf(
                    SearchResult(
                        type = SearchResultType.Poi,
                        searchResultId = SearchResultId("1", Source.Online),
                        place = Place(
                            coordinate = mockk<GeoPoint>(),
                            address = Address(
                                countryCodeIso3 = "ESP",
                                freeformAddress = "Avila, Spain",
                            ),
                        ),
                        poi = Poi(
                            names = setOf("Repsol", "Gas Station"),
                            categoryIds = setOf(CategoryId(StandardCategoryId.GasStation)),
                        ),
                    ),
                    SearchResult(
                        type = SearchResultType.Poi,
                        searchResultId = SearchResultId("1", Source.Online),
                        place = Place(
                            coordinate = mockk<GeoPoint>(),
                            address = Address(
                                countryCodeIso3 = "ITA",
                                freeformAddress = "Napoli, Italy",
                            ),
                        ),
                        poi = Poi(
                            names = setOf("Eni", "Gas Italia"),
                            categoryIds = setOf(CategoryId(StandardCategoryId.GasStation)),
                        ),
                    ),
                )
            },
        )

        viewModel.newPoiInputQuery(StandardCategoryId.GasStation, searchQuery)
        viewModel.performSearch(StandardCategoryId.GasStation)

        assertEquals(2, viewModel.searchResults.value.size)
        assertEquals(SearchPanelState.ResultsFetched, viewModel.searchPanelState.value)
    }
}
