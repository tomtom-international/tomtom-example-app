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

package com.example.demo.map

import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.tomtom.sdk.map.display.compose.BetaMapComposableApi
import com.tomtom.sdk.map.display.compose.state.MapStyleState
import com.tomtom.sdk.map.display.style.StyleDescriptor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.ConnectException
import kotlin.coroutines.cancellation.CancellationException

@OptIn(BetaMapComposableApi::class)
class CustomStyleViewModel(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private var lastAppliedStyle: String? = null

    @Suppress("detekt:TooGenericExceptionCaught")
    fun loadStyleSafely(
        styleState: MapStyleState,
        styleUrl: String?,
        onSuccess: () -> Unit = {},
        onError: (Int) -> Unit = {},
    ) {
        if (styleUrl.isNullOrEmpty() || lastAppliedStyle == styleUrl) return

        viewModelScope.launch {
            try {
                withContext(ioDispatcher) {
                    styleState.loadStyle(StyleDescriptor(uri = styleUrl.toUri()))
                    lastAppliedStyle = styleUrl
                }
                onSuccess()
            } catch (_: CancellationException) {
                return@launch
            } catch (exception: Exception) {
                val message: Int = when (exception) {
                    is IllegalArgumentException -> R.string.custom_style_map_illegal_argument_error
                    is ConnectException -> R.string.custom_style_map_connection_exception
                    is IllegalStateException -> R.string.custom_style_map_invalid_uri_exception
                    else -> R.string.custom_style_map_uknown_error
                }
                Log.e(TAG, exception.message, exception)
                onError(message)
            }
        }
    }

    companion object {
        private const val TAG = "CustomStyleViewModel"
    }
}
