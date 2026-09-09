package com.necdetzr.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_devices")
data class FavoriteDeviceEntity(
    @PrimaryKey
    val macAddress: String,
    val deviceName: String?,
    val favoritedAt: Long
)
