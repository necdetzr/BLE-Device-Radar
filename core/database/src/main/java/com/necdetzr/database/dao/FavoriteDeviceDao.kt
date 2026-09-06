package com.necdetzr.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.necdetzr.database.entities.FavoriteDeviceEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface FavoriteDeviceDao {
    @Upsert
    suspend fun addFavorite(device: FavoriteDeviceEntity)

    @Query(
        """
            DELETE FROM favorite_devices
            WHERE macAddress = :macAddress
        """
    )
    suspend fun removeFavorite(macAddress:String)

    @Query(
        """
            SELECT * FROM favorite_devices
            ORDER BY favoritedAt DESC
        """
    )
    fun getFavoriteDevices(): Flow<List<FavoriteDeviceEntity>>

    @Query(
        """
            SELECT EXISTS(
                SELECT 1 FROM favorite_devices
                  WHERE macAddress = :macAddress
            )
        """
    )
    fun isFavorite(macAddress: String): Flow<Boolean>

}
