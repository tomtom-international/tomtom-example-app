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

package com.example.application.common

/**
 * Test tags for UI Automator and Compose testing.
 * These tags are used as resource IDs when testTagsAsResourceId is enabled.
 */
object TestTags {
    const val MAP_SCREEN = "map_screen"
    const val MAP_VIEW = "map_view"
    const val ARROW_DOWN_ICON_BUTTON = "arrow_down_icon_button"
    const val CHARGE_POINTS_TOGGLE = "charge_points_toggle"
    const val CLEAR_SEARCH_ICON_BUTTON = "clear_search_icon_button"
    const val CLOSE_BUTTON = "close_button"
    const val EV_FILTER_BACK_BUTTON = "ev_filter_back_button"
    const val EV_FILTER_ICON_BUTTON = "ev_filter_icon_button"
    const val EV_RESET_FILTERS_BUTTON = "ev_reset_filters_button"
    const val MAP_MODE_TOGGLE_BUTTON = "map_mode_toggle_button"
    const val NEARBY_POI_TOGGLE = "nearby_poi_toggle"
    const val RECENTER_MAP_BUTTON = "recenter_map_button"
    const val SETTINGS_BUTTON = "settings_button"
    const val ETA_TEXT = "eta_text"

    // Dynamic test tags (use these as templates)
    fun checkbox(text: String) = "checkbox_$text"

    fun radioButton(text: String) = "radio_button_$text"

    fun evFilterCategory(filterDescription: String) = "ev_filter_category_$filterDescription"

    fun evFilterOption(optionDescription: String) = "ev_filter_option_$optionDescription"

    fun nearbyPoiIcon(category: String) = "nearby_poi_icon_$category"

    fun poiIconButton(contentDescription: String) = "poi_icon_button_$contentDescription"

    fun evSearchResultItem(name: String) = "ev_search_result_item_$name"
}
