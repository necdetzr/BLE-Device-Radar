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

    private var scanJob: Job? = null

    private val scannedDevices = HashMap<String,BleDevice>()
    fun onDeviceSelected(){

    }
    fun onStartButtonClicked(){
        scannedDevices.clear()
        _feedUiState.value = DeviceFeedUiState.Scanning(emptyList())
        scanJob?.cancel()

        scanJob = viewModelScope.launch {
            bleRadarRepository.startScanning()
                .timeout(15.seconds)
                .catch { e->
                    println("Hata = $e")
                    e.printStackTrace()
                    if (e is TimeoutCancellationException){
                        _feedUiState.value = DeviceFeedUiState.Success(scannedDevices.values.sortedByDescending { it.rssi })
                    }else{
                    }
                }
                .collect { result->
                    when(result){
                        is Result.Success -> {
                            val newDevice = result.data
                            scannedDevices[newDevice.macAddress] = newDevice
                            _feedUiState.value = DeviceFeedUiState.Scanning(scannedDevices.values.sortedByDescending { it.rssi })
                        }
                        is Result.Error -> {

                        }
                        is Result.Loading -> {

                        }
                    }
                }
        }
    }
    fun onStopButtonClicked(){
        scanJob?.cancel()
        _feedUiState.value = DeviceFeedUiState.Success(scannedDevices.values.sortedByDescending{ it.rssi })

    }

}
