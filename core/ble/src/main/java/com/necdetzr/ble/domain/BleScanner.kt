package com.necdetzr.ble.domain


import com.necdetzr.common.result.Result
import com.necdetzr.model.BleDevice
import kotlinx.coroutines.flow.Flow

interface BleScanner {
    fun startScanning() : Flow<Result<BleDevice>>
    fun stopScanning()
}
