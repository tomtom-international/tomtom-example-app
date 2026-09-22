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
import java.io.File
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object OnboardMapAssetsExtractor {
    private const val TAG = "OnboardMapAssetsExtractor"

    /**
     * Configuration for extracting map and keystore assets.
     *
     * @property targetMapDir the target location of the map
     * @property targetKeystorePath the target location the keystore file
     * @property mapAsset the path to the map asset in the application assets
     * @property keyStoreAsset the path to the keystore asset in the application assets
     */
    data class MapAssetsConfig(
        val targetMapDir: File,
        val targetKeystorePath: File,
        val mapAsset: String,
        val keyStoreAsset: String,
    )

    /**
     * Extracting the onboard map from assets, if the target location already exists then skip.
     * NOTE: The caller should handle the Exception
     * NOTE: This function should not be executed in the main thread
     * @param context Android Context
     * @param config configuration for the extraction
     * @param forceExtraction if false, skip the extraction if the target location exists, otherwise
     * clean the target location and then do extraction
     */
    @WorkerThread
    fun extractMapAssets(
        context: Context,
        config: MapAssetsConfig,
        forceExtraction: Boolean,
    ) {
        if (forceExtraction) {
            deleteFileOrDirectory(config.targetMapDir)
            deleteFileOrDirectory(config.targetKeystorePath)
        }

        if (!config.targetMapDir.exists()) {
            Log.i(TAG, "Extracting map to ${config.targetMapDir.path}")
            context.assets.open(config.mapAsset).also {
                unzipInputStream(it, config.targetMapDir)
            }
        } else {
            Log.i(TAG, "Skip extracting map")
        }

        if (!config.targetKeystorePath.exists()) {
            Log.i(TAG, "Extracting keystore to ${config.targetKeystorePath.path}")
            context.assets.open(config.keyStoreAsset).also {
                unzipInputStream(it, config.targetKeystorePath.parentFile!!)
            }
        } else {
            Log.i(TAG, "Skip extracting keystore")
        }
    }

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

    private fun deleteFileOrDirectory(target: File) {
        if (!target.exists()) {
            return
        }
        Log.i(TAG, "deleting $target")
        if (target.isFile) {
            if (!target.delete()) {
                Log.w(TAG, "Failed to delete file $target")
            }
        } else if (target.isDirectory) {
            if (!target.deleteRecursively()) {
                Log.w(TAG, "Failed to delete directory $target")
            }
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
            file.outputStream().use {
                this.copyTo(it)
            }
        }
    }
}
