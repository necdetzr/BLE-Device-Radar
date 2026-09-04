package com.necdetzr.data

import com.google.common.truth.Truth.assertThat
import com.necdetzr.data.repository.DefaultScanHistoryRepository
import com.necdetzr.database.ScanHistoryDao
import com.necdetzr.database.ScanRecordWithDevices
import com.necdetzr.database.entities.BleDeviceEntity
import com.necdetzr.database.entities.ScanRecordEntity
import com.necdetzr.database.relations.DeviceSearchSummaryRow
import com.necdetzr.model.BleAdvertisement
import com.necdetzr.model.ScannedBleDevice
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DefaultScanHistoryRepositoryTest {

    private lateinit var dao: ScanHistoryDao
    private lateinit var repository: DefaultScanHistoryRepository

    @Before
    fun setUp() {
        dao = mockk(relaxed = true)
        repository = DefaultScanHistoryRepository(dao)
    }

    @Test
    fun `searchScans forwards parameters and maps entities`() = runTest {
        val entity = createScanEntity(
            scanId = 4L,
            name = "Office Scan",
        )
        every {
            dao.searchScans(
                query = "Office",
                limit = 30,
            )
        } returns flowOf(listOf(entity))

        val results = repository
            .searchScans(
                query = "Office",
                limit = 30,
            )
            .first()

        assertThat(results).hasSize(1)
        assertThat(results.single().scanId).isEqualTo(4L)
        assertThat(results.single().scanName).isEqualTo("Office Scan")

        verify(exactly = 1) {
            dao.searchScans(
                query = "Office",
                limit = 30,
            )
        }
    }

    @Test
    fun `searchDevices maps device summaries`() = runTest {
        val entity = createDeviceEntity()
        every {
            dao.searchDeviceSummaries(
                query = "Device",
                limit = 10,
            )
        } returns flowOf(
            listOf(
                DeviceSearchSummaryRow(
                    device = entity,
                    scanCount = 3,
                )
            )
        )

        val results = repository
            .searchDevices(
                query = "Device",
                limit = 10,
            )
            .first()

        val result = results.single()

        assertThat(result.scanCount).isEqualTo(3)
        assertThat(result.device.macAddress)
            .isEqualTo(entity.macAddress)
        assertThat(result.device.name)
            .isEqualTo(entity.deviceName)

        verify(exactly = 1) {
            dao.searchDeviceSummaries(
                query = "Device",
                limit = 10,
            )
        }
    }

    @Test
    fun `getScanWithDevices maps relation to detail`() = runTest {
        val scanEntity = createScanEntity(scanId = 8L)
        val deviceEntity = createDeviceEntity(ownerScanId = 8L)
        val relation = ScanRecordWithDevices(
            scanRecord = scanEntity,
            devices = listOf(deviceEntity),
        )

        every {
            dao.getScanWithDevices(8L)
        } returns flowOf(relation)

        val detail = repository.getScanWithDevices(8L).first()

        assertThat(detail).isNotNull()
        assertThat(detail?.scan?.scanId).isEqualTo(8L)
        assertThat(detail?.devices).hasSize(1)
        assertThat(detail?.devices?.single()?.macAddress)
            .isEqualTo(deviceEntity.macAddress)
    }

    @Test
    fun `getScanWithDevices preserves null result`() = runTest {
        every {
            dao.getScanWithDevices(99L)
        } returns flowOf(null)

        val detail = repository.getScanWithDevices(99L).first()

        assertThat(detail).isNull()
    }

    @Test
    fun `getScansForDevice forwards mac and maps scans`() = runTest {
        val firstEntity = createScanEntity(
            scanId = 1L,
            name = "First Scan",
        )
        val secondEntity = createScanEntity(
            scanId = 2L,
            name = "Second Scan",
        )
        val macAddress = "AA:BB:CC:DD:EE:FF"

        every {
            dao.getScansForDevice(macAddress)
        } returns flowOf(
            listOf(secondEntity, firstEntity)
        )

        val results = repository
            .getScansForDevice(macAddress)
            .first()

        assertThat(results.map { it.scanName })
            .containsExactly(
                "Second Scan",
                "First Scan",
            )
            .inOrder()

        verify(exactly = 1) {
            dao.getScansForDevice(macAddress)
        }
    }

    @Test
    fun `saveFullScan creates scan and device entities`() = runTest {
        val scanSlot = slot<ScanRecordEntity>()
        val devicesSlot = slot<List<BleDeviceEntity>>()
        val device = createDeviceModel()
        val beforeSave = System.currentTimeMillis()

        repository.saveFullScan(
            name = "Saved Scan",
            devices = listOf(device),
        )

        val afterSave = System.currentTimeMillis()

        coVerify(exactly = 1) {
            dao.saveAllScan(
                scanRecord = capture(scanSlot),
                devices = capture(devicesSlot),
            )
        }

        val savedScan = scanSlot.captured
        val savedDevice = devicesSlot.captured.single()

        assertThat(savedScan.scanName).isEqualTo("Saved Scan")
        assertThat(savedScan.deviceCount).isEqualTo(1)
        assertThat(savedScan.timeStamp).isAtLeast(beforeSave)
        assertThat(savedScan.timeStamp).isAtMost(afterSave)

        assertThat(savedDevice.ownerScanId).isEqualTo(0L)
        assertThat(savedDevice.macAddress).isEqualTo(device.macAddress)
        assertThat(savedDevice.deviceName).isEqualTo(device.name)
        assertThat(savedDevice.rssi).isEqualTo(device.rssi)
        assertThat(savedDevice.advertisement)
            .isEqualTo(device.advertisement)
    }

    @Test
    fun `total scan count is exposed from dao`() = runTest {
        every {
            dao.getTotalScanCount()
        } returns flowOf(6)

        val count = repository.getTotalScanCount().first()

        assertThat(count).isEqualTo(6)
        verify(exactly = 1) {
            dao.getTotalScanCount()
        }
    }

    @Test
    fun `delete operations are forwarded to dao`() = runTest {
        repository.deleteScan(12L)
        repository.deleteAllScans()

        coVerify(exactly = 1) {
            dao.deleteScan(12L)
        }
        coVerify(exactly = 1) {
            dao.deleteAllScans()
        }
    }

    private fun createScanEntity(
        scanId: Long = 1L,
        name: String = "Test Scan",
    ): ScanRecordEntity =
        ScanRecordEntity(
            scanId = scanId,
            scanName = name,
            timeStamp = 1_000L,
            deviceCount = 1,
        )

    private fun createDeviceEntity(
        ownerScanId: Long = 1L,
    ): BleDeviceEntity =
        BleDeviceEntity(
            deviceId = 2L,
            ownerScanId = ownerScanId,
            macAddress = "AA:BB:CC:DD:EE:FF",
            deviceName = "Test Device",
            rssi = -65,
            firstSeenAt = 1_000L,
            lastSeenAt = 2_000L,
            packetCount = 4,
            advertisement = BleAdvertisement(
                txPower = -10,
                isConnectable = true,
            ),
        )

    private fun createDeviceModel(): ScannedBleDevice =
        ScannedBleDevice(
            macAddress = "AA:BB:CC:DD:EE:FF",
            name = "Test Device",
            rssi = -65,
            firstSeenAt = 1_000L,
            lastSeenAt = 2_000L,
            packetCount = 4,
            advertisement = BleAdvertisement(
                txPower = -10,
                isConnectable = true,
            ),
        )
}
