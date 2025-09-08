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

package com.example.application.map.model

import com.example.application.map.scenarios.arrival.ArrivalStateHolder
import com.example.application.map.scenarios.freedriving.FreeDrivingStateHolder
import com.example.application.map.scenarios.guidance.GuidanceStateHolder
import com.example.application.map.scenarios.home.HomeStateHolder
import com.example.application.map.scenarios.poifocus.PoiFocusStateHolder
import com.example.application.map.scenarios.routepreview.RoutePreviewStateHolder

/**
 * Each holder encapsulates state and actions for its scenario.
 */
data class ScenarioHolders(
    val homeStateHolder: HomeStateHolder,
    val poiFocusStateHolder: PoiFocusStateHolder,
    val routePreviewStateHolder: RoutePreviewStateHolder,
    val guidanceStateHolder: GuidanceStateHolder,
    val freeDrivingStateHolder: FreeDrivingStateHolder,
    val arrivalStateHolder: ArrivalStateHolder,
)
