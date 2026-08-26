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

package com.example.automotive.common.permissions

import android.content.pm.PackageManager
import android.util.Log
import androidx.car.app.CarContext
import androidx.core.content.ContextCompat

/**
 * Manages runtime permission requests for Car API (CAR_INFO, CAR_ENERGY) and location access.
 */
class PermissionsManager(private val carContext: CarContext) {
    fun checkAndRequestPermissions(onPermissionsGranted: () -> Unit) {
        val permissionsToRequest = getMissingPermissions()

        if (permissionsToRequest.isEmpty()) {
            onPermissionsGranted()
            return
        }

        Log.i(TAG, "Requesting permissions: $permissionsToRequest")
        try {
            carContext.requestPermissions(permissionsToRequest) { approved, rejected ->
                handlePermissionResult(approved, rejected, onPermissionsGranted)
            }
        } catch (e: IllegalStateException) {
            Log.e(TAG, "requestPermissions failed: $e")
        }
    }

    private fun getMissingPermissions(): List<String> = ALL_PERMISSIONS.filterNot { hasPermission(it) }

    private fun handlePermissionResult(
        approved: List<String>,
        rejected: List<String>,
        onPermissionsGranted: () -> Unit,
    ) {
        if (approved.isNotEmpty()) Log.i(TAG, "Approved: $approved")
        if (rejected.isNotEmpty()) Log.w(TAG, "Rejected: $rejected")
        if (hasPermission(FINE_LOCATION_PERMISSION)) {
            onPermissionsGranted()
        } else {
            Log.w(TAG, "ACCESS_FINE_LOCATION not granted — cannot proceed")
        }
    }

    private fun hasPermission(permission: String): Boolean = try {
        ContextCompat.checkSelfPermission(carContext, permission) == PackageManager.PERMISSION_GRANTED
    } catch (e: IllegalArgumentException) {
        Log.w(TAG, "Invalid permission string '$permission': $e")
        false
    }

    companion object {
        private const val TAG = "PermissionsManager"
        private const val CAR_INFO_PERMISSION = "android.car.permission.CAR_INFO"
        private const val CAR_ENERGY_PERMISSION = "android.car.permission.CAR_ENERGY"
        private const val FINE_LOCATION_PERMISSION = "android.permission.ACCESS_FINE_LOCATION"
        private const val COARSE_LOCATION_PERMISSION = "android.permission.ACCESS_COARSE_LOCATION"

        private val ALL_PERMISSIONS = listOf(
            CAR_INFO_PERMISSION,
            CAR_ENERGY_PERMISSION,
            FINE_LOCATION_PERMISSION,
            COARSE_LOCATION_PERMISSION,
        )
    }
}
