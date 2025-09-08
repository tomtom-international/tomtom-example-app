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

package com.example.automotive.vehicle

import android.content.Context
import android.util.Log
import com.tomtom.sdk.vehicle.provider.VehicleProvider

/**
 * Repository that retrieves vehicle data from Android Car API and configures the TomTom SDK.
 */
class VehicleRepository(private val context: Context) {
    fun configureVehicle() {
        val vehicle = AndroidCarVehicleProvider(context).getVehicle()
        if (vehicle != null) {
            VehicleProvider.vehicle = vehicle
            Log.d(TAG, "EV Vehicle configured: $vehicle")
        } else {
            Log.d(TAG, "Vehicle not available from Android Car API")
        }
    }

    companion object {
        private const val TAG = "VehicleRepository"
    }
}
