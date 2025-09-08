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

package com.example.application.common.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.application.map.model.MapScreenUiState.ErrorState
import com.example.application.map.model.MapScreenUiState.ErrorState.RoutingError
import com.example.application.map.model.MapScreenUiState.ErrorState.SearchError
import com.example.application.ui.theme.NavSdkExampleTheme
import kotlinx.coroutines.flow.StateFlow

@Composable
private fun ErrorSnackbar(
    errorMessage: String,
    modifier: Modifier = Modifier,
) = Snackbar(
    modifier = modifier,
    content = {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = stringResource(R.string.common_error_message),
            tint = MaterialTheme.colorScheme.error,
        )
        Text(
            text = errorMessage,
            color = MaterialTheme.colorScheme.inversePrimary,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    },
    containerColor = MaterialTheme.colorScheme.inverseSurface,
)

@Composable
fun ErrorSnackbarHost(
    errorStateFlow: StateFlow<ErrorState?>,
    modifier: Modifier = Modifier,
    onErrorShown: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val errorState by errorStateFlow.collectAsStateWithLifecycle()

    ErrorStateListener(errorState, snackbarHostState, onErrorShown)
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = modifier,
        snackbar = {
            ErrorSnackbar(
                errorMessage = it.visuals.message,
            )
        },
    )
}

@Composable
private fun ErrorStateListener(
    errorState: ErrorState?,
    snackbarHostState: SnackbarHostState,
    onErrorShown: () -> Unit,
) {
    errorState?.let { error ->
        val message = when (error) {
            is SearchError -> stringResource(R.string.search_error_failed)
            is RoutingError -> stringResource(R.string.navigation_error_routing_failed)
        }
        LaunchedEffect(error) {
            snackbarHostState.showSnackbar(message)
            onErrorShown()
        }
    }
}

@PreviewLightDark
@Composable
private fun ErrorSnackbarPreview() {
    NavSdkExampleTheme {
        ErrorSnackbar(
            errorMessage = "Error message",
            modifier = Modifier,
        )
    }
}
