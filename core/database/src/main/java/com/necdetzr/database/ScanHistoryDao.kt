package com.necdetzr.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.necdetzr.database.entities.BleDeviceEntity
import com.necdetzr.database.entities.ScanRecordEntity
import com.necdetzr.database.relations.DeviceSearchSummaryRow
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBleDevices(devices:List<BleDeviceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScanRecord(scanRecord: ScanRecordEntity) : Long

    @Transaction
    suspend fun saveAllScan(scanRecord: ScanRecordEntity, devices:List<BleDeviceEntity>){
        val newScanId = insertScanRecord(scanRecord)
        val devicesWithScanId = devices.map { it.copy(ownerScanId = newScanId) }
        insertBleDevices(devicesWithScanId)
    }

    @Query("SELECT * FROM scan_records ORDER BY timeStamp DESC")
    fun getAllScans(): Flow<List<ScanRecordEntity>>

    @Query("SELECT * FROM scan_records ORDER BY timestamp DESC LIMIT 5")
    fun getRecentScans(): Flow<List<ScanRecordEntity>>

    @Transaction
    @Query("SELECT * FROM scan_records WHERE scanId = :scanId LIMIT 1")
    fun getScanWithDevices(scanId:Long): Flow<ScanRecordWithDevices?>

    @Query("DELETE FROM scan_records WHERE scanId = :scanId")
    suspend fun deleteScan(scanId: Long)

    @Query("SELECT COUNT(*) FROM scan_records")
    fun getTotalScanCount(): Flow<Int>

    @Query(
        """
            SELECT * FROM scan_records
            WHERE scanName LIKE '%' || :query || '%'
            ORDER BY timeStamp DESC
            LIMIT :limit
        """
    )
    fun searchScans(query:String,limit:Int) : Flow<List<ScanRecordEntity>>

    @Query(
        """
                SELECT device.*,
            (
                SELECT COUNT(*)
                FROM scanned_devices AS occurrence
                WHERE occurrence.macAddress = device.macAddress
            ) AS scanCount
            FROM scanned_devices AS device
            WHERE device.deviceId = (
                SELECT latest.deviceId
                FROM scanned_devices AS latest
                WHERE latest.macAddress = device.macAddress
                ORDER BY latest.lastSeenAt DESC, latest.deviceId DESC
                LIMIT 1
            )
            AND EXISTS (
                SELECT 1
                FROM scanned_devices AS matching
                WHERE matching.macAddress = device.macAddress
                AND (
                    matching.deviceName LIKE '%' || :query || '%' COLLATE NOCASE
                    OR matching.macAddress LIKE '%' || :query || '%' COLLATE NOCASE
            )
         )
        ORDER BY scanCount DESC, device.lastSeenAt DESC
        LIMIT :limit
        """
    )
    fun searchDeviceSummaries(
        query:String,
        limit: Int
    ) : Flow<List<DeviceSearchSummaryRow>>
    @Query(
        """
            SELECT scan_records.*
            FROM scan_records
            INNER JOIN scanned_devices
                ON scanned_devices.ownerScanId = scan_records.scanId
            WHERE scanned_devices.macAddress = :macAddress
            ORDER BY scan_records.timeStamp DESC
        """
    )
    fun getScansForDevice(
        macAddress: String
    ):Flow<List<ScanRecordEntity>>

    @Query("DELETE FROM scan_records")
    suspend fun deleteAllScans()
}
