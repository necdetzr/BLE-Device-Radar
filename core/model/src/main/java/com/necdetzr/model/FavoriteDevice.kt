package com.necdetzr.model

data class FavoriteDevice(
    val macAddress:String,
    val deviceName: String?,
    val favoritedAt: Long
)
