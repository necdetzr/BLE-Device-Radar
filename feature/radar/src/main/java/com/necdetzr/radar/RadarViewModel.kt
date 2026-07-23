package com.necdetzr.radar


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.necdetzr.common.result.Result
import com.necdetzr.data.repository.BleRadarRepository
import com.necdetzr.data.repository.UserDataRepository
import com.necdetzr.model.BleDevice
import com.necdetzr.ui.DeviceFeedUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class RadarViewModel @Inject constructor(
    private val bleRadarRepository: BleRadarRepository,
    private val userDataRepository: UserDataRepository

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
           val userData = userDataRepository.userPreferences.first()
            val scanPeriod = userData.scanPeriod
            val currentRssi = userData.rssiRange

            withTimeoutOrNull(scanPeriod.milliseconds){
                bleRadarRepository
                    .startScanning()
                    .catch {
                        _radarMessage.value = RadarUserMessage.ScanFailed
                    }
                    .collect { result->
                        when(result){
                            is Result.Success -> {
                                val newDevice = result.data
                                scannedDevices[newDevice.macAddress] = newDevice
                                _feedUiState.value =
                                    DeviceFeedUiState.Scanning(sortedDevices(currentRssi))
                            }
                            is Result.Error -> {
                                _radarMessage.value =
                                    RadarUserMessage.ScanFailed

                            }
                            is Result.Loading -> Unit
                        }
                    }
            }
            completeScanning(currentRssi)
            scanJob = null

        }
    }
    private fun completeScanning(rssi:Int){
            _feedUiState.value =
                DeviceFeedUiState.Success(
                    devices = sortedDevices(rssi)
                )


    }
    private fun sortedDevices(rssi:Int): List<BleDevice> =
        scannedDevices.values
            .filter { device -> device.rssi >= rssi }
            .sortedByDescending(BleDevice::rssi)

    fun onStopButtonClicked(){
        scanJob?.cancel()
        scanJob = null
        viewModelScope.launch {
            val currentRssi = userDataRepository.userPreferences.first().rssiRange
            completeScanning(currentRssi)
        }

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
