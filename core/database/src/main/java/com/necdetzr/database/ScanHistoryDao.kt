package com.necdetzr.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.necdetzr.database.entities.BleDeviceEntity
import com.necdetzr.database.entities.ScanRecordEntity
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

    @Transaction
    @Query("SELECT * FROM scan_records WHERE scanId = :scanId LIMIT 1")
    fun getScanWithDevices(scanId:Long): Flow<ScanRecordWithDevices?>

    @Query("DELETE FROM scan_records WHERE scanId = :scanId")
    suspend fun deleteScan(scanId: Long)

}
