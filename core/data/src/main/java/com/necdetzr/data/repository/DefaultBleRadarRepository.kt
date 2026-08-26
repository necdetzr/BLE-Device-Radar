package com.necdetzr.data.repository

import com.necdetzr.ble.domain.BleScanner
import com.necdetzr.model.ScannedBleDevice
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DefaultBleRadarRepository @Inject constructor(
    private val bleScanner: BleScanner
) : BleRadarRepository{
    override fun startScanning(): Flow<ScannedBleDevice> {
        return bleScanner.startScanning()
    }

}
