package com.necdetzr.data.repository

import com.necdetzr.model.DeviceSearchResult
import com.necdetzr.model.ScanRecord
import com.necdetzr.model.ScanRecordDetail
import com.necdetzr.model.ScannedBleDevice
import kotlinx.coroutines.flow.Flow

interface ScanHistoryRepository {
    suspend fun saveFullScan(name: String,devices:List<ScannedBleDevice>)
    fun getAllScans(): Flow<List<ScanRecord>>
    fun getRecentScans(): Flow<List<ScanRecord>>
    fun getScanWithDevices(scanId:Long):Flow<ScanRecordDetail?>
    suspend fun deleteScan(scanId:Long)
    fun getTotalScanCount(): Flow<Int>
    fun searchScans(query:String,limit:Int) : Flow<List<ScanRecord>>
    fun searchDevices(query:String,limit:Int) : Flow<List<DeviceSearchResult>>
    fun getScansForDevice(macAddress: String): Flow<List<ScanRecord>>
    suspend fun deleteAllScans()
}
