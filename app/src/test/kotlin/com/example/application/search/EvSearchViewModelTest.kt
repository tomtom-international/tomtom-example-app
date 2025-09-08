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

import com.example.application.common.TOMTOM_AMSTERDAM_OFFICE
import com.tomtom.quantity.Distance
import com.tomtom.sdk.common.Result
import com.tomtom.sdk.location.BetaLocationApi
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.location.Place
import com.tomtom.sdk.location.poi.CategoryId
import com.tomtom.sdk.location.poi.Poi
import com.tomtom.sdk.location.poi.StandardCategoryId
import com.tomtom.sdk.search.BetaEvSearchApi
import com.tomtom.sdk.search.BetaNearbyEvSearchApi
import com.tomtom.sdk.search.Search
import com.tomtom.sdk.search.common.error.SearchFailure
import com.tomtom.sdk.search.ev.EvSearchResponse
import com.tomtom.sdk.search.model.result.EvSearchResult
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EvSearchViewModelTest {
    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    private lateinit var searchApi: Search

    @MockK
    private lateinit var locationProvider: LocationProvider

    private lateinit var viewModel: EvSearchViewModel

    @OptIn(BetaEvSearchApi::class)
    @Before
    fun setup() {
        every { searchApi.evSearch(any()) } returns Result.success(
            EvSearchResponse(
                results = emptyList(),
            ),
        )

        val dispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(dispatcher)

        viewModel = EvSearchViewModel(
            search = searchApi,
            locationProvider = locationProvider,
            geoPoint = TOMTOM_AMSTERDAM_OFFICE,
            onSearchSuccess = {},
            onSearchFailure = { _ -> },
            ioDispatcher = dispatcher,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @OptIn(BetaEvSearchApi::class)
    @Test
    fun `when ev search is performed and results are empty, search panel state is set to NoResults`() = runTest {
        every { searchApi.evSearch(any()) } returns Result.success(
            EvSearchResponse(
                results = emptyList(),
            ),
        )
        every { locationProvider.lastKnownLocation } returns null

        viewModel.performEvSearch()

        Assert.assertEquals(SearchPanelState.NoResults, viewModel.searchPanelState.value)
    }

    @OptIn(BetaEvSearchApi::class)
    @Test
    fun `when ev search is performed and there is an error, search panel state is set to Error`() = runTest {
        every { searchApi.evSearch(any()) } returns Result.failure(SearchFailure.NetworkFailure("test error"))
        every { locationProvider.lastKnownLocation } returns null

        viewModel.performEvSearch()

        Assert.assertEquals(SearchPanelState.Error, viewModel.searchPanelState.value)
    }

    @OptIn(BetaEvSearchApi::class)
    @Test
    fun `when ev search is performed and results are received, search panel state is set to ResultsFetched`() =
        runTest {
            every { locationProvider.lastKnownLocation } returns null
            every { searchApi.evSearch(any()) } returns Result.success(EvSearchResponse(results = searchResults))

            viewModel.performEvSearch()

            Assert.assertEquals(2, viewModel.searchResults.value.size)
            Assert.assertEquals(SearchPanelState.ResultsFetched, viewModel.searchPanelState.value)
        }

    @Test
    fun `when a new category is set, the ev search ui state is updated`() = runTest {
        viewModel.setSelectedFilterCategory(evFilterCategories[EvFilter.CHARGING_SPEED])

        Assert.assertEquals(
            evFilterCategories[EvFilter.CHARGING_SPEED],
            viewModel.evSearchUiState.value.selectedFilterCategory,
        )
    }

    @OptIn(BetaEvSearchApi::class)
    @Test
    fun `when active filters are reset, the active filters state is cleared`() = runTest {
        every { searchApi.evSearch(any()) } returns Result.success(
            EvSearchResponse(
                results = emptyList(),
            ),
        )
        every { locationProvider.lastKnownLocation } returns null

        viewModel.resetActiveFilters()
        Assert.assertTrue(viewModel.activeFilters.value.isEmpty())
    }

    @OptIn(BetaEvSearchApi::class)
    @Test
    fun `when active filters are set, the active filters state is updated`() = runTest {
        every { searchApi.evSearch(any()) } returns Result.success(
            EvSearchResponse(
                results = emptyList(),
            ),
        )
        every { locationProvider.lastKnownLocation } returns null

        val filterCategory = evFilterCategories[EvFilter.CHARGING_SPEED]
        val filterOptions = filterCategory?.filterOptions?.get(0)

        if (filterCategory != null && filterOptions != null) {
            viewModel.addActiveFilter(
                category = filterCategory,
                filterOption = filterOptions,
            )
            Assert.assertEquals(filterOptions, viewModel.activeFilters.value[filterCategory]?.firstOrNull())
        }
    }

    @OptIn(BetaEvSearchApi::class)
    @Test
    fun `when active filters are updated + the category does not allow multiple options, the option is replaced`() =
        runTest {
            every { searchApi.evSearch(any()) } returns Result.success(
                EvSearchResponse(
                    results = emptyList(),
                ),
            )
            every { locationProvider.lastKnownLocation } returns null

            val filterCategory = evFilterCategories[EvFilter.CHARGING_SPEED]
            val filterOptions = filterCategory?.filterOptions?.get(0)
            val updatedFilterOptions = filterCategory?.filterOptions?.get(1)

            if (filterCategory != null && filterOptions != null && updatedFilterOptions != null) {
                viewModel.addActiveFilter(
                    category = filterCategory,
                    filterOption = filterOptions,
                )
                Assert.assertEquals(filterOptions, viewModel.activeFilters.value[filterCategory]?.firstOrNull())
                Assert.assertEquals(1, viewModel.activeFilters.value[filterCategory]?.size)

                viewModel.addActiveFilter(
                    category = filterCategory,
                    filterOption = updatedFilterOptions,
                )
                Assert.assertEquals(updatedFilterOptions, viewModel.activeFilters.value[filterCategory]?.firstOrNull())
                Assert.assertEquals(1, viewModel.activeFilters.value[filterCategory]?.size)
            }
        }

    @OptIn(BetaEvSearchApi::class)
    @Test
    fun `when active filters are updated + the category allows multiple options, the option is added`() = runTest {
        every { searchApi.evSearch(any()) } returns Result.success(
            EvSearchResponse(
                results = emptyList(),
            ),
        )
        every { locationProvider.lastKnownLocation } returns null

        val filterCategory = evFilterCategories[EvFilter.CONNECTOR_TYPE]
        val filterOptions = filterCategory?.filterOptions?.get(0)
        val newFilterOptions = filterCategory?.filterOptions?.get(1)

        if (filterCategory != null && filterOptions != null && newFilterOptions != null) {
            viewModel.addActiveFilter(
                category = filterCategory,
                filterOption = filterOptions,
            )
            Assert.assertEquals(filterOptions, viewModel.activeFilters.value[filterCategory]?.firstOrNull())
            Assert.assertEquals(1, viewModel.activeFilters.value[filterCategory]?.size)

            viewModel.addActiveFilter(
                category = filterCategory,
                filterOption = newFilterOptions,
            )
            Assert.assertEquals(2, viewModel.activeFilters.value[filterCategory]?.size)
        }
    }

    @OptIn(BetaEvSearchApi::class)
    @Test
    fun `when an active filter is removed + the category does not allow multiple options, the category is removed`() =
        runTest {
            every { searchApi.evSearch(any()) } returns Result.success(
                EvSearchResponse(
                    results = emptyList(),
                ),
            )
            every { locationProvider.lastKnownLocation } returns null

            val filterCategory = evFilterCategories[EvFilter.CHARGING_SPEED]
            val filterOptions = filterCategory?.filterOptions?.get(0)

            if (filterCategory != null && filterOptions != null) {
                viewModel.addActiveFilter(
                    category = filterCategory,
                    filterOption = filterOptions,
                )
                Assert.assertEquals(filterOptions, viewModel.activeFilters.value[filterCategory]?.firstOrNull())
                Assert.assertEquals(1, viewModel.activeFilters.value[filterCategory]?.size)

                viewModel.removeActiveFilter(
                    category = filterCategory,
                    filterOption = filterOptions,
                )
                Assert.assertNull(viewModel.activeFilters.value[filterCategory]?.firstOrNull())
            }
        }

    @OptIn(BetaEvSearchApi::class)
    @Test
    fun `active filter removed + category allows multiple options + two active filters, the option is removed`() =
        runTest {
            every { searchApi.evSearch(any()) } returns Result.success(
                EvSearchResponse(
                    results = emptyList(),
                ),
            )
            every { locationProvider.lastKnownLocation } returns null

            val filterCategory = evFilterCategories[EvFilter.CONNECTOR_TYPE]
            val filterOptions = filterCategory?.filterOptions?.get(0)
            val newFilterOptions = filterCategory?.filterOptions?.get(1)

            if (filterCategory != null && filterOptions != null && newFilterOptions != null) {
                viewModel.addActiveFilter(
                    category = filterCategory,
                    filterOption = filterOptions,
                )
                Assert.assertEquals(filterOptions, viewModel.activeFilters.value[filterCategory]?.firstOrNull())
                Assert.assertEquals(1, viewModel.activeFilters.value[filterCategory]?.size)

                viewModel.addActiveFilter(
                    category = filterCategory,
                    filterOption = newFilterOptions,
                )
                Assert.assertEquals(2, viewModel.activeFilters.value[filterCategory]?.size)

                viewModel.removeActiveFilter(
                    category = filterCategory,
                    filterOption = filterOptions,
                )
                Assert.assertEquals(newFilterOptions, viewModel.activeFilters.value[filterCategory]?.firstOrNull())
                Assert.assertEquals(1, viewModel.activeFilters.value[filterCategory]?.size)
            }
        }

    @OptIn(BetaEvSearchApi::class)
    @Test
    fun `one active filter + active filter removed + category allows multiple options, the category is removed`() =
        runTest {
            every { searchApi.evSearch(any()) } returns Result.success(
                EvSearchResponse(
                    results = emptyList(),
                ),
            )
            every { locationProvider.lastKnownLocation } returns null

            val filterCategory = evFilterCategories[EvFilter.CONNECTOR_TYPE]
            val filterOptions = filterCategory?.filterOptions?.get(0)

            if (filterCategory != null && filterOptions != null) {
                viewModel.addActiveFilter(
                    category = filterCategory,
                    filterOption = filterOptions,
                )
                Assert.assertEquals(filterOptions, viewModel.activeFilters.value[filterCategory]?.firstOrNull())
                Assert.assertEquals(1, viewModel.activeFilters.value[filterCategory]?.size)

                viewModel.removeActiveFilter(
                    category = filterCategory,
                    filterOption = filterOptions,
                )
                Assert.assertNull(viewModel.activeFilters.value[filterCategory]?.firstOrNull())
            }
        }

    @OptIn(BetaEvSearchApi::class)
    @Test
    fun `ev filters active + search is performed + results are empty, search panel state is set to NoResults`() =
        runTest {
            every { searchApi.evSearch(any()) } returns Result.success(
                EvSearchResponse(
                    results = emptyList(),
                ),
            )
            every { locationProvider.lastKnownLocation } returns null

            setAllActiveFilters(viewModel)

            Assert.assertEquals(4, viewModel.activeFilters.value.size)

            viewModel.performEvSearch()

            Assert.assertEquals(SearchPanelState.NoResults, viewModel.searchPanelState.value)
        }

    @OptIn(BetaEvSearchApi::class)
    @Test
    fun `ev filters are active + ev search is performed + there is an error, search panel state is set to Error`() =
        runTest {
            every { searchApi.evSearch(any()) } returns Result.failure(SearchFailure.NetworkFailure("test error"))
            every { locationProvider.lastKnownLocation } returns null

            setAllActiveFilters(viewModel)

            Assert.assertEquals(4, viewModel.activeFilters.value.size)

            viewModel.performEvSearch()

            Assert.assertEquals(SearchPanelState.Error, viewModel.searchPanelState.value)
        }

    @Suppress("detekt:LongMethod")
    @OptIn(BetaEvSearchApi::class)
    @Test
    fun `ev filters active + ev search performed + results are received, panel state is set to ResultsFetched`() =
        runTest {
            every { locationProvider.lastKnownLocation } returns null
            every { searchApi.evSearch(any()) } returns Result.success(EvSearchResponse(results = searchResults))

            setAllActiveFilters(viewModel)

            Assert.assertEquals(4, viewModel.activeFilters.value.size)

            viewModel.performEvSearch()

            Assert.assertEquals(2, viewModel.searchResults.value.size)
            Assert.assertEquals(SearchPanelState.ResultsFetched, viewModel.searchPanelState.value)
        }

    private fun setAllActiveFilters(viewModel: EvSearchViewModel) {
        evFilterCategories[EvFilter.CHARGING_SPEED]?.filterOptions?.get(0)?.let {
            viewModel.addActiveFilter(evFilterCategories[EvFilter.CHARGING_SPEED]!!, it)
        }

        evFilterCategories[EvFilter.CONNECTOR_TYPE]?.filterOptions?.get(0)?.let {
            viewModel.addActiveFilter(evFilterCategories[EvFilter.CONNECTOR_TYPE]!!, it)
        }

        evFilterCategories[EvFilter.ACCESS_TYPE]?.filterOptions?.get(0)?.let {
            viewModel.addActiveFilter(evFilterCategories[EvFilter.ACCESS_TYPE]!!, it)
        }

        evFilterCategories[EvFilter.STATE]?.filterOptions?.get(0)?.let {
            viewModel.addActiveFilter(evFilterCategories[EvFilter.STATE]!!, it)
        }
    }

    @OptIn(BetaLocationApi::class, BetaNearbyEvSearchApi::class, BetaEvSearchApi::class)
    private val searchResults = listOf(
        mockk<EvSearchResult>().apply {
            every { accessType } returns null
            every { detour } returns null
            every { nearbyPoiCategories } returns emptySet()
            every { place } returns mockk<Place>().apply {
                every { coordinate } returns mockk<GeoPoint>().apply {
                    every { distanceTo(any()) } returns Distance.meters(0)
                }
            }
            every { poi } returns mockk<Poi>().apply {
                every { categoryIds } returns setOf(CategoryId(StandardCategoryId.GasStation))
            }
        },
        mockk<EvSearchResult>().apply {
            every { accessType } returns null
            every { detour } returns null
            every { nearbyPoiCategories } returns emptySet()
            every { place } returns mockk<Place>().apply {
                every { coordinate } returns mockk<GeoPoint>().apply {
                    every { distanceTo(any()) } returns Distance.meters(0)
                }
            }
            every { poi } returns mockk<Poi>().apply {
                every { categoryIds } returns setOf(CategoryId(StandardCategoryId.GasStation))
            }
        },
    )
}
