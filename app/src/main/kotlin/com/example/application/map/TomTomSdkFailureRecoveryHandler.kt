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

package com.example.application.map

import android.content.Context
import android.util.Log
import androidx.annotation.WorkerThread
import com.tomtom.sdk.common.Callback
import com.tomtom.sdk.datamanagement.nds.update.MapUpdateError
import com.tomtom.sdk.datamanagement.regionstore.RegionStore
import com.tomtom.sdk.init.TomTomSdkFailure
import java.io.File
import java.io.InputStream
import java.util.concurrent.atomic.AtomicBoolean
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * Handles recovery from [com.tomtom.sdk.init.TomTomSdkFailure]s emitted by [com.tomtom.sdk.init.TomTomSdk.failures].
 *
 * - [com.tomtom.sdk.init.TomTomSdkFailure.CannotReadMap] — re-extracts the map asset to a temporary directory and
 *   recovers by replacing the onboard map using [com.tomtom.sdk.datamanagement.regionstore.RegionStore.replaceMap].
 * - [com.tomtom.sdk.init.TomTomSdkFailure.CannotReadKeyStore] — re-extracts the keystore asset to a temporary directory
 *   and recovers by replacing the keystore using
 *   [com.tomtom.sdk.datamanagement.regionstore.RegionStore.replaceKeystore].
 */
object TomTomSdkFailureRecoveryHandler {
    private const val TAG = "TomTomSdkFailureRecoveryHandler"

    private val isRecovering = AtomicBoolean(false)

    fun handleFailure(
        failure: TomTomSdkFailure,
        context: Context,
        mapAsset: String,
        keystoreAsset: String,
    ) {
        if (!isRecovering.compareAndSet(false, true)) {
            Log.i(TAG, "Recovery already in progress or completed successfully. Ignoring failure: $failure")
            return
        }
        when (failure) {
            is TomTomSdkFailure.CannotReadMap -> {
                val tempDir = extractMapAsset(context, mapAsset)
                replaceMap(failure.regionStore, tempDir)
            }
            is TomTomSdkFailure.CannotReadKeyStore -> {
                val tempDir = extractKeystoreAsset(context, keystoreAsset)
                val keystoreFile = tempDir.listFiles()?.firstOrNull()
                    ?: error("No keystore file found after extraction in $tempDir")
                replaceKeystore(failure.regionStore, keystoreFile, tempDir)
            }
        }
    }

    @WorkerThread
    private fun extractMapAsset(
        context: Context,
        mapAsset: String,
    ): File {
        val tempDir = createTempRecoveryDir(context, "map_recovery")
        Log.i(TAG, "Extracting map asset '$mapAsset' to $tempDir")
        context.assets.open(mapAsset).use { unzipInputStream(it, tempDir) }
        return tempDir
    }

    @WorkerThread
    private fun extractKeystoreAsset(
        context: Context,
        keystoreAsset: String,
    ): File {
        val tempDir = createTempRecoveryDir(context, "keystore_recovery")
        Log.i(TAG, "Extracting keystore asset '$keystoreAsset' to $tempDir")
        context.assets.open(keystoreAsset).use { unzipInputStream(it, tempDir) }
        return tempDir
    }

    private fun createTempRecoveryDir(
        context: Context,
        prefix: String,
    ): File = File(context.cacheDir, "$prefix-${System.currentTimeMillis()}").also { it.mkdirs() }

    private fun unzipInputStream(
        stream: InputStream,
        targetDir: File,
    ) {
        ZipInputStream(stream).use { zipInputStream ->
            var entry = zipInputStream.nextEntry
            while (entry != null) {
                zipInputStream.extractToDirectory(entry, targetDir)
                entry = zipInputStream.nextEntry
            }
            zipInputStream.closeEntry()
        }
    }

    private fun ZipInputStream.extractToDirectory(
        entry: ZipEntry,
        dir: File,
    ) {
        val file = File(dir, entry.name)
        val canonicalDirPath = dir.canonicalPath
        val canonicalFilePath = file.canonicalPath

        // Zip Slip protection: ensure file is within target dir
        if (!canonicalFilePath.startsWith("$canonicalDirPath${File.separator}")) {
            throw SecurityException("Zip entry is outside of the target dir: ${entry.name}")
        }

        if (entry.isDirectory) {
            file.mkdirs()
        } else {
            file.parentFile?.mkdirs()
            file.outputStream().use { this.copyTo(it) }
        }
    }

    private fun replaceMap(
        regionStore: RegionStore,
        tempDir: File,
    ) {
        Log.i(TAG, "Recovering from CannotReadMap. Replacing map from $tempDir")
        regionStore.replaceMap(
            newMapDirectoryPath = tempDir,
            newMapFileList = null,
            callback = object : Callback<Unit, MapUpdateError> {
                override fun onSuccess(result: Unit) {
                    Log.i(TAG, "Map replacement completed successfully")
                    tempDir.deleteRecursively()
                }

                override fun onFailure(failure: MapUpdateError) {
                    Log.e(TAG, "Map replacement failed: $failure")
                    tempDir.deleteRecursively()
                    isRecovering.set(false)
                }
            },
            progressReporter = { progress -> Log.d(TAG, "Map replacement progress: $progress%") },
        )
    }

    private fun replaceKeystore(
        regionStore: RegionStore,
        keystoreFile: File,
        tempDir: File,
    ) {
        Log.i(TAG, "Recovering from CannotReadKeyStore. Replacing keystore from $keystoreFile")
        regionStore.replaceKeystore(
            newKeystoreFilePath = keystoreFile,
            keystoreChecksum = "",
            callback = object : Callback<Unit, MapUpdateError> {
                override fun onSuccess(result: Unit) {
                    Log.i(TAG, "Keystore replacement completed successfully")
                    tempDir.deleteRecursively()
                }

                override fun onFailure(failure: MapUpdateError) {
                    Log.e(TAG, "Keystore replacement failed: $failure")
                    tempDir.deleteRecursively()
                    isRecovering.set(false)
                }
            },
        )
    }
}
