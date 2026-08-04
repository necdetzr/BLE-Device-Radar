package com.necdetzr.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.necdetzr.database.entities.BleDeviceEntity
import com.necdetzr.database.entities.ScanRecordEntity


@Database(
    entities = [
        ScanRecordEntity::class,
        BleDeviceEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(BleTypeConverters::class)
abstract class BleRadarDatabase : RoomDatabase() {
    abstract fun scanHistoryDao() : ScanHistoryDao
}
