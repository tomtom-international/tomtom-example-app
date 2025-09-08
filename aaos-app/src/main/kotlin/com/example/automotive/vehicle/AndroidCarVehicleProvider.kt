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

import android.car.Car
import android.car.VehiclePropertyIds
import android.car.hardware.property.CarPropertyManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.tomtom.sdk.vehicle.Vehicle

/**
 * Retrieves EV battery information from Android Automotive CarPropertyManager
 * and maps it to TomTom Vehicle model.
 */
class AndroidCarVehicleProvider(private val context: Context) {
    @Suppress("detekt:TooGenericExceptionCaught")
    fun getVehicle(): Vehicle? {
        if (!hasPermission(CAR_INFO_PERMISSION) || !hasPermission(CAR_ENERGY_PERMISSION)) {
            Log.i(TAG, "Permissions missing: INFO=$CAR_INFO_PERMISSION, ENERGY=$CAR_ENERGY_PERMISSION")
            return null
        }

        return try {
            Car.createCar(context).useConnection { car ->
                if (!car.isConnected) {
                    Log.i(TAG, "Car not connected.")
                    return@useConnection null
                }

                val propertyManager = car.getCarManager(Car.PROPERTY_SERVICE) as CarPropertyManager
                val capacity = propertyManager.readDouble(VehiclePropertyIds.INFO_EV_BATTERY_CAPACITY)
                val level = propertyManager.readDouble(VehiclePropertyIds.EV_BATTERY_LEVEL)

                if (capacity != null && level != null) {
                    mapToVehicle(capacity, level)
                } else {
                    Log.i(TAG, "EV properties unavailable (capacity=$capacity, level=$level).")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to obtain vehicle info from Car API", e)
            null
        }
    }

    /**
     * Maps raw battery data to the Vehicle object.
     * Extracts business logic from the main flow.
     */
    private fun mapToVehicle(
        capacity: Double,
        level: Double,
    ): Vehicle {
        val capacityKWh = capacity / WATT_HOURS_PER_KWH
        val levelKWh = level / WATT_HOURS_PER_KWH

        Log.d(TAG, "Battery (raw): capacity=${capacityKWh}kWh, level=${levelKWh}kWh")

        return AndroidCarToTomTomVehicleMapper.toVehicleFromCurrentKWh(
            capacityKWh = capacityKWh,
            currentChargeKWh = levelKWh,
        )
    }

    private fun hasPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    private fun CarPropertyManager.readDouble(propertyId: Int): Double? =
        runCatching { getFloatProperty(propertyId, DEFAULT_AREA_ID).toDouble() }.getOrNull()
            ?: runCatching { getIntProperty(propertyId, DEFAULT_AREA_ID).toDouble() }.getOrNull()

    /**
     * Safely handles the Car lifecycle.
     * Mimics Kotlin's .use() for objects that require explicit disconnection.
     */
    private inline fun <R> Car.useConnection(block: (Car) -> R): R {
        try {
            return block(this)
        } finally {
            runCatching { disconnect() }
        }
    }

    companion object {
        private const val TAG = "AndroidCarVehicleProvider"
        private const val DEFAULT_AREA_ID = 0
        private const val CAR_INFO_PERMISSION = "android.car.permission.CAR_INFO"
        private const val CAR_ENERGY_PERMISSION = "android.car.permission.CAR_ENERGY"
        private const val WATT_HOURS_PER_KWH = 1000.0
    }
}
