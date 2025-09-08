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

package com.example.application.tts

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TextToSpeechEngineTest {
    private val testMessage = "Test message"
    private val testErrorCode = 123

    private lateinit var textToSpeechEngine: TextToSpeechEngine

    @Before
    fun setup() {
        textToSpeechEngine = mockk(relaxed = true)

        every { textToSpeechEngine.playMessage(any(), any(), any(), any()) } answers {
            val onStart = secondArg<() -> Unit>()
            val onDone = thirdArg<() -> Unit>()

            onStart()
            onDone()
        }

        every { textToSpeechEngine.shutdown() } answers { }
    }

    @Test
    fun testPlayMessageCallsCallbacks() {
        var startCalled = false
        var doneCalled = false

        textToSpeechEngine.playMessage(
            message = testMessage,
            onStart = { startCalled = true },
            onDone = { doneCalled = true },
        )

        assertEquals(true, startCalled)
        assertEquals(true, doneCalled)
    }

    @Test
    fun testShutdownIsCalled() {
        textToSpeechEngine.shutdown()
        verify { textToSpeechEngine.shutdown() }
    }

    @Test
    fun testPlayMessageHandlesError() {
        every { textToSpeechEngine.playMessage(any(), any(), any(), any()) } answers {
            val onError = arg<(String) -> Unit>(3)
            onError("TTS error occurred with code: $testErrorCode")
        }

        var startCalled = false
        var doneCalled = false
        var errorMessage = ""

        textToSpeechEngine.playMessage(
            message = testMessage,
            onStart = { startCalled = true },
            onDone = { doneCalled = true },
            onError = { errorMessage = it },
        )

        assertEquals("TTS error occurred with code: $testErrorCode", errorMessage)
        assertEquals(false, startCalled)
        assertEquals(false, doneCalled)
    }
}
