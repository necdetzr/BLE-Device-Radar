package com.necdetzr.data

import com.google.common.truth.Truth.assertThat
import com.necdetzr.data.mapper.toEntity
import com.necdetzr.data.mapper.toModel
import com.necdetzr.database.ScanRecordWithDevices
import com.necdetzr.database.entities.BleDeviceEntity
import com.necdetzr.database.entities.ScanRecordEntity
import com.necdetzr.model.BleAdvertisement
import com.necdetzr.model.ScannedBleDevice
import org.junit.Test

class DataMappersTest {

    @Test
    fun `ble device entity maps to model`() {
        val advertisement = BleAdvertisement(
            txPower = -10,
            isConnectable = true,
            rawData = byteArrayOf(1, 2, 3),
        )
        val entity = BleDeviceEntity(
            deviceId = 12L,
            ownerScanId = 4L,
            macAddress = "AA:BB:CC:DD:EE:FF",
            deviceName = "Test Device",
            rssi = -65,
            firstSeenAt = 1_000L,
            lastSeenAt = 2_000L,
            packetCount = 5,
            advertisement = advertisement,
        )

        val model = entity.toModel()

        assertThat(model.macAddress).isEqualTo(entity.macAddress)
        assertThat(model.name).isEqualTo(entity.deviceName)
        assertThat(model.rssi).isEqualTo(entity.rssi)
        assertThat(model.firstSeenAt).isEqualTo(entity.firstSeenAt)
        assertThat(model.lastSeenAt).isEqualTo(entity.lastSeenAt)
        assertThat(model.packetCount).isEqualTo(entity.packetCount)
        assertThat(model.advertisement).isEqualTo(advertisement)
    }

    @Test
    fun `ble device model maps to entity with owner scan id`() {
        val device = ScannedBleDevice(
            macAddress = "AA:BB:CC:DD:EE:FF",
            name = null,
            rssi = -70,
            advertisement = BleAdvertisement(txPower = -15),
            firstSeenAt = 1_000L,
            lastSeenAt = 3_000L,
            packetCount = 8,
        )

        val entity = device.toEntity(ownerScanId = 42L)

        assertThat(entity.ownerScanId).isEqualTo(42L)
        assertThat(entity.macAddress).isEqualTo(device.macAddress)
        assertThat(entity.deviceName).isNull()
        assertThat(entity.rssi).isEqualTo(device.rssi)
        assertThat(entity.firstSeenAt).isEqualTo(device.firstSeenAt)
        assertThat(entity.lastSeenAt).isEqualTo(device.lastSeenAt)
        assertThat(entity.packetCount).isEqualTo(device.packetCount)
        assertThat(entity.advertisement).isEqualTo(device.advertisement)
    }

    @Test
    fun `scan record entity maps to model`() {
        val entity = ScanRecordEntity(
            scanId = 7L,
            scanName = "Office Scan",
            timeStamp = 5_000L,
            deviceCount = 3,
        )

        val model = entity.toModel()

        assertThat(model.scanId).isEqualTo(7L)
        assertThat(model.scanName).isEqualTo("Office Scan")
        assertThat(model.timestamp).isEqualTo(5_000L)
        assertThat(model.deviceCount).isEqualTo(3)
    }

    @Test
    fun `scan relation maps to detail with devices`() {
        val scanEntity = ScanRecordEntity(
            scanId = 7L,
            scanName = "Office Scan",
            timeStamp = 5_000L,
            deviceCount = 1,
        )
        val deviceEntity = BleDeviceEntity(
            deviceId = 9L,
            ownerScanId = 7L,
            macAddress = "AA:BB:CC:DD:EE:FF",
            deviceName = "Test Device",
            rssi = -60,
            firstSeenAt = 1_000L,
            lastSeenAt = 2_000L,
            packetCount = 4,
            advertisement = BleAdvertisement(),
        )
        val relation = ScanRecordWithDevices(
            scanRecord = scanEntity,
            devices = listOf(deviceEntity),
        )

        val detail = relation.toModel()

        assertThat(detail.scan).isEqualTo(scanEntity.toModel())
        assertThat(detail.devices)
            .containsExactly(deviceEntity.toModel())
    }
}
