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

package com.example

import com.example.application.NavigationActivity
import com.example.demo.DemoActivity
import kotlinx.serialization.Serializable

@Serializable
sealed interface Destination {
    @Serializable
    sealed class ChildActivityDestination(val activityClassName: String) : Destination {
        @Serializable
        object NavigationActivityDestination : ChildActivityDestination(NavigationActivity::class.java.name)

        @Serializable
        object RoutePlanningDestination : ChildActivityDestination(DemoActivity::class.java.name)

        @Serializable
        object RoutingWithWaypointsDestination : ChildActivityDestination(DemoActivity::class.java.name)

        @Serializable
        object LdevrDestination : ChildActivityDestination(DemoActivity::class.java.name)

        @Serializable
        object RouteWithTimingDestination : ChildActivityDestination(DemoActivity::class.java.name)

        @Serializable
        object EvSearchDestination : ChildActivityDestination(DemoActivity::class.java.name)

        @Serializable
        object PoiAlongRouteDestination : ChildActivityDestination(DemoActivity::class.java.name)

        @Serializable
        object AutocompleteDestination : ChildActivityDestination(DemoActivity::class.java.name)

        @Serializable
        object PoiSearchAreaDestination : ChildActivityDestination(DemoActivity::class.java.name)

        @Serializable
        object ManualMapManagementDestination : ChildActivityDestination(DemoActivity::class.java.name)
    }

    @Serializable
    object HomeScreenDestination : Destination

    @Serializable
    object DeploymentModeDestination : Destination

    @Serializable
    object PrivacyDestination : Destination

    @Serializable
    object DemoListScreenDestination : Destination

    @Serializable
    object RoutingListScreenDestination : Destination

    @Serializable
    object SearchListScreenDestination : Destination

    @Serializable
    object MapListScreenDestination : Destination
}
