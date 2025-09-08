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

import android.content.Context
import android.media.AudioAttributes
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import java.util.UUID

/**
 * A simple Text-to-Speech engine that uses Android's built-in TTS capabilities.
 * Designed to be used with Jetpack Compose.
 */
class TextToSpeechEngine(
    context: Context,
    private val initialLanguage: Locale = Locale.getDefault(),
) {
    private val tts: TextToSpeech

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                initTts()
            }
        }
    }

    private fun initTts() {
        tts.setLanguage(initialLanguage)
        tts.setAudioAttributes(createAudioAttributes())
    }

    fun playMessage(
        message: String,
        onStart: () -> Unit = {},
        onDone: () -> Unit = {},
        onError: (String) -> Unit = {},
    ) {
        val utteranceId = UUID.randomUUID().toString()

        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String) {
                onStart()
            }

            override fun onDone(utteranceId: String) {
                onDone()
            }

            @Deprecated("Deprecated in Java", ReplaceWith("onError(\"TTS error occurred\")"))
            override fun onError(utteranceId: String) {
                onError("TTS error occurred")
            }

            override fun onError(
                utteranceId: String,
                errorCode: Int,
            ) {
                onError("TTS error occurred with code: $errorCode")
            }
        })

        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)

        tts.speak(message, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }

    private fun createAudioAttributes() = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        .build()
}
