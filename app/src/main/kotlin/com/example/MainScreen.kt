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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.Destination.ChildActivityDestination.NavigationActivityDestination
import com.example.Destination.DemoListScreenDestination
import com.example.application.ui.theme.NavSdkExampleTheme

/**
 * This is the entry screen to the application and handles the Telemetry Consent prompt for the user.
 */
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onNavigateToDestination: (Destination) -> Unit,
    onExitApp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isTelemetryConsentDialogShown) {
        TelemetryConsentDialog(
            onDismissRequest = onExitApp,
            onConfirm = { viewModel.confirmTelemetryConsent(it) },
        )
    }

    PaddedLazyColumn(modifier = modifier) {
        item {
            DestinationCard(
                onClick = { onNavigateToDestination(NavigationActivityDestination) },
                title = stringResource(R.string.navsdk_application_title),
                subtitle = stringResource(R.string.navsdk_application_subtitle),
            )
        }

        item {
            DestinationCard(
                onClick = { onNavigateToDestination(DemoListScreenDestination) },
                title = stringResource(R.string.demo_examples_title),
                subtitle = stringResource(R.string.demo_examples_subtitle),
            )
        }
    }
}

@Composable
private fun TelemetryConsentDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(dismissOnClickOutside = false),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        ) {
            var selectedTelemetryIndex by rememberSaveable { mutableIntStateOf(-1) }
            val options = listOf(
                stringResource(R.string.settings_label_telemetry_consent_off),
                stringResource(R.string.settings_label_telemetry_consent_location_only),
                stringResource(R.string.settings_label_telemetry_consent_on),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(weight = 0.65f),
                    text = stringResource(R.string.main_telemetry_dialog_label),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Start,
                )

                Box(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(weight = 0.35f)
                        .background(MaterialTheme.colorScheme.inversePrimary),
                    contentAlignment = Alignment.Center,
                ) {
                    var expanded by remember { mutableStateOf(false) }
                    Text(
                        text = options[selectedTelemetryIndex + 1],
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { expanded = !expanded }
                            .padding(8.dp),
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        options.forEachIndexed { index, item ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = item,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                },
                                onClick = {
                                    selectedTelemetryIndex = index - 1
                                    expanded = false
                                },
                            )
                        }
                    }
                }
            }

            Button(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp),
                onClick = { onConfirm(selectedTelemetryIndex) },
            ) {
                Text(
                    text = stringResource(R.string.main_telemetry_dialog_button),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

@Composable
fun PaddedLazyColumn(
    modifier: Modifier,
    content: LazyListScope.() -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(8.dp),
        content = content,
    )
}

@PreviewLightDark
@Composable
private fun PaddedLazyColumnPreview() {
    NavSdkExampleTheme {
        PaddedLazyColumn(modifier = Modifier) {
            item {
                DestinationCard(
                    onClick = { },
                    title = "Destination Card Title",
                    subtitle = "Destination Card Subtitle. Subtitle description.",
                )
            }
            item {
                DestinationCard(
                    onClick = { },
                    title = "Destination Card Title",
                    subtitle = "Destination Card Subtitle. Subtitle description.",
                )
            }
        }
    }
}

@Composable
fun DestinationCard(
    onClick: () -> Unit,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp),
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun DestinationCardPreview() {
    NavSdkExampleTheme {
        Column {
            DestinationCard(
                onClick = { },
                title = "Destination Card Title",
                subtitle = "Destination Card Subtitle. Subtitle description.",
            )
        }
    }
}
