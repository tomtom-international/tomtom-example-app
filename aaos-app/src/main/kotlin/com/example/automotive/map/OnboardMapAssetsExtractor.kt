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

package com.example.automotive.map

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
     * Extracting the onboard map from assets, if the target location already exists then skip.
     * NOTE: The caller should handle the Exception
     * NOTE: This function should not be executed in the main thread
     * @param context Android Context
     * @param targetMapDir the target location of the map
     * @param targetKeystorePath the target location the keystore file
     * @param forceExtraction if false, skip the extraction if the target location exists, otherwise
     * clean the target location and then do extraction
     *
     */
    @WorkerThread
    @Suppress("detekt:LongParameterList")
    fun extractMapAssets(
        context: Context,
        targetMapDir: File,
        targetKeystorePath: File,
        forceExtraction: Boolean,
        mapAsset: String,
        keyStoreAsset: String,
    ) {
        if (forceExtraction) {
            deleteFileOrDirectory(targetMapDir)
            deleteFileOrDirectory(targetKeystorePath)
        }

        val rootNdsFile = File(targetMapDir, "ROOT.NDS")
        if (!rootNdsFile.exists()) {
            Log.i(TAG, "Extracting map to ${targetMapDir.path}")
            targetMapDir.mkdirs()
            context.assets.open(mapAsset).also {
                unzipInputStream(it, targetMapDir)
            }
        } else {
            Log.i(TAG, "Skip extracting map - ROOT.NDS already exists")
        }

        if (!targetKeystorePath.exists()) {
            Log.i(TAG, "Extracting keystore to ${targetKeystorePath.path}")
            context.assets.open(keyStoreAsset).also {
                unzipInputStream(it, targetKeystorePath.parentFile!!)
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
            target.delete()
        } else if (target.isDirectory) {
            target.deleteRecursively()
        }
    }

    private fun ZipInputStream.extractToDirectory(
        entry: ZipEntry,
        dir: File,
    ) {
        val file = File(dir, entry.name)
        val canonicalDirPath = dir.canonicalPath
        val canonicalFilePath = file.canonicalPath

        if (!canonicalFilePath.startsWith("$canonicalDirPath${File.separator}")) {
            throw SecurityException("Zip entry is outside of the target dir: ${entry.name}")
        }

        val isActualDirectory = entry.isDirectory && entry.name.endsWith("/")

        if (isActualDirectory) {
            file.mkdirs()
        } else {
            file.parentFile?.mkdirs()
            file.outputStream().use {
                this.copyTo(it)
            }
        }
    }
}
