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

import android.app.Presentation
import android.content.Context
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.util.Log
import android.view.WindowManager
import androidx.car.app.SurfaceContainer
import androidx.compose.ui.platform.ComposeView

/**
 * Manages VirtualDisplay and Presentation lifecycle for AAOS surface rendering.
 */
class ComposePresentationManager(private val context: Context) {
    private var virtualDisplay: VirtualDisplay? = null
    private var presentation: Presentation? = null

    /**
     * Creates VirtualDisplay and Presentation for the given surface.
     *
     * @param surfaceContainer Container providing the surface and display metrics
     * @param composeView The Compose view to display in the Presentation
     */
    fun create(
        surfaceContainer: SurfaceContainer,
        composeView: ComposeView,
    ) {
        virtualDisplay = createVirtualDisplay(surfaceContainer)
        presentation = virtualDisplay?.let { Presentation(context, it.display) }

        presentation?.setContentView(composeView)

        try {
            presentation?.show()
            Log.d(TAG, "Presentation shown on virtual display: ${virtualDisplay?.display}")
        } catch (exception: WindowManager.BadTokenException) {
            Log.e(TAG, "Failed to show Presentation due to bad window token", exception)
        } catch (exception: IllegalStateException) {
            Log.e(TAG, "Failed to show Presentation due to illegal state", exception)
        }
    }

    fun destroy() {
        presentation?.dismiss()
        virtualDisplay?.release()
        presentation = null
        virtualDisplay = null
    }

    private fun createVirtualDisplay(surfaceContainer: SurfaceContainer): VirtualDisplay? {
        val displayManager = context.getSystemService(DisplayManager::class.java)
        return displayManager.createVirtualDisplay(
            VIRTUAL_DISPLAY_NAME,
            surfaceContainer.width,
            surfaceContainer.height,
            surfaceContainer.dpi,
            surfaceContainer.surface,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION or
                DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY,
        )
    }

    private companion object {
        const val TAG = "ComposePresentationMgr"
        const val VIRTUAL_DISPLAY_NAME = "TomTomMapVirtualDisplay"
    }
}
