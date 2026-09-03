package com.necdetzr.database

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.necdetzr.database.entities.BleDeviceEntity
import com.necdetzr.database.entities.ScanRecordEntity
import com.necdetzr.model.BleAdvertisement
import com.necdetzr.model.ScanRecord
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class ScanHistoryDaoTest {

    private lateinit var database: BleRadarDatabase
    private lateinit var dao: ScanHistoryDao

    @Before
    fun setUp(){
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        database = Room.inMemoryDatabaseBuilder(
            context,
            BleRadarDatabase::class.java
        ).build()

        dao = database.scanHistoryDao()
    }
    @After
    fun tearDown(){
        database.close()
    }

    @Test
    fun saveAllScan_savesScanAndAssignsGeneratedIdToDevices() = runTest{
        val scan = ScanRecordEntity(
            scanName = "Office Scan",
            timeStamp = 1_000L,
            deviceCount = 1,
        )
        val device = BleDeviceEntity(
            ownerScanId = 0L,
            macAddress = "AA:BB:CC:DD:EE:FF",
            deviceName = "Test Device",
            rssi = -50,
            firstSeenAt = 1_000L,
            lastSeenAt = 2_000L,
            packetCount = 3,
            advertisement = BleAdvertisement(),
        )

        dao.saveAllScan(
            scan,listOf(device)
        )
        val savedScan = dao.getAllScans().first().single()
        val detail = requireNotNull(
            dao.getScanWithDevices(savedScan.scanId).first()
        )
        val savedDevice = detail.devices.single()

        assertThat(savedScan.scanId).isGreaterThan(0L)
        assertThat(savedScan.scanName).isEqualTo("Office Scan")
        assertThat(detail.scanRecord).isEqualTo(savedScan)
        assertThat(savedDevice.deviceId).isGreaterThan(0L)
        assertThat(savedDevice.ownerScanId)
            .isEqualTo(savedScan.scanId)
        assertThat(savedDevice.macAddress)
            .isEqualTo(device.macAddress)
        assertThat(savedDevice.deviceName)
            .isEqualTo(device.deviceName)
        assertThat(savedDevice.advertisement)
            .isEqualTo(device.advertisement)
    }
    @Test
    fun deleteScan_deletesItsDevicesWithCascade() = runTest {
        val scan = ScanRecordEntity(
            scanName = "Office Scan",
            timeStamp = 1_000L,
            deviceCount = 1,
        )
        val device = BleDeviceEntity(
            ownerScanId = 0L,
            macAddress = "AA:BB:CC:DD:EE:FF",
            deviceName = "Test Device",
            rssi = -50,
            firstSeenAt = 1_000L,
            lastSeenAt = 2_000L,
            packetCount = 3,
            advertisement = BleAdvertisement(),
        )

        dao.saveAllScan(scan,listOf(device))

        val savedScan = dao.getAllScans().first().single()

        assertThat(
            dao.searchDeviceSummaries(
                query = "",
                limit = 10
            ).first()
        ).hasSize(1)

        dao.deleteScan(savedScan.scanId)

        assertThat(dao.getAllScans().first()).isEmpty()
        assertThat(dao.getScanWithDevices(savedScan.scanId).first()).isNull()

        assertThat(
            dao.searchDeviceSummaries(
                query = "",
                limit = 10
            ).first()
        ).isEmpty()

    }

    @Test
    fun getRecentScans_returnsLatestFiveScans() = runTest {
        repeat(7){index->
            dao.insertScanRecord(
                ScanRecordEntity(
                    scanName = "Scan ${index+1}",
                    timeStamp = (index + 1) * 1_000L,
                    deviceCount = 0
                )
            )
        }
        val recentScans = dao.getRecentScans().first()

        assertThat(recentScans).hasSize(5)
        assertThat(recentScans.map { it.scanName })
            .containsExactly(
                "Scan 7",
                "Scan 6",
                "Scan 5",
                "Scan 4",
                "Scan 3",
            ).inOrder()

        assertThat(recentScans.map { it.timeStamp })
            .containsExactly(
                7_000L,
                6_000L,
                5_000L,
                4_000L,
                3_000L,
            )
            .inOrder()
    }

    @Test
    fun searchDeviceSummaries_groupsByMacAndOrdersByScanCount() = runTest {
        val repeatedMac = "AA:AA:AA:AA:AA:AA"
        val otherMac = "BB:BB:BB:BB:BB:BB"

        dao.saveAllScan(
            scanRecord = ScanRecordEntity(
                scanName = "First Scan",
                timeStamp = 1_000L,
                deviceCount = 1,
            ),
            devices = listOf(
                createDeviceEntity(
                    macAddress = repeatedMac,
                    name = "Old Name",
                    lastSeenAt = 1_000L,
                )
            ),
        )
        dao.saveAllScan(
            scanRecord = ScanRecordEntity(
                scanName = "Second Scan",
                timeStamp = 2_000L,
                deviceCount = 1,
            ),
            devices = listOf(
                createDeviceEntity(
                    macAddress = repeatedMac,
                    name = "Latest Name",
                    lastSeenAt = 3_000L,
                )
            ),
        )
        dao.saveAllScan(
            scanRecord = ScanRecordEntity(
                scanName = "Third Scan",
                timeStamp = 3_000L,
                deviceCount = 1,
            ),
            devices = listOf(
                createDeviceEntity(
                    macAddress = otherMac,
                    name = "Other Device",
                    lastSeenAt = 4_000L,
                )
            ),
        )
        val results = dao.searchDeviceSummaries(
            query = "",
            limit = 10,
        ).first()
        assertThat(results).hasSize(2)
        val firstResult = results[0]
        val secondResult = results[1]

        assertThat(firstResult.device.macAddress)
            .isEqualTo(repeatedMac)
        assertThat(firstResult.device.deviceName)
            .isEqualTo("Latest Name")
        assertThat(firstResult.scanCount).isEqualTo(2)

        assertThat(secondResult.device.macAddress)
            .isEqualTo(otherMac)
        assertThat(secondResult.scanCount).isEqualTo(1)
    }

    @Test
    fun searchDeviceSummaries_matchesNameAndMacIgnoringCase() = runTest {
        val macAddress = "AA:BB:CC:DD:EE:FF"

        dao.saveAllScan(
            scanRecord = ScanRecordEntity(
                scanName = "Office Scan",
                timeStamp = 1_000L,
                deviceCount = 1,
            ),
            devices = listOf(
                createDeviceEntity(
                    macAddress = macAddress,
                    name = "Heart Rate Monitor",
                    lastSeenAt = 1_000L,
                )
            ),
        )

        val nameResults = dao.searchDeviceSummaries(
            query = "heart",
            limit = 10,
        ).first()

        val macResults = dao.searchDeviceSummaries(
            query = "bb:cc",
            limit = 10,
        ).first()

        val missingResults = dao.searchDeviceSummaries(
            query = "keyboard",
            limit = 10,
        ).first()

        assertThat(nameResults.single().device.macAddress)
            .isEqualTo(macAddress)

        assertThat(macResults.single().device.macAddress)
            .isEqualTo(macAddress)

        assertThat(missingResults).isEmpty()
    }
    @Test
    fun getScansForDevice_returnsMatchingScansNewestFirst() = runTest {
        val targetMac = "AA:BB:CC:DD:EE:FF"

        dao.saveAllScan(
            scanRecord = ScanRecordEntity(
                scanName = "Old Target Scan",
                timeStamp = 1_000L,
                deviceCount = 1,
            ),
            devices = listOf(
                createDeviceEntity(
                    macAddress = targetMac,
                    name = "Target Device",
                    lastSeenAt = 1_000L,
                )
            ),
        )

        dao.saveAllScan(
            scanRecord = ScanRecordEntity(
                scanName = "Unrelated Scan",
                timeStamp = 2_000L,
                deviceCount = 1,
            ),
            devices = listOf(
                createDeviceEntity(
                    macAddress = "11:22:33:44:55:66",
                    name = "Other Device",
                    lastSeenAt = 2_000L,
                )
            ),
        )

        dao.saveAllScan(
            scanRecord = ScanRecordEntity(
                scanName = "New Target Scan",
                timeStamp = 3_000L,
                deviceCount = 1,
            ),
            devices = listOf(
                createDeviceEntity(
                    macAddress = targetMac,
                    name = "Target Device",
                    lastSeenAt = 3_000L,
                )
            ),
        )

        val results = dao.getScansForDevice(targetMac).first()

        assertThat(results.map { it.scanName }).containsExactly(
            "New Target Scan",
            "Old Target Scan",
        ).inOrder()
    }
    @Test
    fun searchScans_filtersOrdersAndLimitsResults() = runTest {
        dao.insertScanRecord(
            ScanRecordEntity(
                scanName = "Office Morning",
                timeStamp = 1_000L,
                deviceCount = 1,
            )
        )
        dao.insertScanRecord(
            ScanRecordEntity(
                scanName = "Home Scan",
                timeStamp = 2_000L,
                deviceCount = 1,
            )
        )
        dao.insertScanRecord(
            ScanRecordEntity(
                scanName = "Office Evening",
                timeStamp = 3_000L,
                deviceCount = 1,
            )
        )

        val results = dao.searchScans(
            query = "office",
            limit = 1,
        ).first()

        assertThat(results).hasSize(1)
        assertThat(results.single().scanName)
            .isEqualTo("Office Evening")
    }
    @Test
    fun deleteAllScans_removesScansAndDevices() = runTest {
        dao.saveAllScan(
            scanRecord = ScanRecordEntity(
                scanName = "First Scan",
                timeStamp = 1_000L,
                deviceCount = 1,
            ),
            devices = listOf(
                createDeviceEntity(
                    macAddress = "AA:BB:CC:DD:EE:FF",
                    name = "First Device",
                    lastSeenAt = 1_000L,
                )
            ),
        )

        dao.saveAllScan(
            scanRecord = ScanRecordEntity(
                scanName = "Second Scan",
                timeStamp = 2_000L,
                deviceCount = 1,
            ),
            devices = listOf(
                createDeviceEntity(
                    macAddress = "11:22:33:44:55:66",
                    name = "Second Device",
                    lastSeenAt = 2_000L,
                )
            ),
        )

        dao.deleteAllScans()

        assertThat(dao.getAllScans().first()).isEmpty()
        assertThat(dao.getTotalScanCount().first()).isEqualTo(0)
        assertThat(
            dao.searchDeviceSummaries(
                query = "",
                limit = 10,
            ).first()
        ).isEmpty()
    }
    @Test
    fun getTotalScanCount_reflectsInsertedAndDeletedScans() = runTest {
        val firstScanId = dao.insertScanRecord(
            ScanRecordEntity(
                scanName = "First Scan",
                timeStamp = 1_000L,
                deviceCount = 0,
            )
        )

        dao.insertScanRecord(
            ScanRecordEntity(
                scanName = "Second Scan",
                timeStamp = 2_000L,
                deviceCount = 0,
            )
        )

        assertThat(dao.getTotalScanCount().first()).isEqualTo(2)

        dao.deleteScan(firstScanId)

        assertThat(dao.getTotalScanCount().first()).isEqualTo(1)
    }
}
private fun createDeviceEntity(
    macAddress: String,
    name: String?,
    lastSeenAt: Long,
): BleDeviceEntity {
    return BleDeviceEntity(
        ownerScanId = 0L,
        macAddress = macAddress,
        deviceName = name,
        rssi = -50,
        firstSeenAt = lastSeenAt,
        lastSeenAt = lastSeenAt,
        packetCount = 1,
        advertisement = BleAdvertisement(),
    )
}
