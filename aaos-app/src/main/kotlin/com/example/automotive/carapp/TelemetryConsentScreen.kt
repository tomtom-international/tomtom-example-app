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

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Header
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import com.example.automotive.R
import com.example.automotive.settings.data.model.ConsentLevel

/**
 * Screen that presents the telemetry consent options to the user on first launch.
 * Uses a [ListTemplate] so there is no restriction on the number of selectable items.
 *
 * @param carContext CarContext from the Car App Library
 * @param onConsentSelected Callback invoked with the chosen [ConsentLevel]
 */
class TelemetryConsentScreen(
    carContext: CarContext,
    private val onConsentSelected: (ConsentLevel) -> Unit,
) : Screen(carContext) {
    override fun onGetTemplate(): Template {
        val itemList = ItemList.Builder()
            .addItem(
                Row.Builder()
                    .setTitle(carContext.getString(R.string.template_label_telemetry_consent_off))
                    .setOnClickListener { onConsentSelected(ConsentLevel.OFF) }
                    .build(),
            )
            .addItem(
                Row.Builder()
                    .setTitle(carContext.getString(R.string.template_label_telemetry_consent_location_only))
                    .setOnClickListener { onConsentSelected(ConsentLevel.LOCATION_ONLY) }
                    .build(),
            )
            .addItem(
                Row.Builder()
                    .setTitle(carContext.getString(R.string.template_label_telemetry_consent_on))
                    .setOnClickListener { onConsentSelected(ConsentLevel.ON) }
                    .build(),
            )
            .build()

        return ListTemplate.Builder()
            .setHeader(
                Header.Builder()
                    .setTitle(carContext.getString(R.string.template_title_send_telemetry))
                    .build(),
            )
            .setSingleList(itemList)
            .build()
    }
}
