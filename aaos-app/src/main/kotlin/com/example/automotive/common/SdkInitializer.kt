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

package com.example.automotive.common

import android.content.Context
import android.util.Log
import com.example.automotive.BuildConfig
import com.example.automotive.map.OnboardMapAssetsExtractor
import com.tomtom.sdk.common.configuration.buildSdkConfiguration
import com.tomtom.sdk.datamanagement.nds.update.BetaNdsStoreUpdateApi
import com.tomtom.sdk.datamanagement.nds.update.NdsStoreAutomaticUpdatesConfiguration
import com.tomtom.sdk.init.TomTomSdk
import com.tomtom.sdk.telemetry.UserConsent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Handles TomTom SDK initialization including map extraction and configuration.
 *
 * @param context Application context
 * @param ioDispatcher Dispatcher for IO operations, defaults to Dispatchers.IO
 */
class SdkInitializer(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val onboardMapPath by lazy { context.filesDir.resolve(RELATIVE_ONBOARD_MAP_PATH) }

    private val mapDir by lazy {
        val defaultMapDir = onboardMapPath.resolve(RELATIVE_NDS_STORE_PATH)
        val alternativeMapDir = onboardMapPath.resolve(ALTERNATIVE_RELATIVE_NDS_STORE_PATH)
        if (!defaultMapDir.isValidMapDir() && alternativeMapDir.isValidMapDir()) {
            alternativeMapDir
        } else {
            defaultMapDir
        }
    }

    private val ndsKeyStorePath by lazy { onboardMapPath.resolve(RELATIVE_KEYSTORE_PATH) }

    private fun File.isValidMapDir() = this.isDirectory && this.resolve(RELATIVE_ROOT_NDS_PATH).isFile

    /**
     * Initializes the TomTom SDK asynchronously.
     * This includes enabling required features, extracting maps if needed, and configuring the SDK.
     *
     * @param telemetryConsent Suspend lambda returning the user's [UserConsent] choice
     */
    suspend fun initializeSdk(telemetryConsent: suspend () -> UserConsent) {
        extractMapsIfNeeded()

        initializeTomTomSdk(telemetryConsent)
        Log.i(TAG, "SDK initialization complete")
    }

    private suspend fun extractMapsIfNeeded() {
        withContext(ioDispatcher) {
            if (!regionStoreExists()) {
                Log.i(TAG, "Extracting sample maps from assets...")
                OnboardMapAssetsExtractor.extractMapAssets(
                    context = context,
                    targetMapDir = mapDir,
                    targetKeystorePath = ndsKeyStorePath,
                    forceExtraction = false,
                    mapAsset = MAP_ASSET_PATH,
                    keyStoreAsset = KEYSTORE_ASSET_PATH,
                )
                Log.i(TAG, "Successfully extracted sample maps")
            } else {
                Log.i(TAG, "Region store already exists, skipping extraction")
            }
        }
    }

    private fun regionStoreExists() = mapDir.isValidMapDir()

    @OptIn(BetaNdsStoreUpdateApi::class)
    private fun initializeTomTomSdk(telemetryConsent: suspend () -> UserConsent) {
        if (TomTomSdk.isInitialized) {
            Log.i(TAG, "SDK already initialized, skipping initialization")
            return
        }
        TomTomSdk.initialize(
            context,
            buildSdkConfiguration(
                context = context,
                apiKey = BuildConfig.TOMTOM_API_KEY,
                regionStorePath = mapDir,
                telemetryUserConsent = telemetryConsent,
                regionStoreConfiguration = {
                    keyStorePath = ndsKeyStorePath
                    updateConfiguration = {
                        automaticUpdates = {
                            allRegions = NdsStoreAutomaticUpdatesConfiguration.AllRegions()
                        }
                    }
                },
            ),
        )
    }

    private companion object {
        const val TAG = "SdkInitializer"
        private const val MAP_ASSET_PATH = "sample-map/map.zip"
        private const val KEYSTORE_ASSET_PATH = "sample-map/keystore.zip"
        private const val RELATIVE_ONBOARD_MAP_PATH = "offline-orbis"
        private const val RELATIVE_NDS_STORE_PATH = "DATA"
        private const val ALTERNATIVE_RELATIVE_NDS_STORE_PATH = "map"
        private const val RELATIVE_ROOT_NDS_PATH = "ROOT.NDS"
        private const val RELATIVE_KEYSTORE_PATH = "keystore.sqlite"
    }
}
