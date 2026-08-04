package com.necdetzr.data.repository

import com.necdetzr.data.mapper.toEntity
import com.necdetzr.data.mapper.toModel
import com.necdetzr.database.ScanHistoryDao
import com.necdetzr.database.entities.ScanRecordEntity
import com.necdetzr.model.ScanRecord
import com.necdetzr.model.ScanRecordDetail
import com.necdetzr.model.ScannedBleDevice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DefaultScanHistoryRepository @Inject constructor(
    private val dao: ScanHistoryDao
) : ScanHistoryRepository{
    override suspend fun deleteScan(scanId: Long) {
        dao.deleteScan(scanId)
    }

    override fun getAllScans(): Flow<List<ScanRecord>> {
        return dao.getAllScans()
            .map { entities->
                entities.map(ScanRecordEntity::toModel)
            }
    }

    override fun getScanWithDevices(scanId: Long): Flow<ScanRecordDetail?> {
        return dao.getScanWithDevices(scanId)
            .map { it?.toModel()}
    }

    override suspend fun saveFullScan(
        name: String,
        devices: List<ScannedBleDevice>
    ) {
        val scanRecordEntity = ScanRecordEntity(
            scanName = name,
            timeStamp = System.currentTimeMillis(),
            deviceCount = devices.size
        )

        val deviceEntities = devices.map { device ->
            device.toEntity()
        }

        dao.saveAllScan(
            scanRecord = scanRecordEntity,
            devices = deviceEntities
        )
    }
}
