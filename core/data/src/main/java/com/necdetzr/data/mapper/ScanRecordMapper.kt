package com.necdetzr.data.mapper

import com.necdetzr.database.ScanRecordWithDevices
import com.necdetzr.database.entities.ScanRecordEntity
import com.necdetzr.model.ScanRecord
import com.necdetzr.model.ScanRecordDetail

internal fun ScanRecordEntity.toModel(): ScanRecord {
    return ScanRecord(
        scanId = scanId,
        scanName = scanName,
        timestamp = timeStamp,
        deviceCount = deviceCount
    )
}

internal fun ScanRecordWithDevices.toModel(): ScanRecordDetail {
    return ScanRecordDetail(
        scan = scanRecord.toModel(),
        devices = devices.map { it.toModel() }
    )
}
