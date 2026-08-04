package com.necdetzr.database

import androidx.room.Embedded
import androidx.room.Relation

data class ScanRecordWithDevices(
    @Embedded
    val scanRecord: ScanRecordEntity,

    @Relation(
        parentColumn = "scanId",
        entityColumn = "ownerScanId"
    )
    val devices: List<BleDeviceEntity>
)
