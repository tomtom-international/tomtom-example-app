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

package com.example.automotive.common.extension

import androidx.car.app.navigation.model.Maneuver
import com.tomtom.sdk.location.DrivingSide
import com.tomtom.sdk.navigation.guidance.instruction.ArrivalGuidanceInstruction
import com.tomtom.sdk.navigation.guidance.instruction.DepartureGuidanceInstruction
import com.tomtom.sdk.navigation.guidance.instruction.ExitHighwayGuidanceInstruction
import com.tomtom.sdk.navigation.guidance.instruction.ForkGuidanceInstruction
import com.tomtom.sdk.navigation.guidance.instruction.GuidanceInstruction
import com.tomtom.sdk.navigation.guidance.instruction.MandatoryTurnGuidanceInstruction
import com.tomtom.sdk.navigation.guidance.instruction.MergeGuidanceInstruction
import com.tomtom.sdk.navigation.guidance.instruction.RoundaboutGuidanceInstruction
import com.tomtom.sdk.navigation.guidance.instruction.TollgateGuidanceInstruction
import com.tomtom.sdk.navigation.guidance.instruction.TurnGuidanceInstruction
import com.tomtom.sdk.routing.route.instruction.common.ExitDirection
import com.tomtom.sdk.routing.route.instruction.common.TurnDirection
import com.tomtom.sdk.routing.route.instruction.fork.ForkDirection
import com.tomtom.sdk.routing.route.instruction.merge.MergeSide

private const val HALF_CIRCLE_DEGREES = 180
private const val FULL_CIRCLE_DEGREES = 360

/**
 * [RoundaboutGuidanceInstruction.turnAngle] is the turn relative to the entry road, -180 to 180,
 * where -90 is a left turn. [Maneuver.Builder.setRoundaboutExitAngle] takes a different quantity:
 * the degrees traveled around the roundabout, 1 to 360.
 */
fun GuidanceInstruction.toCarManeuver(): Maneuver {
    val builder = Maneuver.Builder(toCarManeuverType())
    if (this is RoundaboutGuidanceInstruction) {
        val turnDegrees = turnAngle.inWholeDegrees().toInt()
        val circulationDegrees = when (drivingSide) {
            DrivingSide.LEFT -> HALF_CIRCLE_DEGREES + turnDegrees
            DrivingSide.RIGHT -> HALF_CIRCLE_DEGREES - turnDegrees
        }
        builder.setRoundaboutExitAngle(
            if (circulationDegrees == 0) FULL_CIRCLE_DEGREES else circulationDegrees,
        )
        exitNumber?.takeIf { it >= 1 }?.let { builder.setRoundaboutExitNumber(it) }
    }
    return builder.build()
}

private fun GuidanceInstruction.toCarManeuverType(): Int = when (this) {
    is DepartureGuidanceInstruction -> Maneuver.TYPE_DEPART
    is TurnGuidanceInstruction -> toTurnCarManeuverType()
    is MandatoryTurnGuidanceInstruction -> toMandatoryTurnCarManeuverType()
    is ForkGuidanceInstruction -> toForkCarManeuverType()
    is MergeGuidanceInstruction -> toMergeCarManeuverType()
    is RoundaboutGuidanceInstruction -> toRoundaboutCarManeuverType()
    is ArrivalGuidanceInstruction -> Maneuver.TYPE_DESTINATION
    is ExitHighwayGuidanceInstruction -> toExitHighwayCarManeuverType()
    is TollgateGuidanceInstruction -> Maneuver.TYPE_STRAIGHT
    else -> Maneuver.TYPE_UNKNOWN
}

private fun TurnGuidanceInstruction.toTurnCarManeuverType(): Int = when (turnDirection) {
    TurnDirection.TurnLeft -> Maneuver.TYPE_TURN_NORMAL_LEFT
    TurnDirection.TurnRight -> Maneuver.TYPE_TURN_NORMAL_RIGHT
    TurnDirection.BearLeft -> Maneuver.TYPE_TURN_SLIGHT_LEFT
    TurnDirection.BearRight -> Maneuver.TYPE_TURN_SLIGHT_RIGHT
    TurnDirection.SharpLeft -> Maneuver.TYPE_TURN_SHARP_LEFT
    TurnDirection.SharpRight -> Maneuver.TYPE_TURN_SHARP_RIGHT
    TurnDirection.GoStraight -> Maneuver.TYPE_STRAIGHT
    TurnDirection.TurnAround -> Maneuver.TYPE_U_TURN_LEFT
    else -> Maneuver.TYPE_UNKNOWN
}

private fun MandatoryTurnGuidanceInstruction.toMandatoryTurnCarManeuverType(): Int = when (turnDirection) {
    TurnDirection.TurnLeft -> Maneuver.TYPE_TURN_NORMAL_LEFT
    else -> Maneuver.TYPE_TURN_NORMAL_RIGHT
}

private fun ForkGuidanceInstruction.toForkCarManeuverType(): Int = when (forkDirection) {
    ForkDirection.Left -> Maneuver.TYPE_FORK_LEFT
    else -> Maneuver.TYPE_FORK_RIGHT
}

private fun MergeGuidanceInstruction.toMergeCarManeuverType(): Int = when (mergeSide) {
    MergeSide.TO_LEFT_LANE -> Maneuver.TYPE_MERGE_LEFT
    else -> Maneuver.TYPE_MERGE_RIGHT
}

private fun RoundaboutGuidanceInstruction.toRoundaboutCarManeuverType(): Int = when (drivingSide) {
    DrivingSide.LEFT -> Maneuver.TYPE_ROUNDABOUT_ENTER_AND_EXIT_CW_WITH_ANGLE
    DrivingSide.RIGHT -> Maneuver.TYPE_ROUNDABOUT_ENTER_AND_EXIT_CCW_WITH_ANGLE
}

private fun ExitHighwayGuidanceInstruction.toExitHighwayCarManeuverType(): Int = when (exitDirection) {
    ExitDirection.LEFT -> Maneuver.TYPE_OFF_RAMP_NORMAL_LEFT
    ExitDirection.RIGHT -> Maneuver.TYPE_OFF_RAMP_NORMAL_RIGHT
}
