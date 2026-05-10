package com.camryobd.data.obd

import com.camryobd.model.DTCCode
import com.camryobd.model.DTCSeverity

object DTCDatabase {

    private val dtcMap = mapOf(
        // Powertrain
        "P0100" to Triple("MAF Circuit Malfunction", DTCSeverity.WARNING, "Engine"),
        "P0101" to Triple("MAF Circuit Range/Performance", DTCSeverity.WARNING, "Engine"),
        "P0110" to Triple("Intake Air Temp Sensor Circuit", DTCSeverity.INFO, "Engine"),
        "P0115" to Triple("Engine Coolant Temp Sensor Circuit", DTCSeverity.WARNING, "Engine"),
        "P0120" to Triple("Throttle Position Sensor Circuit", DTCSeverity.WARNING, "Engine"),
        "P0121" to Triple("Throttle Position Sensor Range", DTCSeverity.WARNING, "Engine"),
        "P0130" to Triple("O2 Sensor Circuit (Bank 1 Sensor 1)", DTCSeverity.WARNING, "Engine"),
        "P0136" to Triple("O2 Sensor Circuit (Bank 1 Sensor 2)", DTCSeverity.WARNING, "Engine"),
        "P0171" to Triple("System Too Lean (Bank 1)", DTCSeverity.WARNING, "Fuel System"),
        "P0172" to Triple("System Too Rich (Bank 1)", DTCSeverity.WARNING, "Fuel System"),
        "P0300" to Triple("Random/Multiple Cylinder Misfire", DTCSeverity.CRITICAL, "Engine"),
        "P0301" to Triple("Cylinder 1 Misfire Detected", DTCSeverity.CRITICAL, "Engine"),
        "P0302" to Triple("Cylinder 2 Misfire Detected", DTCSeverity.CRITICAL, "Engine"),
        "P0303" to Triple("Cylinder 3 Misfire Detected", DTCSeverity.CRITICAL, "Engine"),
        "P0304" to Triple("Cylinder 4 Misfire Detected", DTCSeverity.CRITICAL, "Engine"),
        "P0325" to Triple("Knock Sensor Circuit (Bank 1)", DTCSeverity.WARNING, "Engine"),
        "P0340" to Triple("Camshaft Position Sensor Circuit (Bank 1)", DTCSeverity.CRITICAL, "Engine"),
        "P0350" to Triple("Ignition Coil Primary/Secondary Circuit", DTCSeverity.CRITICAL, "Engine"),
        "P0400" to Triple("EGR Flow Malfunction", DTCSeverity.WARNING, "Emissions"),
        "P0420" to Triple("Catalyst System Efficiency Below Threshold", DTCSeverity.WARNING, "Emissions"),
        "P0430" to Triple("Catalyst System Efficiency (Bank 2)", DTCSeverity.WARNING, "Emissions"),
        "P0440" to Triple("EVAP Emission Control System Malfunction", DTCSeverity.INFO, "Emissions"),
        "P0441" to Triple("EVAP Incorrect Purge Flow", DTCSeverity.INFO, "Emissions"),
        "P0442" to Triple("EVAP Small Leak Detected", DTCSeverity.INFO, "Emissions"),
        "P0446" to Triple("EVAP Vent Control Circuit", DTCSeverity.INFO, "Emissions"),
        "P0450" to Triple("EVAP Pressure Sensor Malfunction", DTCSeverity.INFO, "Emissions"),
        "P0455" to Triple("EVAP Large Leak Detected", DTCSeverity.WARNING, "Emissions"),
        "P0500" to Triple("Vehicle Speed Sensor Malfunction", DTCSeverity.WARNING, "Engine"),
        "P0505" to Triple("Idle Control System Malfunction", DTCSeverity.WARNING, "Engine"),
        "P0606" to Triple("ECM/PCM Processor Fault", DTCSeverity.CRITICAL, "Engine"),
        "P0705" to Triple("Transmission Range Sensor Circuit", DTCSeverity.WARNING, "Transmission"),
        "P0720" to Triple("Output Speed Sensor Circuit", DTCSeverity.WARNING, "Transmission"),
        "P0741" to Triple("Torque Converter Clutch Circuit", DTCSeverity.WARNING, "Transmission"),
        "P0750" to Triple("Shift Solenoid A Malfunction", DTCSeverity.WARNING, "Transmission"),
        "P0770" to Triple("Shift Solenoid E Malfunction", DTCSeverity.WARNING, "Transmission"),
        // Toyota-specific
        "P1130" to Triple("Air-Fuel Ratio Sensor Circuit Range (Bank 1)", DTCSeverity.WARNING, "Engine"),
        "P1150" to Triple("Air-Fuel Ratio Sensor Circuit Range (Bank 2)", DTCSeverity.WARNING, "Engine"),
        "P1300" to Triple("Igniter Circuit Malfunction (Bank 1)", DTCSeverity.CRITICAL, "Engine"),
        "P1310" to Triple("Igniter Circuit Malfunction (Bank 2)", DTCSeverity.CRITICAL, "Engine"),
        "P1349" to Triple("VVT-i System Malfunction (Bank 1)", DTCSeverity.WARNING, "Engine"),
        "P1600" to Triple("ECM BATT Malfunction", DTCSeverity.CRITICAL, "Engine"),
    )

    fun lookup(code: String): DTCCode {
        val entry = dtcMap[code.uppercase()]
        return if (entry != null) {
            DTCCode(code, entry.first, entry.second, entry.third)
        } else {
            val system = when {
                code.startsWith("P0") || code.startsWith("P1") -> "Engine/Powertrain"
                code.startsWith("C") -> "Chassis"
                code.startsWith("B") -> "Body"
                code.startsWith("U") -> "Network"
                else -> "Unknown"
            }
            DTCCode(code, "Unknown fault code - Consult dealer", DTCSeverity.WARNING, system)
        }
    }

    fun lookupAll(codes: List<String>): List<DTCCode> = codes.map { lookup(it) }
}
