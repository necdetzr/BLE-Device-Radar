package com.necdetzr.radar

import android.util.Log
import android.util.Log.e
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.necdetzr.common.result.Result
import com.necdetzr.data.repository.BleRadarRepository
import com.necdetzr.model.BleDevice
import com.necdetzr.ui.DeviceFeedUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class RadarViewModel @Inject constructor(
    private val bleRadarRepository: BleRadarRepository

): ViewModel() {

    private val _feedUiState = MutableStateFlow<DeviceFeedUiState>(DeviceFeedUiState.Idle)
    val feedUiState : StateFlow<DeviceFeedUiState> = _feedUiState.asStateFlow()

    private val _radarMessage = MutableStateFlow<RadarUserMessage?>(null)
    val radarMessage : StateFlow<RadarUserMessage?> = _radarMessage.asStateFlow()
    private var scanJob: Job? = null

    private val scannedDevices = HashMap<String,BleDevice>()
    fun onDeviceSelected(){

    }

    override fun onCleared() {
        scanJob?.cancel()
        super.onCleared()
    }
    fun onStartButtonClicked(){
        scanJob?.cancel()
        scannedDevices.clear()
        _feedUiState.value = DeviceFeedUiState.Scanning(emptyList())


        scanJob = viewModelScope.launch {
            bleRadarRepository
                .startScanning()
                .timeout(15.seconds)
                .catch { e->
                    when(e){
                        is TimeoutCancellationException ->{
                            completeScanning()
                        }
                        else -> {
                            _feedUiState.value = DeviceFeedUiState.Idle
                            _radarMessage.value = RadarUserMessage.ScanFailed
                        }
                    }

                }
                .collect { result->
                    when(result){
                        is Result.Success -> {
                            val newDevice = result.data
                            scannedDevices[newDevice.macAddress] = newDevice
                            _feedUiState.value =
                                DeviceFeedUiState.Scanning(sortedDevices())
                        }
                        is Result.Error -> {
                            _feedUiState.value =
                                DeviceFeedUiState.Success(sortedDevices())
                            _radarMessage.value = RadarUserMessage.ScanFailed
                            scanJob?.cancel()

                        }
                        is Result.Loading -> Unit
                    }
                }
        }
    }
    private fun completeScanning(){
        _feedUiState.value =
            DeviceFeedUiState.Success(
                devices = sortedDevices()
            )
    }
    private fun sortedDevices(): List<BleDevice> =
        scannedDevices.values
            .sortedByDescending(BleDevice::rssi)

    fun onStopButtonClicked(){
        scanJob?.cancel()
        scanJob = null
        completeScanning()

    }
    fun onBluetoothNotSupported(){
        _radarMessage.value = RadarUserMessage.BluetoothNotSupported
    }
    fun onPermissionDenied(){
        _radarMessage.value = RadarUserMessage.PermissionDenied
    }
    fun onBluetoothEnableDenied(){
        _radarMessage.value = RadarUserMessage.BluetoothEnableDenied
    }
    fun onUserMessageShown(){
        _radarMessage.value = null
    }

}

sealed interface RadarUserMessage {
    data object BluetoothNotSupported : RadarUserMessage
    data object PermissionDenied : RadarUserMessage
    data object ScanFailed : RadarUserMessage
    data object BluetoothEnableDenied : RadarUserMessage
}
