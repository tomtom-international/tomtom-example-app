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

package com.example.application

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.R
import com.example.application.map.MapScreen
import com.example.application.settings.SettingsActivity
import com.example.application.settings.data.LocalSettingsRepository
import com.example.application.ui.theme.NavSdkExampleTheme
import com.example.dataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Activity that hosts the full map + guidance experience.
 * Requests location permission and shows MapScreen when granted.
 */
class NavigationActivity : ComponentActivity() {
    private val locationRequestGrantedFlow = MutableStateFlow<Boolean?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar?.hide()
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            NavSdkExampleTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.navigationBars,
                ) { innerPadding ->
                    MapScreen(
                        settingsRepository = LocalSettingsRepository(dataStore),
                        modifier = Modifier.padding(innerPadding),
                        onCheckLocationPermission = onCheckLocationPermission,
                        locationRequestGrantedFlow = locationRequestGrantedFlow.asStateFlow(),
                        onSettingsClick = { startActivity(Intent(this, SettingsActivity::class.java)) },
                    )
                }
            }
        }
    }

    private val locationRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions[ACCESS_FINE_LOCATION] == true || permissions[ACCESS_COARSE_LOCATION] == true
            locationRequestGrantedFlow.tryEmit(granted)

            if (!granted) {
                Toast.makeText(
                    this,
                    R.string.common_error_location_permission_denied,
                    Toast.LENGTH_SHORT,
                ).show()
                finish()
            }
        }

    private val onCheckLocationPermission = {
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                applicationContext,
                ACCESS_COARSE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            true
        } else {
            locationRequestLauncher.launch(arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION))
            false
        }
    }
}
