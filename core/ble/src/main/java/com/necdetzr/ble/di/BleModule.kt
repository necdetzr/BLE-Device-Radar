package com.necdetzr.ble.di

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import com.necdetzr.ble.data.BleScannerImpl
import com.necdetzr.ble.domain.BleScanner
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class BleModule {
    @Binds
    @Singleton
    abstract fun bindBleScanner(
        bleScannerImpl: BleScannerImpl
    ) : BleScanner
    companion object {
        @Provides
        @Singleton
        fun provideBluetoothAdapter(
            @ApplicationContext context: Context
        ) : BluetoothAdapter {
            val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            return manager.adapter
        }
    }
}
