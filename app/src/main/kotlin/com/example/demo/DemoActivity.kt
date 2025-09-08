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

package com.example.demo

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.viewmodel.MutableCreationExtras
import com.example.Destination
import com.example.Destination.ChildActivityDestination
import com.example.Destination.ChildActivityDestination.AutocompleteDestination
import com.example.Destination.ChildActivityDestination.CustomMapStyleDestination
import com.example.Destination.ChildActivityDestination.EvSearchDestination
import com.example.Destination.ChildActivityDestination.PoiAlongRouteDestination
import com.example.Destination.ChildActivityDestination.PoiSearchAreaDestination
import com.example.Destination.ChildActivityDestination.RoutePlanningDestination
import com.example.MainActivity.Companion.DESTINATION_KEY
import com.example.application.common.MARKERS_ZOOM_PADDING_DP
import com.example.application.common.safeAreaStartPadding
import com.example.application.settings.data.LocalSettingsRepository
import com.example.application.ui.ErrorSnackbarHost
import com.example.application.ui.theme.NavSdkExampleTheme
import com.example.dataStore
import com.example.demo.map.CustomMapStyleScreen
import com.example.demo.routing.RoutePlanningScreen
import com.example.demo.search.area.PoiSearchAreaScreen
import com.example.demo.search.autocomplete.AutocompleteScreen
import com.example.demo.search.ev.EvSearchScreen
import com.example.demo.search.poialongroute.PoiAlongRouteScreen
import com.tomtom.sdk.annotations.AlphaSdkInitializationApi
import com.tomtom.sdk.entrypoint.TomTomSdk
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.camera.CameraTrackingMode
import com.tomtom.sdk.map.display.compose.BetaMapComposableApi
import com.tomtom.sdk.map.display.compose.TomTomMap
import com.tomtom.sdk.map.display.compose.TomTomMapComposable
import com.tomtom.sdk.map.display.compose.model.GesturesConfig
import com.tomtom.sdk.map.display.compose.model.MapInfrastructure
import com.tomtom.sdk.map.display.compose.state.MapViewState
import kotlinx.serialization.json.Json

@OptIn(AlphaSdkInitializationApi::class)
class DemoActivity : ComponentActivity() {
    private val viewModel: DemoViewModel by viewModels(extrasProducer = {
        MutableCreationExtras().apply {
            set(DemoViewModel.SETTINGS_REPOSITORY_KEY, LocalSettingsRepository(dataStore))
            set(DemoViewModel.NAVIGATION_KEY, TomTomSdk.navigation)
        }
    }) { DemoViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar?.hide()
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val destination = intent.extras?.getString(DESTINATION_KEY)?.let {
            Json.decodeFromString(ChildActivityDestination.serializer(), it)
        }

        setContent {
            NavSdkExampleTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.navigationBars,
                ) { innerPadding ->
                    DemoScreen(viewModel, destination, innerPadding)
                }
            }
        }
    }
}

@Composable
private fun DemoScreen(
    demoViewModel: DemoViewModel,
    destination: Destination?,
    innerPadding: PaddingValues,
) {
    Box(modifier = Modifier.padding(innerPadding)) {
        when (destination) {
            is RoutePlanningDestination -> {
                RoutePlanningScreen(demoViewModel = demoViewModel)
            }

            is EvSearchDestination -> {
                EvSearchScreen(demoViewModel = demoViewModel)
            }

            is PoiAlongRouteDestination -> {
                PoiAlongRouteScreen(demoViewModel = demoViewModel)
            }

            is AutocompleteDestination -> {
                AutocompleteScreen(demoViewModel = demoViewModel)
            }

            is PoiSearchAreaDestination -> {
                PoiSearchAreaScreen(demoViewModel = demoViewModel)
            }

            is CustomMapStyleDestination -> {
                CustomMapStyleScreen(demoViewModel = demoViewModel)
            }

            else -> {}
        }

        ErrorSnackbarHost(
            errorStateFlow = demoViewModel.errorState,
            modifier = Modifier.align(Alignment.BottomCenter),
            onErrorShown = { demoViewModel.clearErrorState() },
        )
    }
}

@OptIn(BetaMapComposableApi::class)
@Composable
fun DemoMap(
    mapUiState: DemoMapUiState,
    mapInfrastructure: MapInfrastructure,
    isDeviceInLandscape: Boolean,
    modifier: Modifier = Modifier,
    cameraOptions: CameraOptions? = null,
    zoomToAllMarkers: Boolean = false,
    disableGestures: Boolean = false,
    mapViewState: MapViewState,
    onMapReady: () -> Unit = { },
    content:
        @Composable
        @TomTomMapComposable () -> Unit = { },
) {
    val localDensity = LocalDensity.current
    mapViewState.safeArea = PaddingValues(
        start = safeAreaStartPadding(isDeviceInLandscape),
        bottom = localDensity.run { mapUiState.safeAreaBottomPadding.toDp() },
        top = localDensity.run { mapUiState.safeAreaTopPadding.toDp() },
    )

    if (disableGestures) {
        mapViewState.gestureState.config = GesturesConfig {
            isScrollEnabled = false
            isZoomEnabled = false
        }
    }

    LaunchedEffect(zoomToAllMarkers) {
        mapViewState.cameraState.zoomToAllMarkers(padding = MARKERS_ZOOM_PADDING_DP)
    }

    LaunchedEffect(cameraOptions) {
        cameraOptions?.let { mapViewState.cameraState.animateCamera(it) }
    }

    TomTomMap(
        modifier = modifier,
        infrastructure = mapInfrastructure,
        state = mapViewState,
        onMapReady = onMapReady,
        content = content,
    )
}

@Stable
data class DemoMapUiState(
    val isLoading: Boolean = false,
    val safeAreaTopPadding: Int = 0,
    val safeAreaBottomPadding: Int = 0,
    val mapStyleUrl: String? = null,
    val cameraTrackingMode: CameraTrackingMode = CameraTrackingMode.None,
)
