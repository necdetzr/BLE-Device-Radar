package com.necdetzr.data.repository

import com.necdetzr.model.FavoriteDevice
import com.necdetzr.model.ScannedBleDevice
import kotlinx.coroutines.flow.Flow

interface FavoriteDeviceRepository {
    fun getFavoriteDevices(): Flow<List<FavoriteDevice>>

    fun isFavorite(macAddress: String): Flow<Boolean>

    suspend fun setFavorite(
        device: ScannedBleDevice,
        shouldBeFavorite: Boolean,
    )
}
