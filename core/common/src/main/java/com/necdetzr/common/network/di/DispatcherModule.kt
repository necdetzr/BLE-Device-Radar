package com.necdetzr.common.network.di

import com.necdetzr.common.network.BleDispatchers
import com.necdetzr.common.network.Dispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {
    @Provides
    @Dispatcher(BleDispatchers.IO)
    fun provideIODispatcher() : CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Dispatcher(BleDispatchers.Default)
    fun provideDefaultDispatcher() : CoroutineDispatcher = Dispatchers.Default
}
