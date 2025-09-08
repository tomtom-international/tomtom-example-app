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
import com.example.application.common.ROTTERDAM
import com.example.application.common.TOMTOM_AMSTERDAM_OFFICE
import com.tomtom.quantity.Distance
import com.tomtom.sdk.common.Cancellable
import com.tomtom.sdk.common.Result
import com.tomtom.sdk.location.Address
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.Place
import com.tomtom.sdk.location.poi.Source
import com.tomtom.sdk.location.poi.StandardCategoryId
import com.tomtom.sdk.routing.RoutePlanner
import com.tomtom.sdk.routing.RoutePlanningCallback
import com.tomtom.sdk.routing.RoutePlanningResponse
import com.tomtom.sdk.routing.RoutingFailure
import com.tomtom.sdk.routing.route.Route
import com.tomtom.sdk.routing.route.RoutePoint
import com.tomtom.sdk.search.Search
import com.tomtom.sdk.search.SearchResponse
import com.tomtom.sdk.search.common.error.SearchFailure
import com.tomtom.sdk.search.model.SearchResultType
import com.tomtom.sdk.search.model.result.SearchResult
import com.tomtom.sdk.search.model.result.SearchResultId
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PoiAlongRouteViewModelTest {
    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    private lateinit var searchApi: Search

    @MockK
    private lateinit var routePlanner: RoutePlanner

    @MockK
    private lateinit var mockRoute: Route

    private lateinit var viewModel: PoiAlongRouteViewModel

    private val onRoutePlanningSuccess: (RoutePlanningResponse) -> Unit = mockk(relaxed = true)
    private val onRoutePlanningFailure: (RoutingFailure) -> Unit = mockk(relaxed = true)
    private val onSearchFailure: (SearchFailure) -> Unit = mockk(relaxed = true)
    private val onSetIsLoading: (Boolean) -> Unit = mockk(relaxed = true)

    @Before
    fun setup() {
        val dispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(dispatcher)

        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0

        val callbackSlot = slot<RoutePlanningCallback>()
        every { routePlanner.planRoute(any(), capture(callbackSlot)) } answers {
            callbackSlot.captured.onSuccess(mockk())
            Cancellable {}
        }

        every { mockRoute.routePoints } returns listOf(
            mockk<RoutePoint>().apply {
                every { coordinate } returns TOMTOM_AMSTERDAM_OFFICE
            },
            mockk<RoutePoint>().apply {
                every { coordinate } returns ROTTERDAM
            },
        )

        viewModel = PoiAlongRouteViewModel(
            routePlanner = routePlanner,
            onRoutePlanningSuccess = onRoutePlanningSuccess,
            onRoutePlanningFailure = onRoutePlanningFailure,
            onSearchFailure = onSearchFailure,
            onSetIsLoading = onSetIsLoading,
            search = searchApi,
            ioDispatcher = UnconfinedTestDispatcher(),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Log::class)
    }

    @Test
    fun `when poi search is performed and results are empty, the results list is updated`() = runTest {
        every { searchApi.search(any()) } returns Result.success(
            mockk<SearchResponse>().apply {
                every { results } returns emptyList()
            },
        )

        viewModel.onPoiCategoryClick(mockRoute, StandardCategoryId.GasStation)

        assertEquals(0, viewModel.searchResults.value.size)
    }

    @Test
    fun `when poi search is performed and there is an error, the results list is empty + onSearchFailure is called`() =
        runTest {
            val failure = SearchFailure.NetworkFailure("test error")
            every { searchApi.search(any()) } returns Result.failure(failure)
            every { onSearchFailure(any()) } just Runs

            viewModel.onPoiCategoryClick(mockRoute, StandardCategoryId.GasStation)

            assertEquals(0, viewModel.searchResults.value.size)
            verify { onSearchFailure(failure) }
        }

    @Test
    fun `when poi search is performed and results are received, the results list contains all the results returned`() =
        runTest {
            val geoPoint1 = mockk<GeoPoint>()
            val geoPoint2 = mockk<GeoPoint>()

            every { geoPoint1.distanceTo(any()) } returns Distance.meters(0)
            every { geoPoint2.distanceTo(any()) } returns Distance.meters(0)
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

            viewModel.onPoiCategoryClick(mockRoute, StandardCategoryId.GasStation)

            assertEquals(2, viewModel.searchResults.value.size)
        }

    @Test
    fun `when route planning is successful, onRoutePlanningSuccess is called`() = runTest {
        val callbackSlot = slot<RoutePlanningCallback>()
        val routePlanningResponse = mockk<RoutePlanningResponse>()

        every { routePlanner.planRoute(any(), capture(callbackSlot)) } answers {
            callbackSlot.captured.onSuccess(routePlanningResponse)
            Cancellable {}
        }

        PoiAlongRouteViewModel(
            routePlanner = routePlanner,
            onRoutePlanningSuccess = onRoutePlanningSuccess,
            onRoutePlanningFailure = onRoutePlanningFailure,
            onSearchFailure = onSearchFailure,
            onSetIsLoading = onSetIsLoading,
            search = searchApi,
            ioDispatcher = UnconfinedTestDispatcher(),
        )

        verify { onRoutePlanningSuccess(routePlanningResponse) }
    }

    @Test
    fun `when route planning fails, onRoutePlanningFailure is called`() = runTest {
        val callbackSlot = slot<RoutePlanningCallback>()
        val routingFailure = mockk<RoutingFailure>()

        every { routePlanner.planRoute(any(), capture(callbackSlot)) } answers {
            callbackSlot.captured.onFailure(routingFailure)
            Cancellable {}
        }

        PoiAlongRouteViewModel(
            routePlanner = routePlanner,
            onRoutePlanningSuccess = onRoutePlanningSuccess,
            onRoutePlanningFailure = onRoutePlanningFailure,
            onSearchFailure = onSearchFailure,
            onSetIsLoading = onSetIsLoading,
            search = searchApi,
            ioDispatcher = UnconfinedTestDispatcher(),
        )

        verify { onRoutePlanningFailure(routingFailure) }
    }
}
