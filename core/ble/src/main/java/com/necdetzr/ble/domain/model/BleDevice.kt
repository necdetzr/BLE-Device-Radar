package com.necdetzr.ble.domain.model

data class BleDevice(
    val name:String?,
    val macAddress:String,
    val rssi:Int
)
