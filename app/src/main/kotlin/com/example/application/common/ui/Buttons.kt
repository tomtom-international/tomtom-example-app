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

@file:Suppress("detekt:TooManyFunctions")

package com.example.application.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.application.ui.theme.NavSdkExampleTheme

@Composable
fun TextCheckBox(
    text: String,
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit),
    modifier: Modifier = Modifier,
) {
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier
                .testTag("checkbox_$text")
                .semantics { contentDescription = "checkbox_$text" },
        )
    }
}

@Composable
fun TextRadioButton(
    key: Any,
    text: String,
    selected: Boolean,
    onOptionSelected: (Any) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .selectable(
                selected = selected,
                onClick = { onOptionSelected(key) },
                role = Role.RadioButton,
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
        )
        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MapIconButton(
    onClick: () -> Unit,
    imageVector: ImageVector,
    contentDescription: String,
    iconTint: Color,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .shadow(
                elevation = 6.0.dp,
                shape = CircleShape,
            )
            .background(
                color = backgroundColor,
                shape = CircleShape,
            ),
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = iconTint,
        )
    }
}

@Composable
fun MapModeToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    IconToggleButton(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier
            .shadow(
                elevation = 6.0.dp,
                shape = CircleShape,
            )
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = CircleShape,
            ),
        enabled = true,
    ) {
        Icon(
            painter = if (checked) {
                painterResource(id = R.drawable.tt_asset_icon_route_line_32)
            } else {
                painterResource(id = R.drawable.tt_asset_icon_chevrondrivingview_line_32)
            },
            contentDescription = stringResource(R.string.common_content_description_toggle_2D_3D),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
        )
    }
}

@PreviewLightDark
@Composable
private fun MapModeToggleButtonPreview() {
    NavSdkExampleTheme {
        MapModeToggleButton(
            checked = true,
            onCheckedChange = {},
            modifier = Modifier,
        )
    }
}

@Composable
fun CloseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
) = MapIconButton(
    onClick = onClick,
    imageVector = Icons.Filled.Close,
    contentDescription = stringResource(R.string.common_content_description_close),
    modifier = modifier,
    iconTint = iconTint,
    backgroundColor = backgroundColor,
)

@PreviewLightDark
@Composable
private fun CloseButtonPreview() {
    NavSdkExampleTheme {
        CloseButton(
            onClick = {},
            modifier = Modifier,
        )
    }
}

@Composable
fun RecenterMapButton(
    stateHolder: RecenterMapStateHolder,
    modifier: Modifier = Modifier,
) {
    if (stateHolder.isInteractiveMode) {
        MapIconButton(
            onClick = stateHolder.onRecenterClick,
            imageVector = ImageVector.vectorResource(R.drawable.tt_asset_icon_recenter_line_32),
            contentDescription = stringResource(id = R.string.common_content_description_center_on_user),
            modifier = modifier,
            iconTint = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
fun SettingsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MapIconButton(
        onClick = onClick,
        imageVector = Icons.Default.Settings,
        contentDescription = stringResource(R.string.common_content_description_settings),
        modifier = modifier,
        iconTint = MaterialTheme.colorScheme.primary,
    )
}

@Stable
data class RecenterMapStateHolder(
    val isInteractiveMode: Boolean,
    val onRecenterClick: () -> Unit,
)

@PreviewLightDark
@Composable
private fun RecenterMapButtonPreview() {
    NavSdkExampleTheme {
        RecenterMapButton(
            stateHolder = RecenterMapStateHolder(isInteractiveMode = true, onRecenterClick = {}),
            modifier = Modifier,
        )
    }
}

@Composable
fun SearchIcon(modifier: Modifier = Modifier) = Icon(
    imageVector = Icons.Default.Search,
    contentDescription = stringResource(id = R.string.search_content_description_search),
    modifier = modifier,
    tint = MaterialTheme.colorScheme.primary,
)

@PreviewLightDark
@Composable
private fun SearchIconPreview() {
    NavSdkExampleTheme {
        SearchIcon(
            modifier = Modifier,
        )
    }
}

@Composable
fun ArrowDownIconButton(
    modifier: Modifier = Modifier,
    onArrowDownIconClicked: () -> Unit = {},
) = IconButton(
    onClick = onArrowDownIconClicked,
    modifier = modifier,
) {
    Icon(
        imageVector = Icons.Default.KeyboardArrowDown,
        contentDescription = stringResource(id = R.string.search_content_description_reset),
        tint = MaterialTheme.colorScheme.primary,
    )
}

@PreviewLightDark
@Composable
private fun ArrowDownIconButtonPreview() {
    NavSdkExampleTheme {
        ArrowDownIconButton(
            modifier = Modifier,
        )
    }
}

@Composable
fun ClearSearchIconButton(
    modifier: Modifier = Modifier,
    onClearSearchIconClicked: () -> Unit = {},
) = IconButton(
    onClick = onClearSearchIconClicked,
    modifier = modifier,
) {
    Icon(
        imageVector = Icons.Default.Close,
        contentDescription = stringResource(id = R.string.common_content_description_clear_search),
        tint = MaterialTheme.colorScheme.primary,
    )
}

@PreviewLightDark
@Composable
private fun ClearSearchIconButtonPreview() {
    NavSdkExampleTheme {
        ClearSearchIconButton(
            modifier = Modifier,
        )
    }
}

@Composable
fun PoiIconButton(
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) = IconButton(
    onClick = onClick,
    modifier = modifier
        .background(
            color = MaterialTheme.colorScheme.background,
            shape = CircleShape,
        )
        .border(
            width = 2.dp,
            color = MaterialTheme.colorScheme.primary,
            shape = CircleShape,
        ),
) {
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        tint = MaterialTheme.colorScheme.primary,
    )
}

@PreviewLightDark
@Composable
private fun PoiIconButtonPreview() {
    NavSdkExampleTheme {
        PoiIconButton(
            imageVector = ImageVector.vectorResource(id = R.drawable.tt_asset_icon_hotelmotel_fill_32),
            contentDescription = "",
            onClick = {},
            modifier = Modifier,
        )
    }
}

@Composable
fun CustomIconButton(
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    onIconClick: () -> Unit = {},
    tint: Color = MaterialTheme.colorScheme.primary,
) = IconButton(
    onClick = onIconClick,
    modifier = modifier,
) {
    Icon(
        imageVector = imageVector,
        contentDescription = stringResource(id = R.string.common_content_description_clear_search),
        tint = tint,
        modifier = Modifier.fillMaxSize(),
    )
}

@PreviewLightDark
@Composable
private fun CustomIconButtonPreview() {
    NavSdkExampleTheme {
        CustomIconButton(
            imageVector = ImageVector.vectorResource(id = R.drawable.tt_asset_icon_filter_line_32),
            modifier = Modifier,
        )
    }
}
