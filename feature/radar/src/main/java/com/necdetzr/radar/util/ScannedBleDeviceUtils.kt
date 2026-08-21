package com.necdetzr.radar.util

import com.necdetzr.model.ScannedBleDevice

object ScannedBleDeviceUtils {
    fun MutableMap<String, ScannedBleDevice>.updateScannedDevice(
        newDevice: ScannedBleDevice
    ) {
        val existingDevice = this[newDevice.macAddress]

        this[newDevice.macAddress] = if (existingDevice == null) {
            newDevice.copy(
                packetCount = 1
            )
        } else {
            newDevice.copy(
                name = newDevice.name ?: existingDevice.name,
                firstSeenAt = existingDevice.firstSeenAt,
                packetCount = existingDevice.packetCount + 1
            )
        }
    }
}
