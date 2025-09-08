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

package com.example.application.freedriving

import com.tomtom.quantity.Distance
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.LocationProvider
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FreeDrivingManagerTest {
    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    private lateinit var locationProvider: LocationProvider

    private lateinit var freeDrivingManager: FreeDrivingManager

    @Before
    fun setup() {
        val dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)

        every { locationProvider.lastKnownLocation?.accuracy } returns Distance.meters(5)
        every { locationProvider.lastKnownLocation?.position } returns GeoPoint(0.0, 0.0)

        freeDrivingManager = FreeDrivingManager(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when there is a cached position farther than the distance threshold, isDriving is sent as true`() = runTest {
        val firstGeoPoint = GeoPoint(1.0, 1.0)
        every { locationProvider.lastKnownLocation?.position } returns firstGeoPoint andThen GeoPoint(0.0, 0.0)

        val isDrivingValues = mutableListOf<Boolean>()
        backgroundScope.launch {
            freeDrivingManager.getIsDrivingFlow { locationProvider }.toList(isDrivingValues)
        }

        advanceTimeBy(2000)

        assertTrue(isDrivingValues.size == 2)
        assertFalse(isDrivingValues[0])
        assertTrue(isDrivingValues[1])
    }

    @Test
    fun `when isDriving is true without a cached position farther than the  threshold, isDriving is sent as false`() =
        runTest {
            val firstGeoPoint = GeoPoint(1.0, 1.0)
            every { locationProvider.lastKnownLocation?.position } returns firstGeoPoint andThen GeoPoint(0.0, 0.0)

            val isDrivingValues = mutableListOf<Boolean>()
            backgroundScope.launch {
                freeDrivingManager.getIsDrivingFlow { locationProvider }.toList(isDrivingValues)
            }

            advanceTimeBy(2000)
            assertTrue(isDrivingValues[1])

            advanceTimeBy(99000)
            assertFalse(isDrivingValues[2])
        }

    @Test
    fun `when a location has an accuracy equal to the threshold, it is not cached and isDriving is not set to true`() =
        runTest {
            every { locationProvider.lastKnownLocation?.accuracy } returns Distance.meters(15) andThen
                Distance.meters(5)
            val firstGeoPoint = GeoPoint(1.0, 1.0)
            every { locationProvider.lastKnownLocation?.position } returns firstGeoPoint andThen GeoPoint(0.0, 0.0)

            val isDrivingValues = mutableListOf<Boolean>()
            backgroundScope.launch {
                freeDrivingManager.getIsDrivingFlow { locationProvider }.toList(isDrivingValues)
            }

            advanceTimeBy(2000)
            assertTrue(isDrivingValues.size == 1)
            assertFalse(isDrivingValues[0])
        }
}
