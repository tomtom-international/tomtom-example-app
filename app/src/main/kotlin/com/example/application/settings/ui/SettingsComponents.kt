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

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPageHeader(
    title: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(title)
        },
        navigationIcon = {
            IconButton(
                onClick = { onNavigateBack() },
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.settings_content_description_back),
                )
            }
        },
        scrollBehavior = scrollBehavior,
    )
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier.padding(start = 20.dp, top = 10.dp),
        text = title,
        color = MaterialTheme.colorScheme.primary,
        fontSize = 18.sp,
    )
}

@Composable
fun VersionInfo(
    versionName: String,
    modifier: Modifier = Modifier,
    buildType: String = "",
) {
    SettingUI(
        modifier = modifier,
        title = stringResource(id = R.string.settings_label_navsdk_version),
        enabled = true,
    ) {
        val versionString = versionName +
            if (buildType.isNotEmpty()) {
                "/${buildType.uppercase()}"
            } else {
                ""
            }
        Text(text = versionString)
    }
}

@Composable
fun SwitchSettingUI(
    title: String,
    onSwitchChanged: (Boolean) -> Unit,
    settingValue: Boolean,
    modifier: Modifier = Modifier,
    description: String? = null,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    SettingUI(
        modifier = modifier
            .toggleable(
                enabled = enabled,
                role = Role.Switch,
                value = settingValue,
                onValueChange = { value -> onSwitchChanged(value) },
            ),
        enabled = enabled,
        title = title,
        description = description,
        leadingIcon = leadingIcon,
        trailingContent = {
            Switch(
                enabled = enabled,
                checked = settingValue,
                onCheckedChange = { value ->
                    onSwitchChanged(value)
                },
            )
        },
    )
}

@Composable
private fun SettingUI(
    title: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    description: String? = null,
    trailingContent: @Composable (() -> Unit)?,
) {
    ListItem(
        modifier = modifier,
        headlineContent = {
            if (enabled) {
                Text(title)
            } else {
                Text(text = title, color = LocalContentColor.current.copy(alpha = .7f))
            }
        },
        supportingContent = {
            if (description != null) {
                if (enabled) {
                    Text(description)
                } else {
                    Text(
                        text = description,
                        color = LocalContentColor.current.copy(alpha = .7f),
                    )
                }
            }
        },
        leadingContent = leadingIcon,
        trailingContent = trailingContent,
    )
}
