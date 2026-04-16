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

import com.android.build.gradle.internal.api.ApkVariantOutputImpl

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.style.ktlint)
}

val tomtomApiKey: String by project
val applicationNamespace = "com.example"

android {
    namespace = applicationNamespace
    compileSdk = 35

    buildFeatures {
        buildConfig = true
        compose = true
    }

    defaultConfig {
        applicationId = applicationNamespace
        versionCode = 1
        versionName = "1.0"

        minSdk = 26
        targetSdk = 34

        // Only 64-bit architectures are supported in TomTom Navigation SDK.
        ndk.abiFilters += listOf("arm64-v8a", "x86_64")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        missingDimensionStrategy("tomtom-sdk-version", "complete")

        buildConfigField("String", "TOMTOM_API_KEY", "\"$tomtomApiKey\"")
    }

    buildTypes {
        applicationVariants.configureEach {
            val flavorPart = if (productFlavors.isEmpty()) {
                ""
            } else {
                productFlavors
                    .filterNot { it.name.contains("public") }
                    .joinToString("-") { it.name.lowercase() } + "-"
            }
            outputs.configureEach {
                (this as? ApkVariantOutputImpl)?.outputFileName =
                    "open-source-example-${flavorPart}${buildType.name}.apk"
            }
        }

        release {
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = true
        }

        debug {
            isDebuggable = true
        }
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.kotlinCompilerExtensionVersion.get()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
        kotlinOptions.allWarningsAsErrors = true
    }

    ktlint {
        version.set(libs.versions.ktlintExt)
    }

    lint {
        warningsAsErrors = true
        abortOnError = true
        baseline = file("lint-baseline.xml")
    }

    packaging {
        resources.excludes.add("META-INF/*")
        resources.pickFirsts.add("META-INF/androidx.compose.*.version") // Keeps Compose in Layout Inspector
    }
}

dependencies {

    implementation(libs.androidxActivity.compose)
    implementation(libs.androidxComposeMaterial3)
    implementation(libs.androidxComposeUi)
    implementation(libs.androidxComposeUi.toolingPreview)
    implementation(platform(libs.androidxCompose.bom))
    implementation(libs.androidxCore.ktx)
    implementation(libs.androidxDatastore.preferences)
    implementation(libs.androidxNavigation.compose)
    implementation(libs.kotlinx.serializationJson)

    implementation(libs.tomtomSdk.init)
    implementation(libs.tomtomSdkCommon.configuration)
    implementation(libs.tomtomSdkLocation.provider.default)
    implementation(libs.tomtomSdkMaps.mapDisplayComposeStandard)
    implementation(libs.tomtomSdkMapsVisualization.navigationCompose)
    implementation(libs.tomtomSdkRouting.routePlanner)
    implementation(libs.tomtomSdkSearch.reverseGeocoder)
    implementation(libs.tomtomSdkSearch.search)

    debugImplementation(libs.androidxComposeUi.testManifest)
    debugImplementation(libs.androidxComposeUi.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutinesTest)
}
