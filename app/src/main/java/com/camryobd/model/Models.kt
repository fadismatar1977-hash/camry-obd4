package com.camryobd.model

data class OBDData(
    val rpm: Int = 0,
    val speed: Int = 0,
    val coolantTemp: Int = 0,
    val throttlePosition: Float = 0f,
    val engineLoad: Float = 0f,
    val fuelLevel: Float = 0f,
    val intakeAirTemp: Int = 0,
    val mafAirFlow: Float = 0f,
    val fuelPressure: Int = 0,
    val timingAdvance: Float = 0f,
    val batteryVoltage: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)

data class DTCCode(
    val code: String,
    val description: String,
    val severity: DTCSeverity,
    val system: String
)

enum class DTCSeverity {
    CRITICAL, WARNING, INFO
}

enum class ConnectionState {
    DISCONNECTED, CONNECTING, CONNECTED, ERROR
}

data class BluetoothDevice(
    val name: String,
    val address: String
)
