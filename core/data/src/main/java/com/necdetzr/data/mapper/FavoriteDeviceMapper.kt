package com.necdetzr.data.mapper

import com.necdetzr.database.entities.FavoriteDeviceEntity
import com.necdetzr.model.FavoriteDevice
import com.necdetzr.model.ScannedBleDevice

fun FavoriteDeviceEntity.toModel(): FavoriteDevice{
    return FavoriteDevice(
        macAddress = macAddress,
        deviceName = deviceName,
        favoritedAt = favoritedAt
    )
}

fun ScannedBleDevice.toFavoriteEntity(
    favoritedAt:Long
) : FavoriteDeviceEntity{
    return FavoriteDeviceEntity(
        macAddress = macAddress,
        deviceName = name,
        favoritedAt = favoritedAt
    )
}
