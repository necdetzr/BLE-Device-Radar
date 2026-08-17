package com.necdetzr.model

import kotlinx.serialization.Serializable

@Serializable
data class ScanRecord(
    val scanId: Long,
    val scanName: String,
    val timestamp: Long,
    val deviceCount: Int
)
@Serializable
data class ScanRecordDetail(
    val scan: ScanRecord,
    val devices: List<ScannedBleDevice>
)
