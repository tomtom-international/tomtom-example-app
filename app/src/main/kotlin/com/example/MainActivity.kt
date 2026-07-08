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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.DeploymentMode.ONLINE_FIRST
import com.example.DeploymentMode.ONLINE_ONLY
import com.example.Destination.ChildActivityDestination
import com.example.Destination.DeploymentModeDestination
import com.example.Destination.HomeScreenDestination
import com.example.Destination.PrivacyDestination
import com.example.MainViewModel.Companion.KEYSTORE_ASSET_PATH
import com.example.MainViewModel.Companion.MAP_ASSET_PATH
import com.example.application.common.ISO3_GBR
import com.example.application.common.ISO3_USA
import com.example.application.map.OnboardMapAssetsExtractor
import com.example.application.settings.data.LocalSettingsRepository
import com.example.application.ui.theme.NavSdkExampleTheme
import com.example.demo.getDemoDestinations
import com.example.onboarding.DeploymentModeScreen
import com.example.onboarding.MapExtractionFailedDialog
import com.example.onboarding.PrivacyScreen
import com.example.onboarding.SplashContent
import com.tomtom.sdk.annotations.BetaSdkInitializationApi
import com.tomtom.sdk.common.configuration.buildSdkConfiguration
import com.tomtom.sdk.common.measures.UnitSystem
import com.tomtom.sdk.init.TomTomSdk
import com.tomtom.sdk.navigation.UnitSystemType
import com.tomtom.sdk.telemetry.UserConsent
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
                set(MainViewModel.APP_FILES_DIR_KEY, application.filesDir)
                set(MainViewModel.ON_TOMTOM_SDK_INITIALIZE_KEY, ::initializeTomTomSdk)
                set(MainViewModel.ON_EXTRACT_MAP_ASSETS_KEY, ::extractMapAssets)
            },
        )[MainViewModel::class]
    }

    @Suppress("detekt:LongMethod")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            NavSdkExampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainActivityContent(
                        uiState = uiState,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }

    @Composable
    private fun MainActivityContent(
        uiState: MainUiState,
        modifier: Modifier = Modifier,
    ) {
        when (uiState.mapExtractionState) {
            MapExtractionState.InProgress -> {
                SplashContent(modifier = modifier)
            }

            MapExtractionState.Failed -> {
                SplashContent(modifier = modifier)
                MapExtractionFailedDialog(onMapExtractionFailed = ::finish)
            }

            MapExtractionState.Completed -> {
                MainNavigationHost(
                    persistedConsentLevel = uiState.persistedConsentLevel,
                    modifier = modifier,
                )
            }
        }
    }

    @Composable
    private fun MainNavigationHost(
        persistedConsentLevel: Int?,
        modifier: Modifier = Modifier,
    ) {
        val navController = rememberNavController()
        val startDestination = remember { startDestinationFor(persistedConsentLevel) }
        val navigateToDestination = remember(navController) {
            { destination: Destination ->
                when (destination) {
                    is ChildActivityDestination -> openChildActivity(destination)
                    else -> navController.navigate(route = destination)
                }
            }
        }

        NavHost(
            navController = navController,
            startDestination = startDestination,
        ) {
            addMainDestinations(
                navController = navController,
                navigateToDestination = navigateToDestination,
                modifier = modifier,
            )
        }
    }

    private fun NavGraphBuilder.addMainDestinations(
        navController: NavController,
        navigateToDestination: (Destination) -> Unit,
        modifier: Modifier,
    ) {
        composable<PrivacyDestination> {
            PrivacyScreen(
                viewModel = viewModel,
                onConsentPersisted = {
                    navController.navigate(DeploymentModeDestination) {
                        popUpTo(PrivacyDestination) { inclusive = true }
                    }
                },
                modifier = modifier,
            )
        }

        composable<DeploymentModeDestination> {
            DeploymentModeScreen(
                viewModel = viewModel,
                onSdkInitializationComplete = {
                    navController.navigate(HomeScreenDestination) {
                        popUpTo(DeploymentModeDestination) { inclusive = true }
                    }
                },
                onSdkInitializationFailed = ::finish,
                modifier = modifier,
            )
        }

        composable<HomeScreenDestination> {
            MainScreen(
                onNavigateToDestination = navigateToDestination,
                modifier = modifier,
            )
        }

        getDemoDestinations(navigateToDestination, modifier)
    }

    private fun startDestinationFor(persistedConsentLevel: Int?): Destination = if (persistedConsentLevel == null) {
        PrivacyDestination
    } else {
        DeploymentModeDestination
    }

    private fun openChildActivity(destination: ChildActivityDestination) {
        val intent = Intent(
            this,
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

    @OptIn(BetaSdkInitializationApi::class)
    private suspend fun initializeTomTomSdk(
        deploymentMode: DeploymentMode,
        telemetryConsent: suspend () -> UserConsent,
    ) {
        val sdkConfiguration = when (deploymentMode) {
            ONLINE_ONLY -> buildSdkConfiguration(
                context = application,
                apiKey = BuildConfig.TOMTOM_API_KEY,
                telemetryUserConsent = telemetryConsent,
            )

            ONLINE_FIRST -> buildSdkConfiguration(
                context = application,
                apiKey = BuildConfig.TOMTOM_API_KEY,
                regionStorePath = viewModel.mapDir,
                telemetryUserConsent = telemetryConsent,
                regionStoreConfiguration = {
                    keyStorePath = viewModel.ndsKeyStorePath
                },
            )
        }

        TomTomSdk.initialize(context = application, sdkConfiguration = sdkConfiguration)

        TomTomSdk.navigation.configuration.update {
            unitSystem = when (Locale.getDefault().isO3Country) {
                ISO3_USA -> UnitSystemType.Fixed(UnitSystem.US)
                ISO3_GBR -> UnitSystemType.Fixed(UnitSystem.UK)
                else -> UnitSystemType.Fixed(UnitSystem.Metric)
            }
        }
    }

    private fun extractMapAssets() = OnboardMapAssetsExtractor.extractMapAssets(
        context = application,
        config = OnboardMapAssetsExtractor.MapAssetsConfig(
            targetMapDir = viewModel.mapDir,
            targetKeystorePath = viewModel.ndsKeyStorePath,
            mapAsset = MAP_ASSET_PATH,
            keyStoreAsset = KEYSTORE_ASSET_PATH,
        ),
        forceExtraction = false,
    )

    companion object {
        const val DESTINATION_KEY = "destination_key"
    }
}
