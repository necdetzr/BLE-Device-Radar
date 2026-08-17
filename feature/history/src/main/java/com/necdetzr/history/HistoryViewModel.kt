package com.necdetzr.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.necdetzr.data.repository.ScanHistoryRepository
import com.necdetzr.model.ScannedBleDevice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository : ScanHistoryRepository,

): ViewModel() {
    private val _uiState = MutableStateFlow(HistoryScreenState())
    val uiState : StateFlow<HistoryScreenState> = _uiState.asStateFlow()


    val recentScans = historyRepository.getRecentScans()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    val totalScans = historyRepository.getTotalScanCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
    private var scanLoadJob: Job? = null

    fun onScanClick(scanId: Long) {
        scanLoadJob?.cancel()

        scanLoadJob = viewModelScope.launch {
            val scan = historyRepository
                .getScanWithDevices(scanId)
                .first()

            _uiState.update {
                it.copy(
                    selectedScan = scan,
                    selectedDevice = null,
                )
            }
        }

    }
    fun onDeviceClick(device: ScannedBleDevice){
        _uiState.update { it.copy(selectedDevice = device) }


    }
    fun onDeviceDetailBack() {
        _uiState.update {
            it.copy(selectedDevice = null)
        }
    }
    fun onSheetDismissed(){
        scanLoadJob?.cancel()
        scanLoadJob = null
        _uiState.update { it.copy(selectedScan = null,selectedDevice = null) }

    }
}
