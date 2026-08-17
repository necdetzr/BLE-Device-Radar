package com.necdetzr.model

data class DeviceSearchResult(
    val device: ScannedBleDevice,
    val scans: List<ScanRecord>,
)
