# TomTom Example App

## Introduction

The TomTom Example App is a fully functional demonstration application created to illustrate the practical usage of the
Tomtom Android SDK. It serves as a hands-on reference for developers, enabling them to explore how to integrate and use the powerful
features of the SDK in their own projects. Following Android Compose’s best practices and developed in Kotlin, the app
highlights modern development paradigms and efficient ways to build navigation-centric applications.

## What you’ll find inside

- Full **turn‑by‑turn navigation** with voice guidance & live traffic
- Flexible **routing** (alternatives, EV‑aware planning)
- **map rendering** with custom styles
- Powerful **search** (POI, autocomplete, along‑route, area)
- Isolated **demos** for each feature

## Prerequisites

| Tool        | Minimum |
|-------------|---------|
| JDK         | 17      |
| Gradle      | 8.14    |
| Android SDK | API 26  |

## Setup

Because the repository for TomTom Navigation SDK is private, you will need to contact us to get access. Once you have obtained access

## Add in the gradle.properties file
Add the entries below to the global `gradle.properties` file.

```
# Required to access Artifactory
repositoriesTomtomComUsername=your_user_name
repositoriesTomtomComPassword=your_password

# Required to use TomTom APIs
tomtomApiKey=your_api_key
```

### Compile and Run It

Before diving into the app's functionality, you'll need to build and run it on your device or emulator. There are two
main ways to do this: using Android Studio or using CLI commands directly.


1. Open the application directory in Android Studio. Make sure you open the directory of the application itself for
   Android Studio to recognize it as an Android project.
2. Sync the project and build it using the Android Studio interface.
3. Run the app on your device or emulator.

#### CLI

1. Navigate to the project directory in your terminal.
2. Run the following command: `./gradlew assembleRelease`
3. Install the built APK on your device using ADB:
   `adb install app/build/outputs/apk/release/open-source-example-release.apk`

