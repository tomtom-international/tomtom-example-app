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

import android.util.Log
import com.tomtom.quantity.ElectricConsumption
import com.tomtom.quantity.Energy
import com.tomtom.quantity.Force
import com.tomtom.quantity.Power
import com.tomtom.quantity.Speed
import com.tomtom.sdk.vehicle.ChargeLevel
import com.tomtom.sdk.vehicle.ChargingConnector
import com.tomtom.sdk.vehicle.ChargingParameters
import com.tomtom.sdk.vehicle.ConnectorType
import com.tomtom.sdk.vehicle.CurrentType
import com.tomtom.sdk.vehicle.ElectricEngine
import com.tomtom.sdk.vehicle.ElectricVehicleConsumption
import com.tomtom.sdk.vehicle.Vehicle

/**
 * Maps raw EV battery information from Android Car to TomTom SDK Vehicle model.
 */
object AndroidCarToTomTomVehicleMapper {
    private const val TAG = "AndroidCarVehicleMapper"

    private const val MIN_CAPACITY_KWH = 1.0
    private const val MAX_CAPACITY_KWH = 250.0
    private const val DEFAULT_CAPACITY_KWH = 64.0
    private const val MIN_CHARGE_KWH = 0.0

    private const val SPEED_STATIONARY_KPH = 0.0
    private const val SPEED_CITY_KPH = 50.0
    private const val SPEED_SUBURBAN_KPH = 80.0
    private const val SPEED_HIGHWAY_KPH = 120.0

    private const val CONSUMPTION_STATIONARY_KWH_PER_100KM = 13.0
    private const val CONSUMPTION_CITY_KWH_PER_100KM = 14.5
    private const val CONSUMPTION_SUBURBAN_KWH_PER_100KM = 16.5
    private const val CONSUMPTION_HIGHWAY_KWH_PER_100KM = 21.0

    private const val BATTERY_LEVEL_10_PERCENT = 0.10
    private const val BATTERY_LEVEL_50_PERCENT = 0.50
    private const val BATTERY_LEVEL_80_PERCENT = 0.80

    private const val PEAK_POWER_CAPACITY_MULTIPLIER = 2.5
    private const val PEAK_POWER_MIN_KW = 60.0
    private const val PEAK_POWER_MAX_KW = 200.0

    private const val MID_POWER_FRACTION = 0.7
    private const val TAIL_POWER_FRACTION = 0.3

    // Reusable, reasonable defaults required by the Route Planner when EV charge is provided.
    private val DEFAULT_CONSUMPTION: Map<Speed, Force> = mapOf(
        Speed.kilometersPerHour(SPEED_STATIONARY_KPH) to ElectricConsumption.kilowattHoursPer100Kilometers(
            CONSUMPTION_STATIONARY_KWH_PER_100KM,
        ),
        Speed.kilometersPerHour(SPEED_CITY_KPH) to ElectricConsumption.kilowattHoursPer100Kilometers(
            CONSUMPTION_CITY_KWH_PER_100KM,
        ),
        Speed.kilometersPerHour(SPEED_SUBURBAN_KPH) to ElectricConsumption.kilowattHoursPer100Kilometers(
            CONSUMPTION_SUBURBAN_KWH_PER_100KM,
        ),
        Speed.kilometersPerHour(SPEED_HIGHWAY_KPH) to ElectricConsumption.kilowattHoursPer100Kilometers(
            CONSUMPTION_HIGHWAY_KWH_PER_100KM,
        ),
    )

    private val DEFAULT_CONNECTORS: List<ChargingConnector> = listOf(
        ChargingConnector(
            currentType = CurrentType.AcThreePhase,
            plugTypes = listOf(ConnectorType.Iec62196Type2Outlet),
        ),
        ChargingConnector(
            currentType = CurrentType.Dc,
            plugTypes = listOf(ConnectorType.Iec62196Type2Ccs),
        ),
    )

    fun toVehicleFromCurrentKWh(
        capacityKWh: Double,
        currentChargeKWh: Double,
    ): Vehicle {
        val safeCapacityKWh = capacityKWh.sanitize(
            MIN_CAPACITY_KWH,
            MAX_CAPACITY_KWH,
            DEFAULT_CAPACITY_KWH,
            "capacity",
        )
        val safeCurrentKWh = currentChargeKWh.sanitize(
            MIN_CHARGE_KWH,
            safeCapacityKWh,
            MIN_CHARGE_KWH,
            "current charge",
        )

        val capacity = Energy.kilowattHours(safeCapacityKWh)
        val current = Energy.kilowattHours(safeCurrentKWh)
        return buildVehicle(current, capacity, safeCapacityKWh)
    }

    private fun buildVehicle(
        current: Energy,
        capacity: Energy,
        capacityKWh: Double,
    ): Vehicle {
        val chargeLevel = ChargeLevel(current, capacity)
        val engine = ElectricEngine(
            consumption = ElectricVehicleConsumption(auxiliaryPower = null, speedConsumption = DEFAULT_CONSUMPTION),
            chargeLevel = chargeLevel,
            chargingParameters = ChargingParameters(
                batteryCurve = buildGenericBatteryCurve(capacityKWh),
                chargingConnectors = DEFAULT_CONNECTORS,
            ),
        )
        return Vehicle.Car(electricEngine = engine)
    }

    // Helper to log and clamp noisy inputs with a fallback when value is NaN/Infinity.
    private fun Double.sanitize(
        min: Double,
        max: Double,
        fallback: Double,
        label: String,
    ): Double {
        val base = if (isNaN() || isInfinite()) fallback else this
        val clamped = base.coerceIn(min, max)
        if (clamped != this) Log.w(TAG, "Clamped $label: $this -> $clamped")
        return clamped
    }

    /**
     * Builds a simple, generic battery charging power curve scaled to capacity.
     * Values are illustrative and conservative, sufficient for planner requirements.
     */
    private fun buildGenericBatteryCurve(capacityKWh: Double): Map<Energy, Power> {
        val c10 = Energy.kilowattHours(capacityKWh * BATTERY_LEVEL_10_PERCENT)
        val c50 = Energy.kilowattHours(capacityKWh * BATTERY_LEVEL_50_PERCENT)
        val c80 = Energy.kilowattHours(capacityKWh * BATTERY_LEVEL_80_PERCENT)

        // Peak/decline charging power assumptions (kW). Keep within realistic ranges.
        val peak = (capacityKWh * PEAK_POWER_CAPACITY_MULTIPLIER).coerceIn(
            PEAK_POWER_MIN_KW,
            PEAK_POWER_MAX_KW,
        )
        val mid = peak * MID_POWER_FRACTION
        val tail = peak * TAIL_POWER_FRACTION

        return mapOf(
            c10 to Power.kilowatts(peak),
            c50 to Power.kilowatts(mid),
            c80 to Power.kilowatts(tail),
        )
    }
}
