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

package com.example.application.settings.ui

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.BuildConfig
import com.example.R
import com.example.application.settings.data.SettingsRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsRepository: SettingsRepository,
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory,
        extras = MutableCreationExtras().apply {
            set(SettingsViewModel.APPLICATION, LocalContext.current.applicationContext as Application)
            set(SettingsViewModel.SETTINGS_REPOSITORY, settingsRepository)
        },
    ),
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(
        rememberTopAppBarState(),
    )
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SettingsPageHeader(
                title = stringResource(id = R.string.settings_title),
                onNavigateBack = onNavigateBack,
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        val settingsUiState by viewModel.settingsUiState.collectAsState()

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .background(color = MaterialTheme.colorScheme.background),
        ) {
            if (settingsUiState is SettingsUiState.Enabled) {
                SettingsList()
            }
        }
    }
}

@Composable
private fun SettingsList() {
    VersionInfo(versionName = BuildConfig.TOMTOM_NAV_SDK, buildType = BuildConfig.BUILD_TYPE)
}
