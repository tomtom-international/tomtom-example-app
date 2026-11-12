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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Retrieves EV battery information from Android Automotive [CarPropertyManager] and maps it to the
 * TomTom [Vehicle] model.
 *
 * @param context Context used to access the Android Car API.
 * @param ioDispatcher Dispatcher for the blocking Car connection and property reads.
 */
class AndroidCarVehicleProvider(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    /**
     * Reads the current EV [Vehicle] from the Android Car API, or returns `null` if the car, its
     * permissions, or the EV data aren't available.
     *
     * The blocking connection and property reads run on [ioDispatcher], and the connection is
     * always released before returning.
     */
    suspend fun currentVehicle(): Vehicle? = withContext(ioDispatcher) {
        val car = connectToCar() ?: return@withContext null
        try {
            car.propertyManager?.let(::readVehicle)
        } finally {
            runCatching { car.disconnect() }
        }
    }

    /**
     * Connects to the car, or returns `null` if permissions are missing or the car can't connect.
     * An unusable connection is released before returning `null`.
     */
    private fun connectToCar(): Car? {
        if (!hasPermission(CAR_INFO_PERMISSION) || !hasPermission(CAR_ENERGY_PERMISSION)) {
            Log.i(TAG, "Permissions missing: INFO=$CAR_INFO_PERMISSION, ENERGY=$CAR_ENERGY_PERMISSION")
            return null
        }

        val car = runCatching { Car.createCar(context) }.getOrElse {
            Log.e(TAG, "Could not connect to the Car API", it)
            null
        }
        return when {
            car == null -> null
            car.isConnected -> car
            else -> {
                Log.w(TAG, "Car not connected.")
                runCatching { car.disconnect() }
                null
            }
        }
    }

    /** The [CarPropertyManager] of this car, or `null` if the property service is unavailable. */
    private val Car.propertyManager: CarPropertyManager?
        get() = runCatching { getCarManager(Car.PROPERTY_SERVICE) as? CarPropertyManager }.getOrNull()

    private fun readVehicle(manager: CarPropertyManager): Vehicle? {
        val capacity = manager.readDouble(VehiclePropertyIds.EV_CURRENT_BATTERY_CAPACITY)
            ?: manager.readDouble(VehiclePropertyIds.INFO_EV_BATTERY_CAPACITY)
        val level = manager.readDouble(VehiclePropertyIds.EV_BATTERY_LEVEL)

        return if (capacity != null && level != null) {
            mapToVehicle(capacity, level)
        } else {
            Log.w(TAG, "EV properties unavailable (capacity=$capacity, level=$level).")
            null
        }
    }

    /** Maps raw battery data to the [Vehicle] object. */
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

    companion object {
        private const val TAG = "AndroidCarVehicleProvider"
        private const val DEFAULT_AREA_ID = 0
        private const val CAR_INFO_PERMISSION = "android.car.permission.CAR_INFO"
        private const val CAR_ENERGY_PERMISSION = "android.car.permission.CAR_ENERGY"
        private const val WATT_HOURS_PER_KWH = 1000.0
    }
}
