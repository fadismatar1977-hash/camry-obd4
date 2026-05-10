package com.camryobd.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.camryobd.data.repository.OBDRepository
import com.camryobd.model.BluetoothDevice
import com.camryobd.model.ConnectionState
import com.camryobd.model.DTCCode
import com.camryobd.model.OBDData
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    val repository = OBDRepository()

    val connectionState: StateFlow<ConnectionState> = repository.connectionState

    private val _liveData = MutableStateFlow(OBDData())
    val liveData: StateFlow<OBDData> = _liveData

    private val _dtcCodes = MutableStateFlow<List<DTCCode>>(emptyList())
    val dtcCodes: StateFlow<List<DTCCode>> = _dtcCodes

    private val _pairedDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val pairedDevices: StateFlow<List<BluetoothDevice>> = _pairedDevices

    private val _statusMessage = MutableStateFlow("")
    val statusMessage: StateFlow<String> = _statusMessage

    private var liveDataJob: Job? = null

    fun loadPairedDevices() {
        _pairedDevices.value = repository.getPairedDevices()
    }

    fun connectToDevice(address: String) {
        viewModelScope.launch {
            val success = repository.connect(address)
            if (success) {
                _statusMessage.value = "متصل بنجاح"
                startLiveData()
            } else {
                _statusMessage.value = "فشل الاتصال. تحقق من الجهاز."
            }
        }
    }

    fun disconnect() {
        liveDataJob?.cancel()
        repository.disconnect()
        _liveData.value = OBDData()
        _statusMessage.value = "تم قطع الاتصال"
    }

    private fun startLiveData() {
        liveDataJob?.cancel()
        liveDataJob = viewModelScope.launch {
            repository.getLiveDataFlow().collect { data ->
                _liveData.value = data
            }
        }
    }

    fun readDTCs() {
        viewModelScope.launch {
            _statusMessage.value = "جارٍ قراءة أكواد الأعطال..."
            val codes = repository.readDTCs()
            _dtcCodes.value = codes
            _statusMessage.value = if (codes.isEmpty()) "لا توجد أعطال مسجلة" else "تم العثور على ${codes.size} عطل"
        }
    }

    fun clearDTCs() {
        viewModelScope.launch {
            val success = repository.clearDTCs()
            if (success) {
                _dtcCodes.value = emptyList()
                _statusMessage.value = "تم مسح أكواد الأعطال"
            } else {
                _statusMessage.value = "فشل مسح الأعطال"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        repository.disconnect()
    }
}
