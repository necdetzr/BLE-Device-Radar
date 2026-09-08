package com.necdetzr.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.necdetzr.database.dao.FavoriteDeviceDao
import com.necdetzr.database.dao.ScanHistoryDao
import com.necdetzr.database.entities.BleDeviceEntity
import com.necdetzr.database.entities.FavoriteDeviceEntity
import com.necdetzr.database.entities.ScanRecordEntity
import com.necdetzr.model.FavoriteDevice


@Database(
    entities = [
        ScanRecordEntity::class,
        BleDeviceEntity::class,
        FavoriteDeviceEntity::class
    ],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
@TypeConverters(BleTypeConverters::class)
abstract class BleRadarDatabase : RoomDatabase() {
    abstract fun scanHistoryDao() : ScanHistoryDao
    abstract fun favoriteDeviceDao() : FavoriteDeviceDao
}
