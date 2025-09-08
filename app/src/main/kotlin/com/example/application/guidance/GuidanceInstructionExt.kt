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
import com.tomtom.sdk.routing.route.instruction.roundabout.RoundaboutDirection

fun GuidanceInstruction.toManeuverType(): ManeuverType? = when (this) {
    is DepartureGuidanceInstruction -> ManeuverType.STRAIGHT
    is TurnGuidanceInstruction -> toTurnManeuverType()
    is MandatoryTurnGuidanceInstruction -> toMandatoryTurnManeuverType()
    is ForkGuidanceInstruction -> toForkManeuverType()
    is MergeGuidanceInstruction -> toMergeManeuverType()
    is RoundaboutGuidanceInstruction -> toRoundaboutManeuverType()
    is ArrivalGuidanceInstruction -> ManeuverType.ARRIVAL
    is ExitHighwayGuidanceInstruction -> toExitHighwayManeuverType()
    is TollgateGuidanceInstruction -> ManeuverType.TOLLGATE
    else -> null
}

private fun TurnGuidanceInstruction.toTurnManeuverType(): ManeuverType? = when (this.turnDirection) {
    TurnDirection.TurnLeft -> ManeuverType.TURN_LEFT
    TurnDirection.TurnRight -> ManeuverType.TURN_RIGHT
    TurnDirection.BearRight -> ManeuverType.BEAR_RIGHT
    TurnDirection.BearLeft -> ManeuverType.BEAR_LEFT
    TurnDirection.GoStraight -> ManeuverType.STRAIGHT
    TurnDirection.TurnAround -> ManeuverType.UTURN
    TurnDirection.SharpLeft -> ManeuverType.SHARP_LEFT
    TurnDirection.SharpRight -> ManeuverType.SHARP_RIGHT
    else -> null
}

private fun MandatoryTurnGuidanceInstruction.toMandatoryTurnManeuverType(): ManeuverType = when (this.turnDirection) {
    TurnDirection.TurnLeft -> ManeuverType.TURN_LEFT
    else -> ManeuverType.TURN_RIGHT
}

private fun ForkGuidanceInstruction.toForkManeuverType(): ManeuverType = when (this.forkDirection) {
    ForkDirection.Left -> ManeuverType.BEAR_LEFT
    else -> ManeuverType.BEAR_RIGHT
}

private fun MergeGuidanceInstruction.toMergeManeuverType(): ManeuverType = when (this.mergeSide) {
    MergeSide.TO_LEFT_LANE -> ManeuverType.MERGE_TO_LEFT
    else -> ManeuverType.MERGE_TO_RIGHT
}

private fun RoundaboutGuidanceInstruction.toRoundaboutManeuverType(): ManeuverType = when (this.roundaboutDirection) {
    RoundaboutDirection.Right -> ManeuverType.ROUNDABOUT_RIGHT
    RoundaboutDirection.Left -> ManeuverType.ROUNDABOUT_LEFT
    RoundaboutDirection.Back -> ManeuverType.ROUNDABOUT_BACK
    RoundaboutDirection.Cross -> ManeuverType.ROUNDABOUT_CROSS
    else -> ManeuverType.ROUNDABOUT_CROSS
}

private fun ExitHighwayGuidanceInstruction.toExitHighwayManeuverType(): ManeuverType = when (this.exitDirection) {
    ExitDirection.LEFT -> ManeuverType.EXIT_HIGHWAY_LEFT
    ExitDirection.RIGHT -> ManeuverType.EXIT_HIGHWAY_RIGHT
}
