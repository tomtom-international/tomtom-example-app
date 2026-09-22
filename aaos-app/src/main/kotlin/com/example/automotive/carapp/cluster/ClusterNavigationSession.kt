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

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner

/**
 * AAOS Session managing the instrument cluster navigation screen.
 *
 * Provides the necessary lifecycle and state management infrastructure for the
 * cluster map screen and its Compose views.
 *
 * @param clusterData Container for cluster map state flows.
 */
class ClusterNavigationSession(
    private val clusterData: ClusterData,
) :
    Session(),
        SavedStateRegistryOwner,
        ViewModelStoreOwner,
        DefaultLifecycleObserver {
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val _viewModelStore = ViewModelStore()

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override val viewModelStore: ViewModelStore
        get() = _viewModelStore

    init {
        savedStateRegistryController.performRestore(null)
        lifecycle.addObserver(this)
    }

    override fun onCreateScreen(intent: Intent): Screen {
        return ClusterMapScreen(
            carContext = carContext,
            savedStateRegistryOwner = this,
            viewModelStoreOwner = this,
            clusterData = clusterData,
        )
    }
}
