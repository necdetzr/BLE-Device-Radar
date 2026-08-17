package com.necdetzr.database

import androidx.room.TypeConverter
import com.necdetzr.model.BleAdvertisement
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class BleTypeConverters {
    private val json = Json {
        ignoreUnknownKeys = true
    }
    @TypeConverter
    fun fromAdvertisement(value: BleAdvertisement) : String{
        return json.encodeToString(value)
    }
    @TypeConverter
    fun toAdvertisement(value:String) : BleAdvertisement{
        return json.decodeFromString(value)
    }
}
