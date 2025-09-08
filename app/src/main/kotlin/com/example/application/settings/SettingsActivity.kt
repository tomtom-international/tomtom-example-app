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

package com.example.application.settings

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.MainActivity
import com.example.application.settings.data.LocalSettingsRepository
import com.example.application.settings.ui.SettingsScreen
import com.example.application.ui.theme.NavSdkExampleTheme
import com.example.dataStore
import kotlin.system.exitProcess

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar?.hide()

        setContent {
            NavSdkExampleTheme {
                SettingsScreen(
                    settingsRepository = LocalSettingsRepository(dataStore),
                    onNavigateBack = { finish() },
                    onRestartApp = {
                        val intent = Intent(this, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                        exitProcess(0)
                    },
                )
            }
        }
    }
}
