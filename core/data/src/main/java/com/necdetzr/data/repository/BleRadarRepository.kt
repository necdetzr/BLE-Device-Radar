package com.necdetzr.data.repository

import com.necdetzr.common.result.Result
import com.necdetzr.model.BleDevice
import kotlinx.coroutines.flow.Flow

interface BleRadarRepository {
    fun startScanning(): Flow<Result<BleDevice>>
}
