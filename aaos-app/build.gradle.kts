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
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.style.ktlint)
}

val tomtomApiKey: String by project
val applicationNamespace = "com.example.automotive"

android {
    namespace = applicationNamespace
    compileSdk = 35

    buildFeatures {
        compose = true
        buildConfig = true
    }

    defaultConfig {
        applicationId = applicationNamespace
        versionCode = 1
        versionName = "1.0"

        minSdk = 29
        targetSdk = 35

        // Only 64-bit architectures are supported in TomTom Navigation SDK.
        ndk.abiFilters += listOf("arm64-v8a", "x86_64")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        missingDimensionStrategy("tomtom-sdk-version", "complete")

        buildConfigField("String", "TOMTOM_API_KEY", "\"$tomtomApiKey\"")
    }

    flavorDimensions += listOf("mapDisplay")

    productFlavors {
        create("standard") {
            dimension = "mapDisplay"
            applicationIdSuffix = ".standard"
            versionNameSuffix = "-standard"
        }

        create("premium") {
            dimension = "mapDisplay"
            applicationIdSuffix = ".premium"
            versionNameSuffix = "-premium"
            isDefault = true
        }
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
                    "open-source-example-aaos-${flavorPart}${buildType.name}.apk"
            }
        }

        release {
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
            languageVersion.set(KotlinVersion.KOTLIN_1_8)
            apiVersion.set(KotlinVersion.KOTLIN_1_8)
            allWarningsAsErrors.set(true)
        }
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

    useLibrary("android.car")
}

dependencies {

    implementation(libs.tomtomSdk.init)
    implementation(libs.tomtomSdkMapsVisualization.visualizationCompose)
    implementation(libs.tomtomSdkDatamanagementNds.sampleMap)
    implementation(libs.tomtomSdkLocation.provider.default)
    implementation(libs.tomtomSdkSearch.reverseGeocoder)

    implementation(libs.androidxDatastore.preferences)
    implementation(libs.androidxCore.ktx)
    implementation(libs.androidxAppcompat)
    implementation(libs.androidxCar.app)
    implementation(libs.androidxCar.appAutomotive)
    implementation(libs.androidxComposeMaterial3)
    implementation(libs.androidxComposeUi)
    implementation(platform(libs.androidxCompose.bom))

    "standardImplementation"(libs.tomtomSdkMaps.mapDisplayComposeStandard)
    "premiumImplementation"(libs.tomtomSdkMaps.mapDisplayComposePremium)
}
