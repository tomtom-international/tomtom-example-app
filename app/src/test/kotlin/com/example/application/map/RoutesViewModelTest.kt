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

package com.example.application.map

import com.tomtom.sdk.common.Cancellable
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.navigation.TomTomNavigation
import com.tomtom.sdk.routing.RoutePlanner
import com.tomtom.sdk.routing.RoutePlanningCallback
import com.tomtom.sdk.routing.RoutePlanningResponse
import com.tomtom.sdk.routing.RoutingFailure
import com.tomtom.sdk.routing.route.Route
import com.tomtom.sdk.routing.route.RouteStop
import com.tomtom.sdk.routing.route.RouteStopId
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RoutesViewModelTest {
    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    private lateinit var routePlanner: RoutePlanner

    @RelaxedMockK
    private lateinit var onRoutePlanningSuccess: () -> Unit

    @RelaxedMockK
    private lateinit var onRoutePlanningFailure: (RoutingFailure) -> Unit

    @RelaxedMockK
    private lateinit var navigation: TomTomNavigation

    private lateinit var viewModel: RoutesViewModel

    @Before
    fun setup() {
        viewModel = RoutesViewModel(routePlanner, navigation)
    }

    @Test
    fun `when planRoute success, onRoutePlanningSuccess is called`() {
        val routes = listOf<Route>()
        val response = mockk<RoutePlanningResponse> { every { this@mockk.routes } returns routes }

        val captureCallback = slot<RoutePlanningCallback>()
        every { routePlanner.planRoute(any(), capture(captureCallback)) } answers {
            captureCallback.captured.onSuccess(response)
            Cancellable {}
        }

        viewModel.planRoute(
            origin = GeoPoint(0.0, 0.0),
            destination = GeoPoint(1.0, 1.0),
            onRoutePlanningFailure = onRoutePlanningFailure,
            onRoutePlanningSuccess = onRoutePlanningSuccess,
        )

        verify(exactly = 1) { onRoutePlanningSuccess() }
        verify(exactly = 0) { onRoutePlanningFailure(any()) }
    }

    @Test
    fun `when planRoute failure, onRoutePlanningFailure is called`() {
        val failure = mockk<RoutingFailure>()

        val captureCallback = slot<RoutePlanningCallback>()
        every { routePlanner.planRoute(any(), capture(captureCallback)) } answers {
            captureCallback.captured.onFailure(failure)
            Cancellable {}
        }

        viewModel.planRoute(
            origin = GeoPoint(0.0, 0.0),
            destination = GeoPoint(1.0, 1.0),
            onRoutePlanningFailure = onRoutePlanningFailure,
            onRoutePlanningSuccess = onRoutePlanningSuccess,
        )

        verify(exactly = 0) { onRoutePlanningSuccess() }
        verify(exactly = 1) { onRoutePlanningFailure(failure) }
    }

    @Test
    fun `when getRouteStop, if the routeStopId exists, it returns the routeStop`() {
        val route = mockk<Route>()
        val routeStop = mockk<RouteStop>()
        val routeStopId = RouteStopId()
        every { routeStop.id } returns routeStopId
        every { route.routeStops } returns listOf(routeStop)
        val routes = listOf(route)
        val response = mockk<RoutePlanningResponse> { every { this@mockk.routes } returns routes }

        val captureCallback = slot<RoutePlanningCallback>()
        every { routePlanner.planRoute(any(), capture(captureCallback)) } answers {
            captureCallback.captured.onSuccess(response)
            Cancellable {}
        }

        viewModel.planRoute(
            origin = GeoPoint(0.0, 0.0),
            destination = GeoPoint(1.0, 1.0),
            onRoutePlanningFailure = onRoutePlanningFailure,
            onRoutePlanningSuccess = onRoutePlanningSuccess,
        )
        assertEquals(routeStop, viewModel.getRouteStop(routeStopId))
    }

    @Test
    fun `when getRouteStop, if the routeStopId does not exist, it returns null`() {
        val route = mockk<Route>()
        val routeStop = mockk<RouteStop>()
        val routeStopId = RouteStopId()
        every { routeStop.id } returns routeStopId
        every { route.routeStops } returns listOf(routeStop)
        val routes = listOf(route)
        val response = mockk<RoutePlanningResponse> { every { this@mockk.routes } returns routes }

        val captureCallback = slot<RoutePlanningCallback>()
        every { routePlanner.planRoute(any(), capture(captureCallback)) } answers {
            captureCallback.captured.onSuccess(response)
            Cancellable {}
        }

        viewModel.planRoute(
            origin = GeoPoint(0.0, 0.0),
            destination = GeoPoint(1.0, 1.0),
            onRoutePlanningFailure = onRoutePlanningFailure,
            onRoutePlanningSuccess = onRoutePlanningSuccess,
        )
        assertNull(viewModel.getRouteStop(RouteStopId()))
    }
}
