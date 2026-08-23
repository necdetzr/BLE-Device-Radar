package com.necdetzr.database.relations

import androidx.room.Embedded
import com.necdetzr.database.entities.BleDeviceEntity

data class DeviceSearchSummaryRow (
    @Embedded
    val device: BleDeviceEntity,
    val scanCount:Int
)
