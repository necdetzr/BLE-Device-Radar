package com.necdetzr.history.search

import com.necdetzr.model.DeviceSearchResult
import com.necdetzr.model.ScanRecord
import com.necdetzr.model.ScanRecordDetail
import com.necdetzr.model.ScannedBleDevice

data class HistorySearchViewState(
    val query:String = "",
    val scanResults: List<ScanRecord> = emptyList(),
    val deviceResults: List<DeviceSearchResult> = emptyList(),
    val selectedCategory: SearchCategory = SearchCategory.ALL,
    val expandedDeviceMac: String? = null,
    val selectedScanId: Long? = null,
    val selectedScan: ScanRecordDetail? = null,
    val selectedDevice: ScannedBleDevice? = null,
    val expandedDeviceScans: List<ScanRecord> = emptyList(),
)
