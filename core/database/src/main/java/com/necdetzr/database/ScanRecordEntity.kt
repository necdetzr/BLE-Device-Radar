package com.necdetzr.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_records")
data class ScanRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val scanId : Long = 0,
    val scanName:String,
    val timeStamp:Long,
    val deviceCount:Int
)
