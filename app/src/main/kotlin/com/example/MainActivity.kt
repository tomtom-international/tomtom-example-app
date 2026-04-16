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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.Destination.ChildActivityDestination
import com.example.Destination.DemoListScreenDestination
import com.example.Destination.HomeScreenDestination
import com.example.Destination.RoutingListScreenDestination
import com.example.Destination.SearchListScreenDestination
import com.example.application.NavigationActivity
import com.example.application.common.ISO3_GBR
import com.example.application.common.ISO3_USA
import com.example.application.settings.data.LocalSettingsRepository
import com.example.application.ui.theme.NavSdkExampleTheme
import com.example.demo.DemoActivity
import com.example.demo.DemoListScreen
import com.example.demo.routing.RoutingListScreen
import com.example.demo.search.SearchListScreen
import com.tomtom.sdk.common.configuration.buildSdkConfiguration
import com.tomtom.sdk.common.measures.UnitSystem
import com.tomtom.sdk.init.TomTomSdk
import com.tomtom.sdk.navigation.UnitSystemType
import com.tomtom.sdk.telemetry.UserConsent
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.Locale

val Context.dataStore by preferencesDataStore(name = "user_preferences")

/**
 * Main activity for the sample app.
 * Wires the home screen and demo lists; launches the full navigation activity or demo activities.
 */
class MainActivity : ComponentActivity() {
    val settingsRepository by lazy { LocalSettingsRepository(dataStore) }

    val viewModel: MainViewModel by lazy {
        ViewModelProvider.create(
            this,
            factory = MainViewModel.Factory,
            extras = MutableCreationExtras().apply {
                set(MainViewModel.SETTINGS_REPOSITORY_KEY, settingsRepository)
                set(MainViewModel.ON_TOMTOM_SDK_INITIALIZE_KEY, ::initializeTomTomSdk)
            },
        )[MainViewModel::class]
    }

    @Suppress("detekt:LongMethod")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            val navigateToDestination = remember {
                { destination: Destination ->
                    when (destination) {
                        is ChildActivityDestination -> {
                            val intent = Intent(
                                this@MainActivity,
                                Class.forName(destination.activityClassName),
                            )
                            intent.putExtras(
                                Bundle().also {
                                    it.putString(
                                        DESTINATION_KEY,
                                        Json.encodeToString(
                                            ChildActivityDestination.serializer(),
                                            destination,
                                        ),
                                    )
                                },
                            )
                            startActivity(intent)
                        }

                        else -> {
                            navController.navigate(route = destination)
                        }
                    }
                }
            }

            NavSdkExampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val innerPaddingModifier = remember { Modifier.padding(innerPadding) }

                    NavHost(navController = navController, startDestination = HomeScreenDestination) {
                        composable<HomeScreenDestination> {
                            MainScreen(
                                viewModel = viewModel,
                                onNavigateToDestination = { navigateToDestination(it) },
                                onExitApp = { finish() },
                                modifier = innerPaddingModifier,
                            )
                        }

                        composable<DemoListScreenDestination> {
                            DemoListScreen(
                                onNavigateToDestination = { navigateToDestination(it) },
                                modifier = innerPaddingModifier,
                            )
                        }

                        composable<RoutingListScreenDestination> {
                            RoutingListScreen(
                                onNavigateToDestination = { navigateToDestination(it) },
                                modifier = innerPaddingModifier,
                            )
                        }

                        composable<SearchListScreenDestination> {
                            SearchListScreen(
                                onNavigateToDestination = { navigateToDestination(it) },
                                modifier = innerPaddingModifier,
                            )
                        }
                    }
                }
            }
        }
    }

    private suspend fun initializeTomTomSdk(telemetryConsent: suspend () -> UserConsent) {
        val sdkConfiguration =
            buildSdkConfiguration(context = application, apiKey = BuildConfig.TOMTOM_API_KEY, coreConfiguration = {
                telemetryUserConsent = telemetryConsent
            })

        TomTomSdk.initialize(context = application, sdkConfiguration = sdkConfiguration)

        TomTomSdk.navigation.configuration.update {
            unitSystem = when (Locale.getDefault().isO3Country) {
                ISO3_USA -> UnitSystemType.Fixed(UnitSystem.US)
                ISO3_GBR -> UnitSystemType.Fixed(UnitSystem.UK)
                else -> UnitSystemType.Fixed(UnitSystem.Metric)
            }
        }
    }

    companion object {
        const val DESTINATION_KEY = "destination_key"
    }
}

@Serializable
sealed interface Destination {
    @Serializable
    sealed class ChildActivityDestination(val activityClassName: String) : Destination {
        @Serializable
        object NavigationActivityDestination : ChildActivityDestination(NavigationActivity::class.java.name)

        @Serializable
        object RoutePlanningDestination : ChildActivityDestination(DemoActivity::class.java.name)

        @Serializable
        object LdevrDestination : ChildActivityDestination(DemoActivity::class.java.name)

        @Serializable
        object EvSearchDestination : ChildActivityDestination(DemoActivity::class.java.name)

        @Serializable
        object PoiAlongRouteDestination : ChildActivityDestination(DemoActivity::class.java.name)

        @Serializable
        object AutocompleteDestination : ChildActivityDestination(DemoActivity::class.java.name)

        @Serializable
        object PoiSearchAreaDestination : ChildActivityDestination(DemoActivity::class.java.name)
    }

    @Serializable
    object HomeScreenDestination : Destination

    @Serializable
    object DemoListScreenDestination : Destination

    @Serializable
    object RoutingListScreenDestination : Destination

    @Serializable
    object SearchListScreenDestination : Destination
}
