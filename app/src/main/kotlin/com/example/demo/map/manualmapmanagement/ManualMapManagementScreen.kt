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

package com.example.demo.map.manualmapmanagement

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.application.common.TOMTOM_AMSTERDAM_OFFICE
import com.example.application.common.ui.FixedHeightBottomSheet
import com.example.application.common.ui.isDeviceInLandscape
import com.example.application.map.model.MapScreenUiState.ErrorState.MapManagementError
import com.example.demo.DemoMap
import com.example.demo.DemoViewModel
import com.example.demo.map.manualmapmanagement.ManualMapManagementViewModel.Companion.OFFLINE_MAP_KEY
import com.example.demo.map.manualmapmanagement.ManualMapManagementViewModel.Companion.ON_OPERATION_FAILED_KEY
import com.example.demo.map.manualmapmanagement.ManualMapManagementViewModel.Companion.ON_SET_IS_LOADING_KEY
import com.example.demo.ui.LoadingOverlay
import com.tomtom.quantity.Memory
import com.tomtom.sdk.datamanagement.nds.region.RegionInstallState
import com.tomtom.sdk.datamanagement.nds.region.RegionOperation
import com.tomtom.sdk.init.TomTomSdk
import com.tomtom.sdk.map.display.camera.InitialCameraOptions
import com.tomtom.sdk.map.display.compose.state.rememberMapViewState

private const val CONTACT_SALES_URL =
    "https://www.tomtom.com/contact-sales?source_app=developerportal&source_product=tomtom-sdk-for-android"

@Composable
fun ManualMapManagementScreen(
    demoViewModel: DemoViewModel,
    modifier: Modifier = Modifier,
    viewModel: ManualMapManagementViewModel = viewModel(
        factory = ManualMapManagementViewModel.Factory,
        extras = MutableCreationExtras().apply {
            set(OFFLINE_MAP_KEY, TomTomSdk.getOfflineRegionStore())
            set(ON_SET_IS_LOADING_KEY) { isLoading: Boolean -> demoViewModel.setIsLoading(isLoading) }
            set(ON_OPERATION_FAILED_KEY) { demoViewModel.updateErrorState { MapManagementError } }
        },
    ),
) {
    val isDeviceInLandscape = isDeviceInLandscape()
    val mapUiState by demoViewModel.mapUiState.collectAsStateWithLifecycle()
    val mapDisplayInfrastructure by demoViewModel.mapDisplayInfrastructure.collectAsStateWithLifecycle()
    val mapViewState = rememberMapViewState(
        initialCameraOptions = InitialCameraOptions.LocationBased(
            position = TOMTOM_AMSTERDAM_OFFICE,
        ),
    )
    val operationProgress by viewModel.operationProgress.collectAsStateWithLifecycle()
    val updateSize by viewModel.updateSize.collectAsStateWithLifecycle()
    val installedSize by viewModel.installedSize.collectAsStateWithLifecycle()
    val installState by viewModel.installState.collectAsStateWithLifecycle()
    val operationType by viewModel.operationType.collectAsStateWithLifecycle()
    val operationState by viewModel.operationState.collectAsStateWithLifecycle()

    Box(modifier = modifier) {
        DemoMap(
            mapUiState = mapUiState,
            mapDisplayInfrastructure = mapDisplayInfrastructure,
            isDeviceInLandscape = isDeviceInLandscape,
            mapViewState = mapViewState,
        )

        LaunchedEffect(Unit) {
            demoViewModel.updateSafeAreaTopPadding(0)
        }

        LoadingOverlay(isLoading = mapUiState.isLoading)

        if (!mapUiState.isLoading) {
            BottomPanel(
                isDeviceInLandscape = isDeviceInLandscape,
                onDownloadClick = { viewModel.downloadRegion() },
                onDeleteClick = { viewModel.deleteRegion() },
                onCancelOperationClick = { viewModel.cancelOperation() },
                operationProgress = operationProgress,
                updateSize = updateSize,
                installedSize = installedSize,
                installState = installState,
                operationType = operationType,
                operationState = operationState,
            )
        }
    }
}

