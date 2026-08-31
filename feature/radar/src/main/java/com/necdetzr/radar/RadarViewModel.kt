package com.necdetzr.radar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.necdetzr.data.repository.BleRadarRepository
import com.necdetzr.data.repository.ScanHistoryRepository
import com.necdetzr.data.repository.UserDataRepository
import com.necdetzr.model.ScannedBleDevice
import com.necdetzr.radar.util.ScannedBleDeviceUtils.updateScannedDevice
import com.necdetzr.ui.DeviceFeedUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.milliseconds
@Suppress("TooManyFunctions")
@HiltViewModel
class RadarViewModel @Inject constructor(
    private val bleRadarRepository: BleRadarRepository,
    private val userDataRepository: UserDataRepository,
    private val scanHistoryRepository: ScanHistoryRepository

): ViewModel() {
    private val _uiState = MutableStateFlow(RadarScreenState())
    val uiState : StateFlow<RadarScreenState> = _uiState.asStateFlow()
    private var scanJob: Job? = null

    private val _selectedMacAddress = MutableStateFlow<String?>(null)
    val selectedDevice: StateFlow<ScannedBleDevice?> = combine(
        _uiState,
        _selectedMacAddress
    ) { state, macAddress ->
        if (macAddress == null) return@combine null

        when (val feed = state.feedState) {
            is DeviceFeedUiState.Scanning -> feed.devices.find { it.macAddress == macAddress }
            is DeviceFeedUiState.Success -> feed.devices.find { it.macAddress == macAddress }
            else -> null
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
    private val scannedDevices = HashMap<String, ScannedBleDevice>()

    fun onDeviceSelected(device: ScannedBleDevice){
        _selectedMacAddress.value = device.macAddress
    }
    fun onSheetDismissed(){
        _selectedMacAddress.value = null
    }
    fun onSaveClick(){
        _uiState.update { it.copy(showAlertDialog = true) }
    }
    fun onSaveDialogDismissed() {
        _uiState.update { it.copy(showAlertDialog = false) }
    }

    override fun onCleared() {
        scanJob?.cancel()
        super.onCleared()
    }
    fun onStartButtonClicked(){
        scanJob?.cancel()
        scannedDevices.clear()
        _uiState.update { it.copy(feedState =DeviceFeedUiState.Scanning(emptyList()),saveButtonEnabled = true)}


        scanJob = viewModelScope.launch {
            var scanFailed = false
            try {
                val userData = userDataRepository.userPreferences.first()
                val scanPeriod = userData.scanPeriod
                val currentRssi = userData.rssiRange
                withTimeoutOrNull(scanPeriod.milliseconds){
                    bleRadarRepository
                        .startScanning()
                        .catch {
                            scanFailed = true
                            _uiState.update {
                                it.copy(
                                    feedState = DeviceFeedUiState.Idle,
                                    radarMessage = RadarUserMessage.ScanFailed,
                                    saveButtonEnabled = false,
                                )
                            }
                        }
                        .collect { device->
                            scannedDevices.updateScannedDevice(device)
                            _uiState.update { currentState ->
                                currentState.copy(
                                    feedState = DeviceFeedUiState.Scanning(
                                        devices = sortedDevices(currentRssi)
                                    )
                                )
                            }
                        }
                }
                if(!scanFailed){
                    completeScanning(currentRssi)
                }
            }finally {
                scanJob = null
            }

        }
    }
    private fun completeScanning(rssi:Int){
        _uiState.update {
            it.copy(feedState = DeviceFeedUiState.Success(devices = sortedDevices(rssi)))
        }
    }
    private fun sortedDevices(rssi:Int): List<ScannedBleDevice> =
        scannedDevices.values
            .filter { device -> device.rssi >= rssi }
            .sortedByDescending(ScannedBleDevice::rssi)

    fun onStopButtonClicked(){
        scanJob?.cancel()
        scanJob = null
        viewModelScope.launch {
            val currentRssi = userDataRepository.userPreferences.first().rssiRange
            completeScanning(currentRssi)
        }

    }
    fun onSaveRecordClick(name: String) {
        viewModelScope.launch {
            val state = _uiState.value
            val feedState = state.feedState as? DeviceFeedUiState.Success
                ?: return@launch

            if (!state.saveButtonEnabled) return@launch

            _uiState.update {
                it.copy(saveButtonEnabled = false)
            }

            try {
                scanHistoryRepository.saveFullScan(
                    name = name.trim(),
                    devices = feedState.devices,
                )

                _uiState.update {
                    it.copy(showAlertDialog = false)
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        saveButtonEnabled = true,
                        radarMessage = RadarUserMessage.SaveFailed,
                    )
                }
            }
        }
    }


    fun onPermissionDenied(){
        _uiState.update { it.copy(radarMessage = RadarUserMessage.PermissionDenied) }
    }

    fun onBluetoothEnableDenied(){
        _uiState.update { it.copy(radarMessage = RadarUserMessage.BluetoothEnableDenied) }
    }

    fun onUserMessageShown(){
        _uiState.update { it.copy(radarMessage = null) }
    }

}

sealed interface RadarUserMessage {
    data object PermissionDenied : RadarUserMessage
    data object ScanFailed : RadarUserMessage
    data object BluetoothEnableDenied : RadarUserMessage
    data object SaveFailed : RadarUserMessage
}
