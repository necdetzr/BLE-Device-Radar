package com.necdetzr.database.relations

import androidx.room.Embedded
import com.necdetzr.database.entities.BleDeviceEntity
import com.necdetzr.database.entities.ScanRecordEntity

data class DeviceScanRow(
    @Embedded
    val device: BleDeviceEntity,

    @Embedded
    val scan: ScanRecordEntity,
)
