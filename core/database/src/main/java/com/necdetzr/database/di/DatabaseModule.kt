package com.necdetzr.database.di

import android.content.Context
import androidx.room.Room
import com.necdetzr.database.BleRadarDatabase
import com.necdetzr.database.ScanHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideBleRadarDatabase(
        @ApplicationContext context : Context
    ): BleRadarDatabase{
        return Room.databaseBuilder(
            context,
            BleRadarDatabase::class.java,
            "ble_radar_database"
        ).build()
    }
    @Provides
    @Singleton
    fun provideScanHistoryDao(database: BleRadarDatabase) : ScanHistoryDao {
        return database.scanHistoryDao()
    }
}
