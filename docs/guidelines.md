# Open source application guidelines

# Table of contents

- [Terminology](#terminology)
- [Summary](#summary)
- [Code and code structure](#code-and-code-structure)
- [UI/UX](#uiux)

# Terminology

| Term               | Definition                                                                                                       |
|--------------------|------------------------------------------------------------------------------------------------------------------|
| Code reader        | A user that reads the open source application code in order to understand NavSDK integration into an application |
| App user           | An open source application user                                                                                  |
| Navigation example | The element of the application that presents the user with a fully-functioning navigation app                    |
| Detailed example   | Discrete domain and use-case specific examples intended to provide focused insight into NavSDK use               |

# Summary

These guidelines are applicable to the open source application and consider both types of app user - code reader and app
user.

# Code and code structure

This section details items that are specific to the application's code and its structure.

## The application comprises a 'navigation example' alongside 'detailed examples'

There are two parts to the application:

1. The navigation example - there is only one of these and it demonstrates a navigation application, planning a route to
   a search result and then navigating that route to the destination. This example type comprises a mix of
   TomTomApp-derived UI components (such as the NIP and speed limit) and standard Material UI components.
2. The detailed example(s) - each example demonstrates a well-defined feature that likely isn't a core part of the
   navigation example or is sufficiently nuanced and/or advanced that it would overcomplicate it. This
   example type should present a Map for user interaction and a
   [BottomSheet](https://developer.android.com/develop/ui/compose/components/bottom-sheets) used for input and output to
   the example. Other UI components must be avoided.

Deciding which of these two should be used for an new example is important in order
to [satisfy the requirements of the target users](overview.md).

## Detailed examples must be focused and manageable

- A single screen must offer an easy to use (from both target user's points of view) sample
- This might not necessarily be a single use-case (which would possible lead to an explosion of screens) but should be
  considered in terms of what the overall demonstration is
- The example screen should have a simple title that makes it easy for the app user and code reader to understand what
  it will allow them to do
- A `BottomSheet` must be used to contain any user-input UI components

# UI/UX

This section details items that are specific to the UI/UX elements of the application.

## Compose

UI components must be implemented using Jetpack Compose. Familiarity with the Android Compose guidelines is
necessary and can be found [here](https://developer.android.com/develop/ui/compose/api-guidelines)
and [here](https://github.com/androidx/androidx/tree/androidx-main/docs/api_guidelines/compose_api_guidelines).

## TomTomApp's UX/UI specifications are the primary reference

- The specifications for much of the UI exist and are proven, avoiding subjective opinion and providing a consistent
  look and feel
- Divergence should only occur when an aspect of the UI/UX is deemed to be superfluous to the goals of the application;
  that the app's usage experience is not significantly enhanced by the cost of the code change.
- Consideration for the maintenance of the code and the ease of understanding it by the code reader and app user should
  be made when diverging from the specifications
- UI/UX specifications may not be exhaustive and in these cases developers should decide whether the issue is
  sufficiently large as to warrant being brought to the attention of others

## The code is the specification

There is no specific specification for the UI components' look and feel. Once the UI component has been implemented and
merged, then that is the specification.

## Supplementary documentation required for significant deviations from specifications

Notable deviations from the specification must be supplemented with documentation - a readme file, named after the UI
component. This must capture how the UI component's implementation differs in some significant way. Exhaustive reasoning
is not required but the reasoning is that it avoids unnecessary revisits of the code.

## Implementation and theming

### Navigation components and non-navigation components

UI components are categorised as being one of two types:

1. A UI component that directly shows or interacts with a navigation-specific aspect of the application. Examples of
   these are the Next Instruction Panel (NIP), Speedlimit and Map.
2. A UI component that does not show or interact with a navigation-specific aspect of the application. Examples of this
   are the search text box and a list of search results.

This differentiation informs _how_ a UI component should be implemented and styled. Navigation-specific UI components (
ie the NIP) require a custom UI component to be created, whereas the UI components like the search text box must use an
out-of-the-box Material UI component with sufficent theming that it fits in with the rest of the application; TomTom App
UI/UX specs should not be applied to significantly customise the component, as this simply adds superfluous code which
needs to be maintained whilst remaining familiar, and thus easy to understand, to the code reader.

As an example, the open source application features a NIP UI component which shows guidance instructions to the
application user and is thus a custom UI component. Search, however, doesn't require a specific UI component, and
conducting a search query and displaying the search results can be done using standard Material UI components (ie a
`TextField` and `LazyList`, respectively). These search components should be implemented using standard Material
composables, with perhaps minor configuration (ie for the result item) and theming.

### UI components should be standardised across the application

UI components should be standardised in their look and feel across the application. For example, the 'detailed examples'
mentioned [here](#detailed-examples-must-be-focused-and-manageable) should use a Compose `BottomSheet` that behaves
consistently across all examples.

Avoid duplicating components, instead looking to reuse them, considering, for
example, [Compose slots](https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-component-api-guidelines.md#slot-parameters)
in order to enable the API flexibility of the UI component.

### Target screen sizes and densities

The application must consider the sizing of its UI components so that it presents the user with a reasonable experience
on all screen sizes. This is not a requirement to create multiple versions or special configurations for different
screen sizes/densities but rather to be aware the the overall experience should be usable and coherent.

`adb` supports changing the display size and density of devices and so useful to check how the application behaves on
different screen configurations:

```
adb shell wm size [reset|WxH|WdpxHdp] [-d DISPLAY_ID]
    Return or override display size.
    width and height in pixels unless suffixed with 'dp'.
adb shell wm density [reset|DENSITY] [-d DISPLAY_ID]
    Return or override display density.
```

### Theming

While TomTomApp implements a TomTom designer specified theme the open source application uses a Material theme and thus
the UI styling elements defined by that. The theme was generated using
the [Material Theme Builder](https://material-foundation.github.io/material-theme-builder/). Despite using the Material
theme the open source application can still visually replicate much of the TomTomApp UI. Using the Material theme with
out-of-the-box attributes keeps the lines of code down thereby reducing cognitive load for the code reader.

#### Text

- The attributes of the text are defined by the theme
- Text sizes and style attributes that are used by the application are intentionally limited

#### Colors

- The theme defines the colors used by the application
- Any Material UI components must use the standard theme colors. There will be cases where specific colors are required
  in order to replicate the TomTomApp UI specification (ie the NIP background color taken from the TomTomApp UI spec).
  These are not theme-derived and instead should be defined in the file where they're required.

#### Resources

- UI component sizes must not be defined in `res/dimens.xml` and instead declared where they're needed. This does mean
  duplication, but it also means that the source file is self-contained and simple to follow.
- Strings must be defined in the `res/strings.xml` file. To add a new string, follow the
  [String naming conventions](strings/guidelines.md) and add it to the file. The string should be descriptive and
  self-contained, so that it is clear what it is used for without needing to refer to the code.
- Given that the application's UI is intended to scale via Android's own mechanisms its UI components must consider the
  different screen sizes. See also the [Target screen sizes and densities](#target-screen-sizes-and-densities) section.

# Additional reading

- [OneApp-Design-System-Repository](https://github.com/tomtom-internal/OneApp-Design-System-Repository) - a repo
  containing theme and asset data obtained from Figma. Changes from this repo are then applied to TomTomApp by executing
  [this script](https://github.com/tomtom-internal/oneapp-android/blob/main/generate_design_tokens.py) which then
  results in the creation of a TomTomApp PR
  like [this one](https://github.com/tomtom-internal/oneapp-android/pull/1096)
- [Figma Driving Visual Specification](https://www.figma.com/design/GYnrFy1Z78A8Gz0jhlVr0A/Driving---Visual-Design-Specification?node-id=1-11085&p=f&t=or2sjUUlKqnXWeWV-0)
- [Figma Live Specifications](https://www.figma.com/files/920730078132659251/project/283945558?fuid=912819837886827844) -
  root page for UX behavior
- [Figma design for the Component Toolkit](https://www.figma.com/design/PTvTpsWN9CMw6evsAIUiGC/%F0%9F%9F%A2-LIVE-%7C-Component-Toolkit?node-id=22-10396&t=Ant03wHTKjcXSdkd-0)
  shows the UI component designs (ie core UI components such as list items, buttons, etc)
- [Figma design tokens](https://www.figma.com/design/sa87jmvfw7UqfYIl4z1yuN/Documentation-Automotive-UI-Toolkit?node-id=1815-3212&t=WgVTLkob8cRp8UI9-0)
