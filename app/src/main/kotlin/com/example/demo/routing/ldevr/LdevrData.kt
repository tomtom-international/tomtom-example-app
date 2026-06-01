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

package com.example.demo.routing.ldevr

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

enum class EvCarRange {
    Short,
    Medium,
    Large,
}

internal object EvSampleCars {
    val mediumRangeCar = Vehicle.Car(
        electricEngine = ElectricEngine(
            consumption = ElectricVehicleConsumption(null, EvCarsProperties.MediumRangeCar.consumption),
            chargeLevel = EvCarsProperties.MediumRangeCar.chargeLevel,
            chargingParameters = ChargingParameters(
                batteryCurve = EvCarsProperties.MediumRangeCar.batteryCurve,
                chargingConnectors = EvCarsProperties.MediumRangeCar.chargingConnector,
            ),
        ),
    )

    val shortRangeCar = Vehicle.Car(
        electricEngine = ElectricEngine(
            consumption = ElectricVehicleConsumption(null, EvCarsProperties.ShortRangeCar.consumption),
            chargeLevel = EvCarsProperties.ShortRangeCar.chargeLevel,
            chargingParameters = ChargingParameters(
                batteryCurve = EvCarsProperties.ShortRangeCar.batteryCurve,
                chargingConnectors = EvCarsProperties.ShortRangeCar.chargingConnector,
            ),
        ),
    )

    val largeRangeCar = Vehicle.Car(
        electricEngine = ElectricEngine(
            consumption = ElectricVehicleConsumption(null, EvCarsProperties.LargeRangeCar.consumption),
            chargeLevel = EvCarsProperties.LargeRangeCar.chargeLevel,
            chargingParameters = ChargingParameters(
                batteryCurve = EvCarsProperties.LargeRangeCar.batteryCurve,
                chargingConnectors = EvCarsProperties.LargeRangeCar.chargingConnector,
            ),
        ),
    )
}

