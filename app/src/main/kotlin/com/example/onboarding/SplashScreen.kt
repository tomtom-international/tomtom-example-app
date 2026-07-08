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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.R

private const val LOGO_WIDTH_FRACTION = 0.3f

/**
 * Visual splash content showing the TomTom logo.
 */
@Composable
internal fun SplashContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.tomtom_logo_small),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth(LOGO_WIDTH_FRACTION)
                .aspectRatio(1f),
        )
    }
}

@Composable
internal fun MapExtractionFailedDialog(onMapExtractionFailed: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text(stringResource(R.string.onboarding_error_install_map_title)) },
        text = { Text(stringResource(R.string.onboarding_error_install_map_message)) },
        confirmButton = {
            TextButton(onClick = onMapExtractionFailed) {
                Text(stringResource(R.string.onboarding_error_install_map_button_close))
            }
        },
    )
}
