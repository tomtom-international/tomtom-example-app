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

@file:Suppress("detekt:TooManyFunctions")

package com.example.application.ui

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
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
                painterResource(id = R.drawable.route_24px)
            } else {
                painterResource(id = R.drawable.navigation_24px)
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
            imageVector = ImageVector.vectorResource(R.drawable.point_scan_24px),
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
            imageVector = ImageVector.vectorResource(id = R.drawable.hotel_24px),
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
            imageVector = ImageVector.vectorResource(id = R.drawable.outline_filter_alt_24px),
            modifier = Modifier,
        )
    }
}
