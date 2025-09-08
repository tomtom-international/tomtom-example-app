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

package com.example.demo.search.autocomplete

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector
import com.tomtom.sdk.search.model.result.AutocompleteSegment
import com.tomtom.sdk.search.model.result.AutocompleteSegmentBrand
import com.tomtom.sdk.search.model.result.AutocompleteSegmentPlainText
import com.tomtom.sdk.search.model.result.AutocompleteSegmentPoiCategory

data class AutocompleteResultItemContent(
    val imageVector: ImageVector,
    val text: String,
    val segment: AutocompleteSegment,
)

fun AutocompleteSegment.toAutocompleteResultItemContent(): AutocompleteResultItemContent = when (this) {
    is AutocompleteSegmentBrand -> AutocompleteResultItemContent(
        imageVector = Icons.Default.ShoppingCart,
        text = this.brand.name,
        segment = this,
    )

    is AutocompleteSegmentPoiCategory -> AutocompleteResultItemContent(
        imageVector = Icons.Default.Menu,
        text = this.poiCategory.name,
        segment = this,
    )

    is AutocompleteSegmentPlainText -> AutocompleteResultItemContent(
        imageVector = Icons.Default.Create,
        text = this.plainText,
        segment = this,
    )

    else -> throw IllegalArgumentException("Unsupported AutocompleteSegment type: ${this::class.java.simpleName}")
}
