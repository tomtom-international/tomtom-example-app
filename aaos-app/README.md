<!--
© 2026 TomTom NV. All rights reserved.

This software is the proprietary copyright of TomTom NV and its subsidiaries and may be
used for internal evaluation purposes or commercial use strictly subject to separate
license agreement between you and TomTom NV. If you are the licensee, you are only permitted
to use this software in accordance with the terms of your license agreement. If you are
not the licensee, you are not authorized to use this software in any manner and should
immediately return or destroy it.
-->

# TomTom Android Automotive Example App

## Introduction

The TomTom Android Automotive Example App is an open-source demonstration application created to illustrate the
practical usage of the Navigation SDK in Android Automotive OS (AAOS) environments. It serves as a hands-on reference
for developers, enabling them to explore how to integrate and use the powerful features of the SDK in their own AAOS
projects.

### Compile and Run It

Before diving into the app's functionality, you'll need to build and run it on your Automotive emulator:

1. Open the application directory in Android Studio.
2. Sync the project and build it using the Android Studio interface.
3. Select the `aaos-app` configuration and run it on an Automotive emulator **without Google Play**.
   > **Note:** If you run the app on an emulator with Google Play, the app will be blocked in Driving mode. This is
   because Activities marked as distraction optimized are only treated as such when installed from a trusted source (
   such as the Google Play Store) or when running on a userdebug system image (such as emulators without the Google Play
   Store). [More info](https://developer.android.com/training/cars/apps/automotive-os#car-app-activity)
4. Depending on the emulator's vehicle configuration, EV stations may not appear when planning a route. To display them:
    - Go to Emulator Settings > Car Data > Vhal properties.
    - Search for `Nominal battery capacity for EV`.
    - For example, enter a value between `20000` and `75000`. You can adjust this value to see how it changes the number
      of EV stations displayed along your route.

### Beta APIs

This project uses some APIs that are still in beta. For more information about beta APIs and guidance on their usage,
please [contact us](https://www.tomtom.com/contact-sales/?source_app=open_source_app).
