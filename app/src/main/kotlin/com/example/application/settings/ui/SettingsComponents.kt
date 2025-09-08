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

package com.example.application.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

@Composable
fun TelemetryConsentLevelSelector(
    modifier: Modifier = Modifier,
    selectedIndex: Int?,
    onValueChange: (Int) -> Unit,
) {
    selectedIndex?.let {
        DropdownSelector(
            title = stringResource(R.string.settings_label_telemetry_consent),
            itemList = mutableListOf(
                stringResource(R.string.settings_label_telemetry_consent_off),
                stringResource(R.string.settings_label_telemetry_consent_location_only),
                stringResource(R.string.settings_label_telemetry_consent_on),
            ),
            selectedIndex = it + 1,
            onValueSelected = { newValue ->
                if (newValue != selectedIndex + 1) {
                    onValueChange(newValue)
                }
            },
            modifier = modifier,
        )
    }
}

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
fun SingleChoiceSelector(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    secondaryText: String? = null,
    enabled: Boolean,
) {
    Row(
        modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick,
                enabled = enabled,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingUI(
            title = text,
            description = secondaryText,
            enabled = enabled,
            leadingIcon = {
                RadioButton(
                    selected = selected,
                    onClick = onClick,
                    enabled = enabled,
                )
            },
            trailingContent = null,
        )
    }
}

@Composable
private fun DropdownSelector(
    title: String,
    itemList: List<String>,
    selectedIndex: Int,
    onValueSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    var expanded by remember { mutableStateOf(false) }

    SettingUI(
        modifier = modifier,
        enabled = enabled,
        title = title,
        description = description,
        leadingIcon = leadingIcon,
        trailingContent = {
            Box {
                Text(text = itemList[selectedIndex], modifier = Modifier.clickable { expanded = !expanded })

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    itemList.forEachIndexed { index, item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                onValueSelected(index)
                                expanded = false
                            },
                        )
                    }
                }
            }
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
