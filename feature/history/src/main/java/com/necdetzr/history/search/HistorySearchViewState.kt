package com.necdetzr.history.search

import com.necdetzr.model.DeviceSearchResult
import com.necdetzr.model.ScanRecord
import com.necdetzr.model.ScanRecordDetail
import com.necdetzr.model.ScannedBleDevice

data class HistorySearchViewState(
    val query:String = "",
    val contentState: HistorySearchContentState = HistorySearchContentState.Loading,
    val selectedCategory: SearchCategory = SearchCategory.ALL,
    val expandedDeviceMac: String? = null,
    val selectedScanId: Long? = null,
    val selectedScan: ScanRecordDetail? = null,
    val selectedDevice: ScannedBleDevice? = null,
    val expandedDeviceScans: List<ScanRecord> = emptyList(),
)
sealed interface HistorySearchContentState {

    data object Loading : HistorySearchContentState

    data object Empty : HistorySearchContentState

    data class Success(
        val scans: List<ScanRecord>,
        val devices: List<DeviceSearchResult>
    ) : HistorySearchContentState

    data object Error : HistorySearchContentState
}
