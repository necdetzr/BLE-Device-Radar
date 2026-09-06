package com.necdetzr.data.repository

import com.necdetzr.data.mapper.toFavoriteEntity
import com.necdetzr.data.mapper.toModel
import com.necdetzr.database.dao.FavoriteDeviceDao
import com.necdetzr.database.entities.FavoriteDeviceEntity
import com.necdetzr.model.FavoriteDevice
import com.necdetzr.model.ScannedBleDevice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.collections.map

class DefaultFavoriteDeviceRepository @Inject constructor(
    private val dao: FavoriteDeviceDao
): FavoriteDeviceRepository{
    override fun getFavoriteDevices(): Flow<List<FavoriteDevice>> {
       return dao.getFavoriteDevices().map { entities ->
           entities.map(FavoriteDeviceEntity::toModel)
       }
    }

    override fun isFavorite(macAddress: String): Flow<Boolean> {
        return dao.isFavorite(macAddress)
    }

    override suspend fun setFavorite(device: ScannedBleDevice, shouldBeFavorite: Boolean) {
        if(shouldBeFavorite){
            dao.addFavorite(
                device.toFavoriteEntity(
                    favoritedAt = System.currentTimeMillis()
                )
            )
        }else{
            dao.removeFavorite(device.macAddress)
        }
    }
}
