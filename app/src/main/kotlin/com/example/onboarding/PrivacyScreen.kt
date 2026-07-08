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

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.MainViewModel
import com.example.R
import com.example.application.ui.theme.NavSdkExampleTheme

/**
 * Onboarding screen where users select and persist their privacy settings.
 */
@Composable
fun PrivacyScreen(
    viewModel: MainViewModel,
    onConsentPersisted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedOption by rememberSaveable { mutableIntStateOf(TelemetryConsentLevel.LOCATION_ONLY.ordinal) }

    LaunchedEffect(uiState.persistedConsentLevel) {
        if (uiState.persistedConsentLevel != null) {
            onConsentPersisted()
        }
    }

    PrivacyScreenContent(
        selectedOption = selectedOption,
        onOptionSelected = { selectedOption = it },
        onProceed = { viewModel.onConsentConfirmed(selectedOption) },
        modifier = modifier,
    )
}

@Composable
private fun PrivacyScreenContent(
    selectedOption: Int,
    onOptionSelected: (Int) -> Unit,
    onProceed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = TelemetryConsentLevel.values().map { stringResource(it.labelRes) }

    OnboardingLayout(
        modifier = modifier,
        header = { PrivacyHeader() },
        content = { PrivacyOptions(options, selectedOption, onOptionSelected) },
        footer = {
            Button(
                onClick = onProceed,
            ) {
                Text(
                    text = stringResource(R.string.onboarding_privacy_button_proceed),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        },
    )
}

@Composable
private fun PrivacyHeader() {
    Text(
        text = stringResource(R.string.onboarding_privacy_title),
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.padding(bottom = 24.dp),
    )
    Text(
        text = stringResource(R.string.onboarding_privacy_description),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(bottom = 48.dp),
    )
}

@Composable
private fun PrivacyOptions(
    options: List<String>,
    selectedOption: Int,
    onOptionSelected: (Int) -> Unit,
) {
    options.forEachIndexed { index, text ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectable(
                    selected = (selectedOption == index),
                    onClick = { onOptionSelected(index) },
                    role = Role.RadioButton,
                )
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(
                selected = (selectedOption == index),
                onClick = null,
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 16.dp),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun PrivacyScreenPreview() {
    NavSdkExampleTheme {
        PrivacyScreenContent(
            selectedOption = TelemetryConsentLevel.LOCATION_ONLY.ordinal,
            onOptionSelected = {},
            onProceed = {},
        )
    }
}
