package com.camryobd.data.repository

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import com.camryobd.data.bluetooth.BluetoothOBDManager
import com.camryobd.data.obd.DTCDatabase
import com.camryobd.model.BluetoothDevice
import com.camryobd.model.ConnectionState
import com.camryobd.model.DTCCode
import com.camryobd.model.OBDData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class OBDRepository {

    private val obdManager = BluetoothOBDManager()

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    @SuppressLint("MissingPermission")
    fun getPairedDevices(): List<BluetoothDevice> {
        return try {
            val adapter = BluetoothAdapter.getDefaultAdapter()
            adapter?.bondedDevices?.map {
                BluetoothDevice(it.name ?: "Unknown", it.address)
            } ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun connect(address: String): Boolean {
        _connectionState.value = ConnectionState.CONNECTING
        val success = obdManager.connect(address)
        _connectionState.value = if (success) ConnectionState.CONNECTED else ConnectionState.ERROR
        return success
    }

    fun disconnect() {
        obdManager.disconnect()
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    fun getLiveDataFlow(): Flow<OBDData> = obdManager.liveDataFlow()

    suspend fun readDTCs(): List<DTCCode> {
        val codes = obdManager.readDTCs()
        return DTCDatabase.lookupAll(codes)
    }

    suspend fun clearDTCs(): Boolean = obdManager.clearDTCs()

    fun isConnected() = obdManager.isConnected()
}
