# TomTom Example App

## Introduction

The TomTom Example App is a fully functional demonstration application created to illustrate the practical usage of the Navigation SDK. It serves as a hands-on reference for developers, enabling them to explore how to integrate and use the powerful
features of the SDK in their own projects. Following Android Compose’s best practices and developed in Kotlin, the app
highlights modern development paradigms and efficient ways to build navigation-centric applications.

## What you’ll find inside

- Full **turn‑by‑turn navigation** with voice guidance & live traffic
- Flexible **routing** (alternatives, EV‑aware planning)
- Online/offline **map rendering** with custom styles
- Powerful **search** (POI, autocomplete, along‑route, area)
- Isolated **demos** for each feature

## Prerequisites

| Tool        | Minimum |
|-------------|---------|
| JDK         | 17      |
| Gradle      | 8.14    |
| Android SDK | API 26  |

## Before Starting

Make sure you have configured your TomTom API Key in the project! Store the TomTom API key to a project property in your
`gradle.properties` file. Replace `api_key_placeholder` with the actual TomTom API key:

```
tomtomApiKey=api_key_placeholder
```

Also, if you have access to an offline map, you have to set the password for the keystore in `gradle.properties` too.
Replace `your_password_here` with the actual password:

```
ndsKeystorePassword=your_password_here
```

### Map renderer access

The app uses the standard map renderer by default. In order to use the app with the premium renderer please [contact us](https://www.tomtom.com/contact-sales/?source_app=developerportal) to get started.

## Getting Started

The "Getting Started Guide" is meant to help developers navigate the TomTom Example App and get started with the NavSDK.
It provides clear instructions on how to set up a development environment and compile the app locally. It can be found
here:

[🚀 Getting Started Guide](https://developer.tomtom.com/navigation/android/tomtom-example-app/quickstart)

### Compile and Run It

Before diving into the app's functionality, you'll need to build and run it on your device or emulator. There are two
main ways to do this: using Android Studio or using CLI commands directly.

#### Android Studio

1. Open the application directory in Android Studio. Make sure you open the directory of the application itself for
   Android Studio to recognize it as an Android project.
2. Sync the project and build it using the Android Studio interface.
3. Run the app on your device or emulator.

#### CLI

1. Navigate to the project directory in your terminal.
2. Run the following command: `./gradlew assembleRelease`
3. Install the built APK on your device using ADB:
   `adb install app/build/outputs/apk/release/open-source-example-release.apk`

