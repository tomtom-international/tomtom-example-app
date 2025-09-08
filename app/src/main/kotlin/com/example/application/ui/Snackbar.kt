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

package com.example.application.ui

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
import com.example.application.map.MapScreenUiState.ErrorState
import com.example.application.map.MapScreenUiState.ErrorState.RoutingError
import com.example.application.map.MapScreenUiState.ErrorState.SearchError
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
) {
    val snackbarHostState = remember { SnackbarHostState() }
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
