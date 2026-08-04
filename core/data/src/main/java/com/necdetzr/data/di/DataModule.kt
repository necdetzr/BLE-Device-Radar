package com.necdetzr.data.di

import com.necdetzr.data.repository.BleRadarRepository
import com.necdetzr.data.repository.DefaultBleRadarRepository
import com.necdetzr.data.repository.DefaultScanHistoryRepository
import com.necdetzr.data.repository.OfflineUserDataRepository
import com.necdetzr.data.repository.ScanHistoryRepository
import com.necdetzr.data.repository.UserDataRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    @Singleton
    internal abstract fun bindUserDataRepository(
        userDataRepository: OfflineUserDataRepository
    ): UserDataRepository
    @Binds
    @Singleton
    internal abstract fun bindBleRadarRepository(
        bleRadarRepository: DefaultBleRadarRepository
    ) : BleRadarRepository
    @Binds
    @Singleton
    internal abstract fun bindScanHistoryRepository(
        scanHistoryRepository: DefaultScanHistoryRepository
    ) : ScanHistoryRepository
}
