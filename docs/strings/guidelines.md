# String Resource Naming Guidelines

## Overview
This document outlines the naming conventions for string resources in the Open Source Example application. Following these guidelines ensures consistency, maintainability, and clarity in the codebase.

## General Principles
1. **Descriptive Names**: Use descriptive names that clearly indicate the purpose and context of the string.
2. **Consistency**: Follow consistent patterns for similar types of strings.
3. **Specificity**: Be specific enough to avoid confusion but not so specific that the name becomes unwieldy.
4. **Lowercase with Underscores**: Use lowercase letters with underscores to separate words (snake_case).
5. **Avoid Abbreviations**: Use full words rather than abbreviations unless the abbreviation is widely recognized.

## Naming Patterns

### 1. Feature Prefixes
All strings should be prefixed with the feature or module they belong to, followed by an underscore.

Examples:
- `map_`
- `arrival_`
- `search_`
- `settings_`
- `common_`
- `guidance_`

**The `common_` prefix should only be used for strings that are used in two or more modules/features. For strings that are used in only one module, use the specific feature/module prefix they belong to.**

### 2. String Types
After the feature prefix, include a type identifier to categorize the string's purpose.

Examples:

- `title_`: For titles of screens, sections, or components
- `subtitle_`: For subtitles or descriptions
- `button_`: For button text
- `label_`: For text labels
- `hint_`: For input field hints
- `error_`: For error messages
- `success_`: For success messages
- `info_`: For informational messages
- `content_description_`: For accessibility content descriptions
- `format_`: For format strings

Note: every time a new type is added, it should be documented here.

### 3. Specific Identifiers
After the type, include a specific identifier that describes the content or context of the string. If the string name is clear and descriptive, it can be omitted.

## Examples

### Titles and Subtitles
- `demo_search_ev_title_bottomsheet`: Title for EV search bottom sheet in demo.

### Buttons
- `navigation_button_route`: Text for planning a route.
- `navigation_button_add_stop`: Text for adding a stop during navigation.

### Content Descriptions
- `search_content_description_clear`: Content description for clear search button
- `poi_content_description_gas_station`: Content description for Gas station POI

### Error Messages
- `route_error_planning_failed`: Error message for route planning failure --> Routing failed
- `search_error_empty_results`: Error message for no search results --> No results for this input

### Format Strings
- `common_format_two_items`: Format for combining two items (e.g., "%1$s %2$s")
- <string name="demo_search_ev_format_power">%1$s kW</string>

## Special Cases

### Demo or Example Strings
For strings used in demo or example features:

- If a string is used only in demos, use the `demo_` prefix followed by the demo name:
  - `demo_search_ev_title_charging_speed`: Title for EV charging speed in search demo.
  - `demo_route_label_fastest`: Label for fastest route option in route demo.
  - `demo_custom_map_style_title`: Title for custom map style screen.
  - `demo_route_planning_subtitle`: Subtitle for route planning section.
- If a string is used in two or more demos (only demos), use the `demo_common_` prefix:
  - `demo_common_button_submit`: Submit button used in multiple demos.
- If a string is used in both demos and other features, use the specific `common_` prefix:
  - `common_content_description_search`: Submit button used both in search demo and in the search feature.

### Application Name
The application name should be simply:
- `app_name`

## Implementation Guidelines

1. Group related strings together in the strings.xml file.
2. Sort strings alphabetically within each group based on the variable name (not the variable value).
3. Review existing strings periodically to ensure they follow the guidelines.
