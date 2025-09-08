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

package com.example.application.common.tts

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
