package com.necdetzr.data.repository

import com.necdetzr.model.ScannedBleDevice
import kotlinx.coroutines.flow.Flow

interface BleRadarRepository {
    fun startScanning(): Flow<ScannedBleDevice>
}