@Composable
private fun BottomPanel(
    isDeviceInLandscape: Boolean,
    modifier: Modifier = Modifier,
    onDownloadClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCancelOperationClick: () -> Unit,
    installedSize: Memory? = null,
    updateSize: Memory? = null,
    operationProgress: UInt = 0u,
    installState: RegionInstallState? = null,
    operationType: RegionOperation.Type? = null,
    operationState: OperationState = OperationState.COMPLETED,
) {
    val sheetPeekHeight = remember { 265.dp }

    FixedHeightBottomSheet(
        sheetPeekHeight = sheetPeekHeight,
        modifier = modifier,
        isDeviceInLandscape = isDeviceInLandscape,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
        ) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            ) {
                val warningText = stringResource(R.string.demo_manual_map_management_evaluation_note)
                val contactSalesText = stringResource(R.string.demo_manual_map_management_contact_sales)
                val toGetStartedText = stringResource(R.string.demo_manual_map_management_evaluation_get_started)
                val linkColor = MaterialTheme.colorScheme.primary
                Text(
                    text = buildAnnotatedString {
                        append("$warningText ")
                        withLink(
                            LinkAnnotation.Url(
                                url = CONTACT_SALES_URL,
                                styles = TextLinkStyles(
                                    SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline),
                                ),
                            ),
                        ) {
                            append(contactSalesText)
                        }
                        append(" $toGetStartedText")
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                installedSize?.let {
                    Text(
                        text = stringResource(
                            R.string.demo_manual_map_management_region_installed_size_text,
                            it.inMebibytes().toInt(),
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }

                updateSize?.let {
                    Text(
                        text = stringResource(
                            R.string.demo_manual_map_management_region_update_size_text,
                            it.inMebibytes().toInt(),
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
            }

            when {
                operationState == OperationState.STARTING -> {
                    CircularProgressIndicator(
                        modifier = modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 32.dp, bottom = 8.dp),
                    )
                }

                installState == RegionInstallState.PartiallyInstalled &&
                    operationState == OperationState.IN_PROGRESS -> {
                    OperationInProgressBottomPanelDetails(
                        operationType = operationType,
                        operationProgress = operationProgress,
                        onCancelOperationClick = onCancelOperationClick,
                    )
                }

                installState == RegionInstallState.CompletelyInstalled -> {
                    BottomPanelDetails(
                        text = stringResource(R.string.demo_manual_map_management_success_download),
                        buttonText = stringResource(R.string.demo_manual_map_management_delete_button),
                        onButtonClick = onDeleteClick,
                    )
                }

                else -> {
                    ReadyToDownloadBottomPanelDetails(
                        installState = installState,
                        onDownloadClick = onDownloadClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.OperationInProgressBottomPanelDetails(
    modifier: Modifier = Modifier,
    operationType: RegionOperation.Type? = null,
    operationProgress: UInt = 0u,
    onCancelOperationClick: () -> Unit = {},
) {
    BottomPanelDetails(
        text = if (operationType == RegionOperation.Type.Download) {
            stringResource(R.string.demo_manual_map_management_downloading)
        } else {
            stringResource(R.string.demo_manual_map_management_removing)
        },
        buttonText = stringResource(R.string.demo_manual_map_management_cancel_button),
        onButtonClick = onCancelOperationClick,
        progressIndicator = {
            if (operationType == RegionOperation.Type.Download) {
                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    LinearProgressIndicator(
                        progress = { (operationProgress.toFloat() / 100f) },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                    )
                    Text(
                        text = stringResource(
                            R.string.demo_manual_map_management_progress_text,
                            operationProgress.toInt(),
                        ),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )
                }
            } else {
                CircularProgressIndicator(
                    modifier = modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 8.dp),
                )
            }
        },
    )
}

@Composable
private fun ColumnScope.ReadyToDownloadBottomPanelDetails(
    installState: RegionInstallState?,
    onDownloadClick: () -> Unit,
) {
    BottomPanelDetails(
        text = when (installState) {
            RegionInstallState.NotInstalled -> {
                stringResource(R.string.demo_manual_map_management_not_installed_text)
            }
            RegionInstallState.PartiallyInstalled -> {
                stringResource(R.string.demo_manual_map_management_partially_installed_text)
            }
            else -> {
                stringResource(R.string.demo_manual_map_management_error_text)
            }
        },
        buttonText = stringResource(R.string.demo_manual_map_management_download_button),
        onButtonClick = onDownloadClick,
        textColor = when (installState) {
            RegionInstallState.NotInstalled, RegionInstallState.PartiallyInstalled -> {
                MaterialTheme.colorScheme.secondary
            }
            else -> {
                MaterialTheme.colorScheme.error
            }
        },
    )
}

@Composable
private fun ColumnScope.BottomPanelDetails(
    text: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.secondary,
    progressIndicator: @Composable (() -> Unit) = { },
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = textColor,
        textAlign = TextAlign.Center,
        modifier = modifier
            .align(Alignment.CenterHorizontally)
            .padding(top = 8.dp),
    )

    progressIndicator()

    Button(
        onClick = onButtonClick,
        modifier = modifier
            .align(Alignment.CenterHorizontally)
            .padding(vertical = 16.dp),
    ) {
        Text(text = buttonText)
    }
}
