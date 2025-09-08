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

package com.example.application.guidance

import com.tomtom.quantity.Angle
import com.tomtom.quantity.Distance
import com.tomtom.sdk.common.UniqueId
import com.tomtom.sdk.location.DrivingSide
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.navigation.guidance.instruction.DepartureGuidanceInstruction
import com.tomtom.sdk.navigation.guidance.instruction.ForkGuidanceInstruction
import com.tomtom.sdk.navigation.guidance.instruction.MandatoryTurnGuidanceInstruction
import com.tomtom.sdk.navigation.guidance.instruction.MergeGuidanceInstruction
import com.tomtom.sdk.navigation.guidance.instruction.TurnGuidanceInstruction
import com.tomtom.sdk.routing.route.instruction.InstructionPoint
import com.tomtom.sdk.routing.route.instruction.common.TurnDirection
import com.tomtom.sdk.routing.route.instruction.fork.ForkDirection
import com.tomtom.sdk.routing.route.instruction.merge.MergeSide
import org.junit.Assert.assertEquals
import org.junit.Test

class GuidanceInstructionTest {
    private val defaultId = UniqueId()
    private val defaultRouteOffset = Distance.ZERO
    private val defaultManeuverPoint = GeoPoint(0.0, 0.0)
    private val defaultDrivingSide = DrivingSide.RIGHT
    private val defaultCombineWithNext = false
    private val defaultPreviousSignificantRoad = null
    private val defaultNextSignificantRoad = null
    private val defaultRoutePath = emptyList<InstructionPoint>()
    private val defaultIntersectionName = ""
    private val defaultSignpost = null

    @Test
    fun `test DepartureGuidanceInstruction to ManeuverType`() {
        val instruction = DepartureGuidanceInstruction(
            id = defaultId,
            routeOffset = defaultRouteOffset,
            maneuverPoint = defaultManeuverPoint,
            drivingSide = defaultDrivingSide,
            combineWithNext = defaultCombineWithNext,
            previousSignificantRoad = defaultPreviousSignificantRoad,
            nextSignificantRoad = defaultNextSignificantRoad,
            routePath = defaultRoutePath,
            intersectionName = defaultIntersectionName,
            signpost = defaultSignpost,
        )
        val result = instruction.toManeuverType()
        assertEquals(ManeuverType.STRAIGHT, result)
    }

    @Test
    fun `test TurnGuidanceInstruction to ManeuverType`() {
        val instruction = TurnGuidanceInstruction(
            turnAngle = Angle.ZERO,
            turnDirection = TurnDirection.TurnLeft,
            id = defaultId,
            routeOffset = defaultRouteOffset,
            maneuverPoint = defaultManeuverPoint,
            drivingSide = defaultDrivingSide,
            combineWithNext = defaultCombineWithNext,
            previousSignificantRoad = defaultPreviousSignificantRoad,
            nextSignificantRoad = defaultNextSignificantRoad,
            routePath = defaultRoutePath,
            intersectionName = defaultIntersectionName,
            signpost = defaultSignpost,
        )
        val result = instruction.toManeuverType()
        assertEquals(ManeuverType.TURN_LEFT, result)
    }

    @Test
    fun `test MandatoryTurnGuidanceInstruction to ManeuverType`() {
        val instruction = MandatoryTurnGuidanceInstruction(
            turnAngle = Angle.ZERO,
            turnDirection = TurnDirection.TurnRight,
            id = defaultId,
            routeOffset = defaultRouteOffset,
            maneuverPoint = defaultManeuverPoint,
            drivingSide = defaultDrivingSide,
            combineWithNext = defaultCombineWithNext,
            previousSignificantRoad = defaultPreviousSignificantRoad,
            nextSignificantRoad = defaultNextSignificantRoad,
            routePath = defaultRoutePath,
            intersectionName = defaultIntersectionName,
            signpost = defaultSignpost,
        )
        val result = instruction.toManeuverType()
        assertEquals(ManeuverType.TURN_RIGHT, result)
    }

    @Test
    fun `test ForkGuidanceInstruction to ManeuverType`() {
        val instruction = ForkGuidanceInstruction(
            forkDirection = ForkDirection.Left,
            id = defaultId,
            routeOffset = defaultRouteOffset,
            maneuverPoint = defaultManeuverPoint,
            drivingSide = defaultDrivingSide,
            combineWithNext = defaultCombineWithNext,
            previousSignificantRoad = defaultPreviousSignificantRoad,
            nextSignificantRoad = defaultNextSignificantRoad,
            routePath = defaultRoutePath,
            intersectionName = defaultIntersectionName,
            signpost = defaultSignpost,
        )
        val result = instruction.toManeuverType()
        assertEquals(ManeuverType.BEAR_LEFT, result)
    }

    @Test
    fun `test MergeGuidanceInstruction to ManeuverType`() {
        val instruction = MergeGuidanceInstruction(
            mergeSide = MergeSide.TO_LEFT_LANE,
            id = defaultId,
            routeOffset = defaultRouteOffset,
            maneuverPoint = defaultManeuverPoint,
            drivingSide = defaultDrivingSide,
            combineWithNext = defaultCombineWithNext,
            previousSignificantRoad = defaultPreviousSignificantRoad,
            nextSignificantRoad = defaultNextSignificantRoad,
            routePath = defaultRoutePath,
            intersectionName = defaultIntersectionName,
            signpost = defaultSignpost,
        )
        val result = instruction.toManeuverType()
        assertEquals(ManeuverType.MERGE_TO_LEFT, result)
    }
}
