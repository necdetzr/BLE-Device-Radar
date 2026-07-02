package com.necdetzr.data.di

import com.necdetzr.data.repository.BleRadarRepository
import com.necdetzr.data.repository.DefaultBleRadarRepository
import com.necdetzr.data.repository.OfflineUserDataRepository
import com.necdetzr.data.repository.UserDataRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    internal abstract fun bindUserDataRepository(
        userDataRepository: OfflineUserDataRepository
    ): UserDataRepository
    @Binds
    internal abstract fun bindBleRadarRepository(
        bleRadarRepository: DefaultBleRadarRepository
    ) : BleRadarRepository
}
