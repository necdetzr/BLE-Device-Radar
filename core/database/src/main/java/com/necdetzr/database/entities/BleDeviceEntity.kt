package com.necdetzr.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.necdetzr.model.BleAdvertisement


@Entity(
    tableName = "scanned_devices",
    foreignKeys = [
        ForeignKey(
            entity = ScanRecordEntity::class,
            parentColumns = ["scanId"],
            childColumns = ["ownerScanId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("ownerScanId"),
        Index(
            value = ["ownerScanId","macAddress"],
            unique = true
        )
    ]
)
data class BleDeviceEntity (
    @PrimaryKey(autoGenerate = true)
    val deviceId:Long = 0,
    val ownerScanId: Long,
    val macAddress:String,
    val deviceName:String,
    val rssi:Int,
    val firstSeenAt: Long,
    val lastSeenAt: Long,
    val packetCount: Int,
    val advertisement: BleAdvertisement
)
