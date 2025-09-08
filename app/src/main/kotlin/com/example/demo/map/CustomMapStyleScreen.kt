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

package com.example.demo.map

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.application.common.FixedHeightBottomSheet
import com.example.application.common.TOMTOM_AMSTERDAM_OFFICE
import com.example.application.common.isDeviceInLandscape
import com.example.demo.DemoMap
import com.example.demo.DemoViewModel
import com.example.demo.ui.LoadingOverlay
import com.tomtom.sdk.map.display.annotation.BetaInitialCameraOptionsApi
import com.tomtom.sdk.map.display.camera.InitialCameraOptions
import com.tomtom.sdk.map.display.compose.BetaMapComposableApi
import com.tomtom.sdk.map.display.compose.state.rememberMapViewState
import com.tomtom.sdk.map.display.style.StyleMode

@OptIn(
    BetaMapComposableApi::class,
    BetaInitialCameraOptionsApi::class,
)
@Composable
fun CustomMapStyleScreen(
    demoViewModel: DemoViewModel,
    modifier: Modifier = Modifier,
    customStyleViewModel: CustomStyleViewModel = viewModel(),
) {
    val context = LocalContext.current

    val isDeviceInLandscape = isDeviceInLandscape()
    val mapUiState by demoViewModel.mapUiState.collectAsStateWithLifecycle()
    val mapInfrastructure by demoViewModel.mapInfrastructure.collectAsStateWithLifecycle()
    val initialCameraOptions = InitialCameraOptions.LocationBased(position = TOMTOM_AMSTERDAM_OFFICE)

    val mapViewState = rememberMapViewState(initialCameraOptions = initialCameraOptions) {
        this.styleState.styleMode = StyleMode.MAIN
    }

    LaunchedEffect(mapUiState.mapStyleUrl) {
        mapUiState.mapStyleUrl?.takeIf { it.isNotEmpty() }?.let {
            customStyleViewModel.loadStyleSafely(
                styleState = mapViewState.styleState,
                styleUrl = mapUiState.mapStyleUrl,
                onSuccess = { demoViewModel.setIsLoading(false) },
                onError = { errorMessageResId ->
                    Toast.makeText(context, errorMessageResId, Toast.LENGTH_LONG).show()
                    demoViewModel.setIsLoading(false)
                },
            )
        }
    }

    Box(modifier = modifier) {
        DemoMap(
            mapUiState = mapUiState,
            mapInfrastructure = mapInfrastructure,
            isDeviceInLandscape = isDeviceInLandscape,
            mapViewState = mapViewState,
            disableGestures = false,
        )

        LaunchedEffect(Unit) {
            demoViewModel.updateSafeAreaTopPadding(0)
        }

        LoadingOverlay(isLoading = mapUiState.isLoading)

        BottomPanel(
            isDeviceInLandscape = isDeviceInLandscape,
            onSubmitClick = { urlText ->
                demoViewModel.setIsLoading(true)
                demoViewModel.updateMapStyleUrl(urlText)
            },
        )
    }
}

@Composable
private fun BottomPanel(
    isDeviceInLandscape: Boolean,
    modifier: Modifier = Modifier,
    onSubmitClick: (submittedUrl: String) -> Unit,
) {
    var urlText by rememberSaveable { mutableStateOf("") }
    val localFocusManager = LocalFocusManager.current
    val sheetPeekHeight = remember { 192.dp }

    FixedHeightBottomSheet(
        sheetPeekHeight = sheetPeekHeight,
        modifier = modifier,
        isDeviceInLandscape = isDeviceInLandscape,
    ) {
        Column(
            modifier = Modifier.padding(top = 32.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
        ) {
            OutlinedTextField(
                value = urlText,
                onValueChange = { urlText = it },
                label = { Text(stringResource(R.string.demo_map_style_text_field_label)) },
                placeholder = { Text(stringResource(R.string.demo_map_style_text_field_placeholder)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        onSubmitClick(urlText)
                        localFocusManager.clearFocus()
                    },
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = {
                    onSubmitClick(urlText)
                    localFocusManager.clearFocus()
                },
                enabled = urlText.isNotBlank(),
                modifier = Modifier.padding(top = 16.dp),
            ) {
                Text(stringResource(R.string.demo_map_style_submit_button))
            }
        }
    }
}
