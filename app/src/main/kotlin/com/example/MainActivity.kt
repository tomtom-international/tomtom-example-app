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

package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.Destination.ChildActivityDestination
import com.example.Destination.DemoListScreenDestination
import com.example.Destination.HomeScreenDestination
import com.example.Destination.MapListScreenDestination
import com.example.Destination.RoutingListScreenDestination
import com.example.Destination.SearchListScreenDestination
import com.example.application.NavigationActivity
import com.example.application.ui.theme.NavSdkExampleTheme
import com.example.demo.DemoActivity
import com.example.demo.DemoListScreen
import com.example.demo.map.MapListScreen
import com.example.demo.routing.RoutingListScreen
import com.example.demo.search.SearchListScreen
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity() {
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
                                onNavigateToDestination = { navigateToDestination(it) },
                                modifier = innerPaddingModifier,
                            )
                        }

                        composable<DemoListScreenDestination> {
                            DemoListScreen(
                                onNavigateToDestination = { navigateToDestination(it) },
                                modifier = innerPaddingModifier,
                            )
                        }

                        composable<MapListScreenDestination> {
                            MapListScreen(
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
        object EvSearchDestination : ChildActivityDestination(DemoActivity::class.java.name)

        @Serializable
        object PoiAlongRouteDestination : ChildActivityDestination(DemoActivity::class.java.name)

        @Serializable
        object AutocompleteDestination : ChildActivityDestination(DemoActivity::class.java.name)

        @Serializable
        object PoiSearchAreaDestination : ChildActivityDestination(DemoActivity::class.java.name)

        @Serializable
        object CustomMapStyleDestination : ChildActivityDestination(DemoActivity::class.java.name)
    }

    @Serializable
    object HomeScreenDestination : Destination

    @Serializable
    object DemoListScreenDestination : Destination

    @Serializable
    object MapListScreenDestination : Destination

    @Serializable
    object RoutingListScreenDestination : Destination

    @Serializable
    object SearchListScreenDestination : Destination
}
