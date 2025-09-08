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

package com.example.application.extension

import com.tomtom.quantity.Speed
import com.tomtom.sdk.location.Address
import com.tomtom.sdk.location.road.SpeedLimit
import com.tomtom.sdk.navigation.locationcontext.LocationContext
import io.mockk.junit4.MockKRule
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LocationContextExtTest {
    @get:Rule
    val mockkRule = MockKRule(this)

    private lateinit var locationContext: LocationContext

    @Before
    fun setup() {
        locationContext = LocationContext(Speed.ZERO)
    }

    @Test
    fun `when locationContext or address is null, isMph is null`() {
        assertNull(locationContext.getSpeedData().isMph)
    }

    @Test
    fun `when address is in USA, isMph is true`() {
        locationContext = LocationContext(Speed.ZERO, address = Address(countryCodeIso3 = "USA"))

        assertTrue(locationContext.getSpeedData().isMph == true)
    }

    @Test
    fun `when address is in GBR, isMph is true`() {
        locationContext = LocationContext(Speed.ZERO, address = Address(countryCodeIso3 = "GBR"))

        assertTrue(locationContext.getSpeedData().isMph == true)
    }

    @Test
    fun `when address is not in USA or GBR, isMph is false`() {
        locationContext = LocationContext(Speed.ZERO, address = Address(countryCodeIso3 = "ESP"))

        assertTrue(locationContext.getSpeedData().isMph == false)
    }

    @Test
    fun `when speed limit is 0, the speed limit is set to --`() {
        locationContext =
            LocationContext(
                speed = Speed.ZERO,
                speedLimit = SpeedLimit(Speed.ZERO),
                address = Address(countryCodeIso3 = "ESP"),
            )

        assertTrue(locationContext.getSpeedData().speedLimit == SPEED_LIMIT_VOID)
    }

    @Test
    fun `when speed limit is greater than 0, the speed limit is not set to --`() {
        locationContext =
            LocationContext(
                speed = Speed.ZERO,
                speedLimit = SpeedLimit(Speed.kilometersPerHour(100)),
                address = Address(countryCodeIso3 = "ESP"),
            )

        assertTrue(locationContext.getSpeedData().speedLimit == "100")
    }
}
