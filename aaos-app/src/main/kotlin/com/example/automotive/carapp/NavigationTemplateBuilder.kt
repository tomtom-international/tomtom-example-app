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

package com.example.automotive.carapp

import android.util.Log
import androidx.car.app.CarContext
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarIcon
import androidx.car.app.model.Template
import androidx.car.app.navigation.model.NavigationTemplate
import androidx.core.graphics.drawable.IconCompat
import com.example.automotive.R

class NavigationTemplateBuilder(
    private val carContext: CarContext,
    private val mainViewModel: MainViewModel,
) {
    private var showRecenterMapAction = true

    fun buildNavigationTemplate(state: MainScreenUIState): Template = NavigationTemplate.Builder()
        .setActionStrip(buildActionStrip(state))
        .setMapActionStrip(buildMapActionStrip())
        .build()

    private fun buildActionStrip(state: MainScreenUIState): ActionStrip {
        Log.d(TAG, "Building action strip")
        val builder = ActionStrip.Builder()

        showRecenterMapAction = true
        when (state.scenario) {
            Scenario.HOME -> buildActionHome(builder)
            Scenario.POI_FOCUS -> buildActionPoiFocus(builder, state)
            Scenario.ROUTE_PREVIEW -> buildActionRoutePreview(builder)
            Scenario.GUIDANCE -> buildActionGuidance(builder)
            Scenario.DESTINATION_ARRIVAL -> buildActionDestinationArrival(builder)
        }

        return builder.build()
    }

    private fun buildActionHome(builder: ActionStrip.Builder) {
        builder.addAction(buildRecenterButton())
        showRecenterMapAction = false
    }

    private fun buildActionPoiFocus(
        builder: ActionStrip.Builder,
        state: MainScreenUIState,
    ) {
        state.placeDetails?.let { placeDetails ->
            builder.addAction(
                Action.Builder()
                    .setTitle(
                        if (state.isLoading) {
                            carContext.getString(R.string.template_action_title_loading)
                        } else {
                            carContext.getString(R.string.template_action_title_plan_route)
                        },
                    )
                    .setOnClickListener {
                        Log.d(TAG, "User clicked: Plan route to place")
                        mainViewModel.planRoute(placeDetails)
                    }
                    .setEnabled(!state.isLoading)
                    .build(),
            ).addAction(buildClearAction())
        }
    }

    private fun buildActionRoutePreview(builder: ActionStrip.Builder) {
        builder.addAction(
            Action.Builder()
                .setTitle(carContext.getString(R.string.template_action_title_drive))
                .setOnClickListener {
                    Log.d(TAG, "User clicked: Drive")
                    mainViewModel.startGuidance()
                }
                .build(),
        ).addAction(buildClearAction())
    }

    private fun buildActionDestinationArrival(builder: ActionStrip.Builder) {
        builder.addAction(buildClearAction())
    }

    private fun buildActionGuidance(builder: ActionStrip.Builder) {
        builder.addAction(buildClearAction())
    }

    private fun buildMapActionStrip(): ActionStrip {
        val builder = ActionStrip.Builder().addAction(Action.Builder(Action.PAN).build())

        if (showRecenterMapAction) {
            builder.addAction(buildRecenterButton())
        }

        return builder.build()
    }

    private fun buildRecenterButton(): Action = Action.Builder()
        .setIcon(
            CarIcon.Builder(
                IconCompat.createWithResource(
                    carContext,
                    R.drawable.tt_asset_icon_recenter_line_32,
                ),
            ).build(),
        )
        .setOnClickListener {
            Log.d(TAG, "User clicked: Recenter")
            mainViewModel.recenterCamera()
        }
        .build()

    private fun buildClearAction(): Action {
        return Action.Builder()
            .setTitle(carContext.getString(R.string.template_action_title_clear))
            .setOnClickListener {
                Log.d(TAG, "User clicked: Clear")
                mainViewModel.clearClicked()
            }
            .build()
    }

    companion object {
        private const val TAG = "NavigationTemplate"
    }
}
