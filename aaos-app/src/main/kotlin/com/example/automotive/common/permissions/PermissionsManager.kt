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
        val permissionsToRequest = mutableListOf<String>()

        if (!hasPermission(CAR_INFO_PERMISSION)) {
            permissionsToRequest.add(CAR_INFO_PERMISSION)
        }
        if (!hasPermission(CAR_ENERGY_PERMISSION)) {
            permissionsToRequest.add(CAR_ENERGY_PERMISSION)
        }
        if (!hasPermission(FINE_LOCATION_PERMISSION)) {
            permissionsToRequest.add(FINE_LOCATION_PERMISSION)
        }
        if (!hasPermission(COARSE_LOCATION_PERMISSION)) {
            permissionsToRequest.add(COARSE_LOCATION_PERMISSION)
        }

        if (permissionsToRequest.isNotEmpty()) {
            Log.i(TAG, "Requesting permissions: $permissionsToRequest")
            carContext.requestPermissions(permissionsToRequest) { approved, rejected ->
                handlePermissionsResult(approved, rejected, onPermissionsGranted)
            }
        } else {
            Log.d(TAG, "All permissions already granted")
            onPermissionsGranted()
        }
    }

    private fun hasAllPermissions(): Boolean =
        hasPermission(CAR_INFO_PERMISSION) && hasPermission(CAR_ENERGY_PERMISSION) &&
            (hasPermission(FINE_LOCATION_PERMISSION) || hasPermission(COARSE_LOCATION_PERMISSION))

    private fun hasPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(carContext, permission) == PackageManager.PERMISSION_GRANTED

    private fun handlePermissionsResult(
        approved: List<String>,
        rejected: List<String>,
        onPermissionsGranted: () -> Unit,
    ) {
        if (approved.isNotEmpty()) {
            Log.i(TAG, "Permissions approved: $approved")
        }
        if (rejected.isNotEmpty()) {
            Log.w(TAG, "Permissions rejected: $rejected")
        }

        if (hasAllPermissions()) {
            onPermissionsGranted()
        } else {
            Log.i(TAG, "Cannot proceed: Required permissions not fully granted")
        }
    }

    companion object {
        private const val TAG = "PermissionsManager"
        private const val CAR_INFO_PERMISSION = "android.car.permission.CAR_INFO"
        private const val CAR_ENERGY_PERMISSION = "android.car.permission.CAR_ENERGY"
        private const val FINE_LOCATION_PERMISSION = "android.permission.ACCESS_FINE_LOCATION"
        private const val COARSE_LOCATION_PERMISSION = "android.permission.ACCESS_COARSE_LOCATION"
    }
}
