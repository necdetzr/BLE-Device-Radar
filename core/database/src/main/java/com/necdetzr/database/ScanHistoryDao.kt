package com.necdetzr.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.necdetzr.database.entities.BleDeviceEntity
import com.necdetzr.database.entities.ScanRecordEntity
import com.necdetzr.database.relations.DeviceScanRow
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
        """
    )
    fun searchScans(query:String) : Flow<List<ScanRecordEntity>>
    @Query(
        """
    SELECT d.*, s.*
    FROM scanned_devices d
    JOIN scan_records s ON s.scanId = d.ownerScanId
    WHERE d.macAddress IN (
        SELECT macAddress
        FROM scanned_devices
        WHERE deviceName LIKE '%' || :query || '%'
           OR macAddress LIKE '%' || :query || '%'
        GROUP BY macAddress
        ORDER BY
            COUNT(DISTINCT ownerScanId) DESC,
            MAX(lastSeenAt) DESC
        LIMIT :limit
    )
    ORDER BY s.timeStamp DESC
    """
    )
    fun searchDevicesWithScans(
        query: String,
        limit: Int,
    ): Flow<List<DeviceScanRow>>

}
