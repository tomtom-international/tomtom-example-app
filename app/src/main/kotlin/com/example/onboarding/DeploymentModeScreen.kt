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

package com.example.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.DeploymentMode
import com.example.MainViewModel
import com.example.R
import com.example.SdkInitializationState
import com.example.application.ui.theme.NavSdkExampleTheme

/**
 * Onboarding screen where users select the deployment mode before SDK initialization.
 */
@Composable
fun DeploymentModeScreen(
    viewModel: MainViewModel,
    onSdkInitializationComplete: () -> Unit,
    onSdkInitializationFailed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.sdkInitializationState) {
        if (uiState.sdkInitializationState == SdkInitializationState.Completed) {
            onSdkInitializationComplete()
        }
    }

    DeploymentModeScreenContent(
        sdkInitializationState = uiState.sdkInitializationState,
        onDeploymentModeSelected = { viewModel.onDeploymentModeSelected(it) },
        onSdkInitializationFailed = onSdkInitializationFailed,
        modifier = modifier,
    )
}

@Composable
private fun DeploymentModeScreenContent(
    sdkInitializationState: SdkInitializationState,
    onDeploymentModeSelected: (DeploymentMode) -> Unit,
    onSdkInitializationFailed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OnboardingLayout(
        modifier = modifier,
        header = { DeploymentModeHeader() },
        content = {
            DeploymentModeButtons(
                onDeploymentModeSelected = onDeploymentModeSelected,
            )
        },
    )

    if (sdkInitializationState == SdkInitializationState.Failed) {
        SdkInitializationFailedDialog(onSdkInitializationFailed = onSdkInitializationFailed)
    }
}

@Composable
private fun DeploymentModeHeader() {
    Text(
        text = stringResource(R.string.onboarding_deployment_mode_title),
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.padding(bottom = 24.dp),
    )
    Text(
        text = stringResource(R.string.onboarding_deployment_mode_description),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(bottom = 48.dp),
    )
}

@Composable
private fun DeploymentModeButtons(onDeploymentModeSelected: (DeploymentMode) -> Unit) {
    Button(
        onClick = { onDeploymentModeSelected(DeploymentMode.ONLINE_ONLY) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Text(
            text = stringResource(R.string.onboarding_deployment_mode_button_online_only),
            style = MaterialTheme.typography.titleMedium,
        )
    }
    Box(modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = { onDeploymentModeSelected(DeploymentMode.ONLINE_FIRST) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        ) {
            Text(
                text = stringResource(R.string.onboarding_deployment_mode_button_offline_fallback),
                style = MaterialTheme.typography.titleMedium,
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .background(MaterialTheme.colorScheme.tertiary, RoundedCornerShape(4.dp))
                .padding(horizontal = 4.dp, vertical = 2.dp),
        ) {
            Text(
                text = "BETA",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onTertiary,
            )
        }
    }
}

@Composable
internal fun SdkInitializationFailedDialog(onSdkInitializationFailed: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text(stringResource(R.string.onboarding_error_sdk_init_title)) },
        text = { Text(stringResource(R.string.onboarding_error_sdk_init_message)) },
        confirmButton = {
            TextButton(onClick = onSdkInitializationFailed) {
                Text(stringResource(R.string.onboarding_error_sdk_init_button_close))
            }
        },
    )
}

@PreviewLightDark
@Composable
private fun DeploymentModeScreenPreview() {
    NavSdkExampleTheme {
        DeploymentModeScreenContent(
            sdkInitializationState = SdkInitializationState.NotStarted,
            onDeploymentModeSelected = {},
            onSdkInitializationFailed = {},
        )
    }
}
