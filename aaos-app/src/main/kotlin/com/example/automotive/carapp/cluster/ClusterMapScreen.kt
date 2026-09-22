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

package com.example.automotive.carapp.cluster

import androidx.car.app.AppManager
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.Template
import androidx.car.app.navigation.model.NavigationTemplate
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner

/**
 * Screen for rendering the TomTom map on the AAOS instrument cluster display.
 *
 * Manages the lifecycle of the cluster map surface callback and returns a minimal
 * navigation template with a dummy action strip (required by the API).
 *
 * @param carContext CarContext from Android Automotive framework.
 * @param savedStateRegistryOwner Saved state registry owner for Compose.
 * @param viewModelStoreOwner ViewModel store owner for Compose.
 * @param clusterData Container for cluster map state flows.
 */
class ClusterMapScreen(
    carContext: CarContext,
    private val savedStateRegistryOwner: SavedStateRegistryOwner,
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val clusterData: ClusterData,
) : Screen(carContext) {
    private val appManager: AppManager by lazy { carContext.getCarService(AppManager::class.java) }
    private var surfaceCallbackRegistered = false

    override fun onGetTemplate(): Template {
        if (!surfaceCallbackRegistered) {
            val clusterMapSurfaceCallback = ClusterMapSurfaceCallback(
                carContext = carContext,
                lifecycleOwner = this,
                savedStateRegistryOwner = savedStateRegistryOwner,
                viewModelStoreOwner = viewModelStoreOwner,
                clusterData = clusterData,
            )
            appManager.setSurfaceCallback(clusterMapSurfaceCallback)
            surfaceCallbackRegistered = true
        }

        return NavigationTemplate.Builder()
            .setActionStrip(getActionStrip())
            .build()
    }

    /**
     * The Cluster display can't show any button, and this action strip won't be shown in the display.
     * But we still have to add a dummy action because it's required by the NavigationTemplate API.
     */
    private fun getActionStrip(): ActionStrip = ActionStrip.Builder()
        .addAction(
            Action.Builder()
                .setTitle("Done")
                .setOnClickListener { }
                .build(),
        )
        .build()
}
