package com.necdetzr.ble.domain


import com.necdetzr.common.result.Result
import com.necdetzr.model.ScannedBleDevice
import kotlinx.coroutines.flow.Flow

interface BleScanner {
    fun startScanning() : Flow<Result<ScannedBleDevice>>
}
