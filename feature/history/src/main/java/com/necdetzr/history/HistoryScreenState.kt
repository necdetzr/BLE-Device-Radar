package com.necdetzr.history

import com.necdetzr.model.ScanRecordDetail
import com.necdetzr.model.ScannedBleDevice

data class HistoryScreenState(
    val selectedScan: ScanRecordDetail? = null,
    val selectedDevice: ScannedBleDevice? = null
)
