package com.camryobd.data.obd

object OBDCommands {
    // Mode 01 - Current Data
    const val ENGINE_RPM = "010C"
    const val VEHICLE_SPEED = "010D"
    const val COOLANT_TEMP = "0105"
    const val THROTTLE_POSITION = "0111"
    const val ENGINE_LOAD = "0104"
    const val FUEL_LEVEL = "012F"
    const val INTAKE_AIR_TEMP = "010F"
    const val MAF_AIR_FLOW = "0110"
    const val FUEL_PRESSURE = "010A"
    const val TIMING_ADVANCE = "010E"

    // Mode 03 - Stored DTCs
    const val GET_DTCS = "03"

    // Mode 04 - Clear DTCs
    const val CLEAR_DTCS = "04"

    // ATZ = reset, ATE0 = echo off, ATL0 = linefeeds off, ATH0 = headers off
    val INIT_COMMANDS = listOf("ATZ", "ATE0", "ATL0", "ATH0", "ATSP0")

    fun parseRPM(response: String): Int {
        return try {
            val bytes = parseHexResponse(response, "41 0C")
            if (bytes.size >= 2) {
                ((bytes[0] * 256) + bytes[1]) / 4
            } else 0
        } catch (e: Exception) { 0 }
    }

    fun parseSpeed(response: String): Int {
        return try {
            val bytes = parseHexResponse(response, "41 0D")
            if (bytes.isNotEmpty()) bytes[0] else 0
        } catch (e: Exception) { 0 }
    }

    fun parseCoolantTemp(response: String): Int {
        return try {
            val bytes = parseHexResponse(response, "41 05")
            if (bytes.isNotEmpty()) bytes[0] - 40 else 0
        } catch (e: Exception) { 0 }
    }

    fun parseThrottlePosition(response: String): Float {
        return try {
            val bytes = parseHexResponse(response, "41 11")
            if (bytes.isNotEmpty()) bytes[0] * 100f / 255f else 0f
        } catch (e: Exception) { 0f }
    }

    fun parseEngineLoad(response: String): Float {
        return try {
            val bytes = parseHexResponse(response, "41 04")
            if (bytes.isNotEmpty()) bytes[0] * 100f / 255f else 0f
        } catch (e: Exception) { 0f }
    }

    fun parseFuelLevel(response: String): Float {
        return try {
            val bytes = parseHexResponse(response, "41 2F")
            if (bytes.isNotEmpty()) bytes[0] * 100f / 255f else 0f
        } catch (e: Exception) { 0f }
    }

    fun parseIntakeAirTemp(response: String): Int {
        return try {
            val bytes = parseHexResponse(response, "41 0F")
            if (bytes.isNotEmpty()) bytes[0] - 40 else 0
        } catch (e: Exception) { 0 }
    }

    fun parseDTCs(response: String): List<String> {
        val dtcs = mutableListOf<String>()
        try {
            val cleaned = response.replace("\r", " ").replace("\n", " ")
                .replace("43", "").trim()
            val hex = cleaned.split(" ").filter { it.length == 2 }
            var i = 0
            while (i + 1 < hex.size) {
                val b1 = hex[i].toInt(16)
                val b2 = hex[i + 1].toInt(16)
                if (b1 == 0 && b2 == 0) { i += 2; continue }
                val prefix = when ((b1 shr 6) and 0x03) {
                    0 -> "P"
                    1 -> "C"
                    2 -> "B"
                    3 -> "U"
                    else -> "P"
                }
                val code = "$prefix${String.format("%01X", (b1 shr 4) and 0x03)}${String.format("%01X", b1 and 0x0F)}${String.format("%02X", b2)}"
                dtcs.add(code)
                i += 2
            }
        } catch (e: Exception) { /* ignore */ }
        return dtcs
    }

    private fun parseHexResponse(response: String, prefix: String): List<Int> {
        val cleaned = response.replace("\r", " ").replace("\n", " ").trim()
        val start = cleaned.indexOf(prefix)
        val data = if (start >= 0) cleaned.substring(start + prefix.length).trim() else cleaned
        return data.split(" ").filter { it.length == 2 }.mapNotNull {
            try { it.toInt(16) } catch (e: Exception) { null }
        }
    }
}
