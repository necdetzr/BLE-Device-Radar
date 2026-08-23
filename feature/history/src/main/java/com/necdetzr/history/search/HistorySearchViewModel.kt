package com.necdetzr.history.search

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.necdetzr.data.repository.ScanHistoryRepository
import com.necdetzr.history.R
import com.necdetzr.model.ScanRecord
import com.necdetzr.model.ScanRecordDetail
import com.necdetzr.model.ScannedBleDevice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class HistorySearchViewModel @Inject constructor(
    private val scanHistoryRepository: ScanHistoryRepository

) : ViewModel(){

    private val _query = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow(SearchCategory.ALL)
    private val _expandedDeviceState =
        MutableStateFlow(ExpandedDeviceState())

    private var deviceScansLoadJob: Job? = null
    private val _sheetState = MutableStateFlow(SheetState())
    private var scanLoadJob: Job? = null
    @OptIn(FlowPreview::class)
    private val normalizedQuery =
        _query
            .map(String::trim)
            .debounce(300.milliseconds)
            .distinctUntilChanged()
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private val scanResults =
        combine(
            normalizedQuery,
            _selectedCategory
        ) {query,category->
            query to category
        }.flatMapLatest{(query,category)->
            if(category == SearchCategory.DEVICE){
                flowOf(emptyList())
            }else{
                scanHistoryRepository.searchScans(
                    query = query,
                    limit = if(query.isBlank()) 10 else 30
                )
            }
        }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private val deviceResults =
        combine(
            normalizedQuery,
            _selectedCategory
        ){query,category->
            query to category
        }.flatMapLatest { (query,category) ->
            if (category == SearchCategory.SCAN) {
                flowOf(emptyList())
            } else {
                scanHistoryRepository.searchDevices(
                    query = query,
                    limit = if (query.isBlank()) 10 else 30
                )
            }
        }
    private val searchResults =
        combine(
            scanResults,
            deviceResults,
        ) { scans, devices ->
            scans to devices
        }
    val uiState : StateFlow<HistorySearchViewState> =
        combine(
            _query,
            searchResults,
            _selectedCategory,
            _expandedDeviceState,
            _sheetState,
            ){query,results,category,expandedDeviceState,sheetState->
            val (scans, devices) = results

            HistorySearchViewState(
                query = query,
                scanResults = scans,
                deviceResults = devices,
                selectedCategory = category,
                expandedDeviceMac = expandedDeviceState.macAddress,
                selectedScan = sheetState.selectedScan,
                selectedDevice = sheetState.selectedDevice,
                expandedDeviceScans = expandedDeviceState.scans

            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistorySearchViewState()
        )



    fun onQueryChange(query:String){
        _query.value = query
    }

    fun onDeviceExpandClick(macAddress:String){
        val currentState = _expandedDeviceState.value
        if (currentState.macAddress == macAddress) {
            deviceScansLoadJob?.cancel()
            _expandedDeviceState.value = ExpandedDeviceState()
            return
        }
        deviceScansLoadJob?.cancel()

        _expandedDeviceState.value = ExpandedDeviceState(
            macAddress = macAddress,
        )
        deviceScansLoadJob = viewModelScope.launch {
            val scans = scanHistoryRepository
                .getScansForDevice(macAddress)
                .first()

            if (_expandedDeviceState.value.macAddress == macAddress) {
                _expandedDeviceState.update {
                    it.copy(scans = scans)
                }
            }
        }

    }

    fun onScanClick(scanId: Long) {
        scanLoadJob?.cancel()

        scanLoadJob = viewModelScope.launch {
            val scan = scanHistoryRepository
                .getScanWithDevices(scanId)
                .first()

            _sheetState.update {
                it.copy(
                    selectedScan = scan,
                    selectedDevice = null,
                )
            }
        }
    }
    fun onCategoryClick(category: SearchCategory){
        _selectedCategory.value = category
    }
    fun onDeviceClick(device: ScannedBleDevice) {
        _sheetState.update {
            it.copy(selectedDevice = device)
        }
    }
    fun onDeviceDetailBack() {
        _sheetState.update {
            it.copy(selectedDevice = null)
        }
    }

    fun onSheetDismissed() {
        scanLoadJob?.cancel()
        scanLoadJob = null
        _sheetState.value = SheetState()
    }
}
enum class SearchCategory(
    @StringRes val labelRes: Int,
) {
    ALL(R.string.feature_history_category_all),
    DEVICE(R.string.feature_history_category_device),
    SCAN(R.string.feature_history_category_scan),
}
private data class SheetState(
    val selectedScan: ScanRecordDetail? = null,
    val selectedDevice: ScannedBleDevice? = null,
)
private data class ExpandedDeviceState(
    val macAddress: String? = null,
    val scans: List<ScanRecord> = emptyList(),
)