private class EvCarsProperties private constructor(
    val consumption: Map<Speed, Force>,
    val batteryCurve: Map<Energy, Power>,
    val chargingConnector: List<ChargingConnector>,
    val chargeLevel: ChargeLevel,
) {
    companion object {
        private val consumptions = hashMapOf(
            EvCarRange.Short to mapOf(
                Speed.kilometersPerHour(0.0) to ElectricConsumption.kilowattHoursPer100Kilometers(12.0),
                Speed.kilometersPerHour(50.0) to ElectricConsumption.kilowattHoursPer100Kilometers(13.5),
                Speed.kilometersPerHour(80.0) to ElectricConsumption.kilowattHoursPer100Kilometers(15.0),
                Speed.kilometersPerHour(120.0) to ElectricConsumption.kilowattHoursPer100Kilometers(19.0),
            ),
            EvCarRange.Medium to mapOf(
                Speed.kilometersPerHour(0.0) to ElectricConsumption.kilowattHoursPer100Kilometers(13.0),
                Speed.kilometersPerHour(50.0) to ElectricConsumption.kilowattHoursPer100Kilometers(14.5),
                Speed.kilometersPerHour(80.0) to ElectricConsumption.kilowattHoursPer100Kilometers(16.5),
                Speed.kilometersPerHour(120.0) to ElectricConsumption.kilowattHoursPer100Kilometers(21.0),
            ),
            EvCarRange.Large to mapOf(
                Speed.kilometersPerHour(0.0) to ElectricConsumption.kilowattHoursPer100Kilometers(15.0),
                Speed.kilometersPerHour(50.0) to ElectricConsumption.kilowattHoursPer100Kilometers(16.5),
                Speed.kilometersPerHour(80.0) to ElectricConsumption.kilowattHoursPer100Kilometers(19.0),
                Speed.kilometersPerHour(120.0) to ElectricConsumption.kilowattHoursPer100Kilometers(24.0),
            ),
        )

        private val batteryCurves = hashMapOf(
            EvCarRange.Short to mapOf(
                Energy.kilowattHours(3.0) to Power.kilowatts(100.0),
                Energy.kilowattHours(10.0) to Power.kilowatts(90.0),
                Energy.kilowattHours(25.0) to Power.kilowatts(60.0),
                Energy.kilowattHours(35.0) to Power.kilowatts(30.0),
                Energy.kilowattHours(39.0) to Power.kilowatts(10.0),
            ),
            EvCarRange.Medium to mapOf(
                Energy.kilowattHours(5.0) to Power.kilowatts(135.0),
                Energy.kilowattHours(10.0) to Power.kilowatts(130.0),
                Energy.kilowattHours(30.0) to Power.kilowatts(100.0),
                Energy.kilowattHours(50.0) to Power.kilowatts(70.0),
                Energy.kilowattHours(60.0) to Power.kilowatts(40.0),
            ),
            EvCarRange.Large to mapOf(
                Energy.kilowattHours(10.0) to Power.kilowatts(200.0),
                Energy.kilowattHours(30.0) to Power.kilowatts(180.0),
                Energy.kilowattHours(60.0) to Power.kilowatts(150.0),
                Energy.kilowattHours(80.0) to Power.kilowatts(100.0),
                Energy.kilowattHours(95.0) to Power.kilowatts(50.0),
            ),
        )

        private val chargingConnectors = hashMapOf(
            EvCarRange.Short to listOf(
                ChargingConnector(
                    currentType = CurrentType.AcThreePhase,
                    plugTypes = listOf(ConnectorType.Iec62196Type2Outlet),
                ),
                ChargingConnector(
                    currentType = CurrentType.Dc,
                    plugTypes = listOf(ConnectorType.Iec62196Type2Ccs),
                ),
            ),
            EvCarRange.Medium to listOf(
                ChargingConnector(
                    currentType = CurrentType.AcThreePhase,
                    plugTypes = listOf(ConnectorType.Iec62196Type2Outlet),
                ),
                ChargingConnector(
                    currentType = CurrentType.Dc,
                    plugTypes = listOf(ConnectorType.Iec62196Type2Ccs),
                ),
            ),
            EvCarRange.Large to listOf(
                ChargingConnector(
                    currentType = CurrentType.AcThreePhase,
                    plugTypes = listOf(ConnectorType.Iec62196Type2Outlet),
                ),
                ChargingConnector(
                    currentType = CurrentType.Dc,
                    plugTypes = listOf(ConnectorType.Iec62196Type2Ccs),
                ),
            ),
        )

        private val chargeLevel = hashMapOf(
            EvCarRange.Short to ChargeLevel(
                Energy.kilowattHours(40.0),
                Energy.kilowattHours(40.0),
            ),
            EvCarRange.Medium to ChargeLevel(
                Energy.kilowattHours(64.0),
                Energy.kilowattHours(64.0),
            ),
            EvCarRange.Large to ChargeLevel(
                Energy.kilowattHours(100.0),
                Energy.kilowattHours(100.0),
            ),
        )

        val MediumRangeCar = EvCarsProperties(
            consumption = consumptions[EvCarRange.Medium]!!,
            batteryCurve = batteryCurves[EvCarRange.Medium]!!,
            chargingConnector = chargingConnectors[EvCarRange.Medium]!!,
            chargeLevel = chargeLevel[EvCarRange.Medium]!!,
        )

        val ShortRangeCar = EvCarsProperties(
            consumption = consumptions[EvCarRange.Short]!!,
            batteryCurve = batteryCurves[EvCarRange.Short]!!,
            chargingConnector = chargingConnectors[EvCarRange.Short]!!,
            chargeLevel = chargeLevel[EvCarRange.Short]!!,
        )

        val LargeRangeCar = EvCarsProperties(
            consumption = consumptions[EvCarRange.Large]!!,
            batteryCurve = batteryCurves[EvCarRange.Large]!!,
            chargingConnector = chargingConnectors[EvCarRange.Large]!!,
            chargeLevel = chargeLevel[EvCarRange.Large]!!,
        )
    }
}
