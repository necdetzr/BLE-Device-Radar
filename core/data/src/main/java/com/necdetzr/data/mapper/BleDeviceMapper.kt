package com.necdetzr.data.mapper


import com.necdetzr.database.entities.BleDeviceEntity
import com.necdetzr.model.ScannedBleDevice

internal fun BleDeviceEntity.toModel(): ScannedBleDevice {
    return ScannedBleDevice(
        macAddress = macAddress,
        name = deviceName,
        rssi = rssi,
        advertisement = advertisement,
        firstSeenAt = firstSeenAt,
        lastSeenAt = lastSeenAt,
        packetCount = packetCount
    )
}

internal fun ScannedBleDevice.toEntity(
    ownerScanId: Long = 0
): BleDeviceEntity {
    return BleDeviceEntity(
        ownerScanId = ownerScanId,
        macAddress = macAddress,
        deviceName = name,
        rssi = rssi,
        firstSeenAt = firstSeenAt,
        lastSeenAt = lastSeenAt,
        packetCount = packetCount,
        advertisement = advertisement
    )
}
