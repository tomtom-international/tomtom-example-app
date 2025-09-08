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

package com.example

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.example.application.common.ISO3_GBR
import com.example.application.common.ISO3_USA
import com.tomtom.sdk.annotations.AlphaSdkInitializationApi
import com.tomtom.sdk.common.configuration.SdkOptions.OnlineOnly
import com.tomtom.sdk.common.measures.UnitSystem
import com.tomtom.sdk.entrypoint.TomTomSdk
import com.tomtom.sdk.featuretoggle.BetaRedesignedTelemetryApi
import com.tomtom.sdk.navigation.UnitSystemType
import com.tomtom.sdk.telemetry.UserConsent
import java.util.Locale

val Context.dataStore by preferencesDataStore(name = "user_preferences")

class NavSdkExampleApplication : Application() {
    @OptIn(
        AlphaSdkInitializationApi::class,
        BetaRedesignedTelemetryApi::class,
    )
    override fun onCreate() {
        super.onCreate()

        TomTomSdk.initialize(
            context = this,
            sdkOptions = OnlineOnly(
                apiKey = BuildConfig.TOMTOM_API_KEY,
                telemetryUserConsent = { UserConsent.TelemetryOn },
            ),
        )

        TomTomSdk.navigation.unitSystem = when (Locale.getDefault().isO3Country) {
            ISO3_USA -> UnitSystemType.Fixed(UnitSystem.US)
            ISO3_GBR -> UnitSystemType.Fixed(UnitSystem.UK)
            else -> UnitSystemType.Fixed(UnitSystem.Metric)
        }
    }
}
