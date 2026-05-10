package com.camryobd.data.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.util.Log
import com.camryobd.data.obd.OBDCommands
import com.camryobd.model.OBDData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class BluetoothOBDManager {

    companion object {
        private const val TAG = "BluetoothOBDManager"
        private val OBD_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private const val READ_TIMEOUT = 2000L
        private const val POLL_INTERVAL = 500L
    }

    private var socket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    private var isConnected = false

    @SuppressLint("MissingPermission")
    suspend fun connect(deviceAddress: String): Boolean = withContext(Dispatchers.IO) {
        try {
            disconnect()
            val adapter = BluetoothAdapter.getDefaultAdapter() ?: return@withContext false
            val device = adapter.getRemoteDevice(deviceAddress)
            socket = device.createRfcommSocketToServiceRecord(OBD_UUID)
            adapter.cancelDiscovery()
            socket?.connect()
            inputStream = socket?.inputStream
            outputStream = socket?.outputStream
            isConnected = true

            // Initialize ELM327
            for (cmd in OBDCommands.INIT_COMMANDS) {
                sendCommand(cmd)
                delay(300)
            }
            true
        } catch (e: IOException) {
            Log.e(TAG, "Connection failed: ${e.message}")
            disconnect()
            false
        }
    }

    fun disconnect() {
        try {
            isConnected = false
            inputStream?.close()
            outputStream?.close()
            socket?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Disconnect error: ${e.message}")
        } finally {
            socket = null
            inputStream = null
            outputStream = null
        }
    }

    fun isConnected() = isConnected

    suspend fun sendCommand(command: String): String = withContext(Dispatchers.IO) {
        try {
            outputStream?.write("$command\r".toByteArray())
            outputStream?.flush()
            readResponse()
        } catch (e: IOException) {
            Log.e(TAG, "Send command error: ${e.message}")
            ""
        }
    }

    private fun readResponse(): String {
        val buffer = StringBuilder()
        val start = System.currentTimeMillis()
        try {
            while (System.currentTimeMillis() - start < READ_TIMEOUT) {
                val available = inputStream?.available() ?: 0
                if (available > 0) {
                    val byte = inputStream?.read() ?: break
                    val char = byte.toChar()
                    if (char == '>') break
                    buffer.append(char)
                } else {
                    Thread.sleep(10)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Read error: ${e.message}")
        }
        return buffer.toString().trim()
    }

    fun liveDataFlow(): Flow<OBDData> = flow {
        while (isConnected) {
            try {
                val rpmResp = sendCommand(OBDCommands.ENGINE_RPM)
                val speedResp = sendCommand(OBDCommands.VEHICLE_SPEED)
                val tempResp = sendCommand(OBDCommands.COOLANT_TEMP)
                val throttleResp = sendCommand(OBDCommands.THROTTLE_POSITION)
                val loadResp = sendCommand(OBDCommands.ENGINE_LOAD)
                val fuelResp = sendCommand(OBDCommands.FUEL_LEVEL)
                val intakeResp = sendCommand(OBDCommands.INTAKE_AIR_TEMP)

                emit(
                    OBDData(
                        rpm = OBDCommands.parseRPM(rpmResp),
                        speed = OBDCommands.parseSpeed(speedResp),
                        coolantTemp = OBDCommands.parseCoolantTemp(tempResp),
                        throttlePosition = OBDCommands.parseThrottlePosition(throttleResp),
                        engineLoad = OBDCommands.parseEngineLoad(loadResp),
                        fuelLevel = OBDCommands.parseFuelLevel(fuelResp),
                        intakeAirTemp = OBDCommands.parseIntakeAirTemp(intakeResp)
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Live data error: ${e.message}")
            }
            delay(POLL_INTERVAL)
        }
    }.flowOn(Dispatchers.IO)

    suspend fun readDTCs(): List<String> = withContext(Dispatchers.IO) {
        val response = sendCommand(OBDCommands.GET_DTCS)
        OBDCommands.parseDTCs(response)
    }

    suspend fun clearDTCs(): Boolean = withContext(Dispatchers.IO) {
        try {
            sendCommand(OBDCommands.CLEAR_DTCS)
            true
        } catch (e: Exception) { false }
    }
}
