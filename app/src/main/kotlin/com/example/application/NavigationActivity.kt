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
