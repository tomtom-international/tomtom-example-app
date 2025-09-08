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

package com.example.application.map.ui

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import com.example.application.map.model.Scenario
import com.example.application.map.model.ScenarioHolders
import com.example.application.map.scenarios.arrival.ArrivalUiComponents
import com.example.application.map.scenarios.freedriving.FreeDrivingUiComponents
import com.example.application.map.scenarios.guidance.GuidanceUiComponents
import com.example.application.map.scenarios.home.HomeUiComponents
import com.example.application.map.scenarios.poifocus.PoiFocusUiComponents
import com.example.application.map.scenarios.routepreview.RoutePreviewUiComponents

@Composable
fun BoxScope.ScenarioHost(
    scenario: Scenario,
    holders: ScenarioHolders,
) {
    when (scenario) {
        Scenario.HOME -> HomeUiComponents(holders.homeStateHolder)
        Scenario.POI_FOCUS -> PoiFocusUiComponents(holders.poiFocusStateHolder)
        Scenario.ROUTE_PREVIEW -> RoutePreviewUiComponents(holders.routePreviewStateHolder)
        Scenario.GUIDANCE -> GuidanceUiComponents(holders.guidanceStateHolder)
        Scenario.DESTINATION_ARRIVAL -> ArrivalUiComponents(holders.arrivalStateHolder)
        Scenario.FREE_DRIVING -> FreeDrivingUiComponents(holders.freeDrivingStateHolder)
    }
}
