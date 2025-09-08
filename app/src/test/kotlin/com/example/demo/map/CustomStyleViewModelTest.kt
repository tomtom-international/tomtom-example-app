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

import android.net.Uri
import android.util.Log
import com.example.R
import com.tomtom.sdk.map.display.compose.BetaMapComposableApi
import com.tomtom.sdk.map.display.compose.state.MapStyleState
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class, BetaMapComposableApi::class)
class CustomStyleViewModelTest {
    @get:Rule
    val mockkRule = MockKRule(this)

    private lateinit var viewModel: CustomStyleViewModel
    private lateinit var styleState: MapStyleState

    @RelaxedMockK
    lateinit var onErrorMock: (Int) -> Unit

    @RelaxedMockK
    lateinit var onSuccessMock: () -> Unit

    @Before
    fun setUp() {
        val dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)

        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } returns 0

        mockkStatic(Uri::class)
        every { Uri.parse(any()) } returns mockk(relaxed = true)

        viewModel = CustomStyleViewModel(ioDispatcher = dispatcher)
        styleState = mockk(relaxUnitFun = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Log::class)
    }

    @Test
    fun `given null or empty URL, loadStyle is never called`() = runTest {
        viewModel.loadStyleSafely(
            styleState = styleState,
            styleUrl = null,
            onSuccess = onSuccessMock,
            onError = onErrorMock,
        )
        viewModel.loadStyleSafely(
            styleState = styleState,
            styleUrl = "",
            onSuccess = onSuccessMock,
            onError = onErrorMock,
        )

        verify { styleState wasNot Called }
    }

    @Test
    fun `given same URL twice, style is loaded only once`() = runTest {
        val url = "https://example.com/style.json"

        viewModel.loadStyleSafely(
            styleState = styleState,
            styleUrl = url,
            onSuccess = onSuccessMock,
            onError = onErrorMock,
        )
        advanceUntilIdle()

        // Second call with the same URL
        viewModel.loadStyleSafely(
            styleState = styleState,
            styleUrl = url,
            onSuccess = onSuccessMock,
            onError = onErrorMock,
        )
        advanceUntilIdle()

        coVerify(exactly = 1) {
            styleState.loadStyle(any())
        }
    }

    @Test
    fun `given valid URL, style loads successfully`() = runTest {
        val url = "https://example.com/style.json"

        viewModel.loadStyleSafely(
            styleState = styleState,
            styleUrl = url,
            onSuccess = onSuccessMock,
            onError = onErrorMock,
        )
        advanceUntilIdle()

        coVerify(exactly = 1) { styleState.loadStyle(any()) }
    }

    @Test
    fun `onSuccess callback is called when style loads successfully`() = runTest {
        val url = "https://example.com/style.json"

        viewModel.loadStyleSafely(
            styleState = styleState,
            styleUrl = url,
            onSuccess = onSuccessMock,
            onError = onErrorMock,
        )
        advanceUntilIdle()

        coVerify(exactly = 1) { styleState.loadStyle(any()) }
        verify { onSuccessMock.invoke() }
    }

    @Test
    fun `illegal argument triggers correct error message`() = runTest {
        val url = "https://bad.url/style.json"
        coEvery { styleState.loadStyle(any()) } throws IllegalArgumentException("Illegal")

        viewModel.loadStyleSafely(
            styleState = styleState,
            styleUrl = url,
            onSuccess = onSuccessMock,
            onError = onErrorMock,
        )
        advanceUntilIdle()

        verify { onErrorMock.invoke(R.string.custom_style_map_illegal_argument_error) }
    }

    @Test
    fun `connection exception triggers correct error message`() = runTest {
        val url = "https://offline/style.json"
        coEvery { styleState.loadStyle(any()) } throws java.net.ConnectException("offline")

        viewModel.loadStyleSafely(
            styleState = styleState,
            styleUrl = url,
            onSuccess = onSuccessMock,
            onError = onErrorMock,
        )
        advanceUntilIdle()

        verify { onErrorMock.invoke(R.string.custom_style_map_connection_exception) }
    }

    @Test
    fun `invalid URI triggers correct error message`() = runTest {
        val url = "notaUri"
        coEvery { styleState.loadStyle(any()) } throws IllegalStateException("invalid uri")

        viewModel.loadStyleSafely(
            styleState = styleState,
            styleUrl = url,
            onSuccess = onSuccessMock,
            onError = onErrorMock,
        )
        advanceUntilIdle()

        verify { onErrorMock.invoke(R.string.custom_style_map_invalid_uri_exception) }
    }

    @Test
    fun `unknown exception triggers generic error message`() = runTest {
        val url = "https://example.com/style.json"
        coEvery { styleState.loadStyle(any()) } throws RuntimeException("unknown")

        viewModel.loadStyleSafely(
            styleState = styleState,
            styleUrl = url,
            onSuccess = onSuccessMock,
            onError = onErrorMock,
        )
        advanceUntilIdle()

        verify { onErrorMock.invoke(R.string.custom_style_map_uknown_error) }
    }
}
