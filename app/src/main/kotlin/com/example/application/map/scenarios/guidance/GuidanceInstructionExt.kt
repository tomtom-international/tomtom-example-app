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

package com.example.application.map.scenarios.guidance

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
