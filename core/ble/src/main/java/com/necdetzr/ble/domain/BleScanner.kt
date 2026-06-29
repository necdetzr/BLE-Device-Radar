package com.necdetzr.ble.domain

import com.necdetzr.ble.domain.model.BleDevice
import com.necdetzr.common.result.Result
import kotlinx.coroutines.flow.Flow

interface BleScanner {
    fun startScanning() : Flow<Result<BleDevice>>
    fun stopScanning()
}
