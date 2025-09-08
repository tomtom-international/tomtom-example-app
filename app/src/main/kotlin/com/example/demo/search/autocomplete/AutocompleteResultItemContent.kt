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
