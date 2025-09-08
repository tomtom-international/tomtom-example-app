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

import com.android.build.gradle.internal.api.ApkVariantOutputImpl

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.style.ktlint)
    alias(libs.plugins.style.detekt)
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
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        missingDimensionStrategy("tomtom-sdk-version", "complete")

        buildConfigField("String", "TOMTOM_API_KEY", "\"$tomtomApiKey\"")
        buildConfigField(
            "String",
            "TOMTOM_NAV_SDK",
            "\"${libs.versions.tomtomNavsdk.get()}\"",
        )
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

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
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
    implementation(libs.androidxCore.ktx)
    implementation(libs.androidxDatastore.preferences)
    implementation(libs.androidxLifecycle.viewmodelCompose)
    implementation(libs.androidxNavigation.compose)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.serializationJson)
    implementation(libs.googleAndroidMaterial)
    implementation(libs.tomtomEntrypoint)
    implementation(libs.tomtomLocation.providerMapMatched)
    implementation(libs.tomtomLocation.providerSimulation)
    implementation(libs.tomtomLocation.tracesLogger)
    implementation(libs.tomtomMaps.mapDisplayStandard)
    implementation(libs.tomtomMaps.mapDisplayComposeCommon)
    implementation(libs.tomtomMapsVis.navigationCompose)
    implementation(libs.tomtomMapsVis.routingCompose)
    implementation(libs.tomtomMaps.mapDataStore)
    implementation(libs.tomtomTraffic.client)
    implementation(platform(libs.androidxCompose.bom))

    debugImplementation(libs.androidxComposeUi.testManifest)
    debugImplementation(libs.androidxComposeUi.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutinesTest)
}
