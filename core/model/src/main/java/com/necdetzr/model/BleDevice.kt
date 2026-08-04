package com.necdetzr.model

import kotlinx.serialization.Serializable

@Serializable
data class ScannedBleDevice(
    val macAddress: String,
    val name: String?,
    val rssi: Int,
    val advertisement: BleAdvertisement = BleAdvertisement(),
    val firstSeenAt: Long,
    val lastSeenAt: Long = firstSeenAt,
    val packetCount: Int = 1
)
@Serializable
data class BleAdvertisement(
    val txPower: Int? = null,
    val isConnectable: Boolean? = null,

    val primaryPhy: BlePhy = BlePhy.UNKNOWN,
    val secondaryPhy: BlePhy = BlePhy.UNKNOWN,

    val advertisingSid: Int? = null,
    val periodicAdvertisingInterval: Int? = null,

    val serviceUuids: List<String> = emptyList(),
    val manufacturerData: List<BleManufacturerData> = emptyList(),
    val serviceData: List<BleServiceData> = emptyList(),

    val rawData: ByteArray = byteArrayOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BleAdvertisement

        if (txPower != other.txPower) return false
        if (isConnectable != other.isConnectable) return false
        if (advertisingSid != other.advertisingSid) return false
        if (periodicAdvertisingInterval != other.periodicAdvertisingInterval) return false
        if (primaryPhy != other.primaryPhy) return false
        if (secondaryPhy != other.secondaryPhy) return false
        if (serviceUuids != other.serviceUuids) return false
        if (manufacturerData != other.manufacturerData) return false
        if (serviceData != other.serviceData) return false
        if (!rawData.contentEquals(other.rawData)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = txPower ?: 0
        result = 31 * result + (isConnectable?.hashCode() ?: 0)
        result = 31 * result + (advertisingSid ?: 0)
        result = 31 * result + (periodicAdvertisingInterval ?: 0)
        result = 31 * result + primaryPhy.hashCode()
        result = 31 * result + secondaryPhy.hashCode()
        result = 31 * result + serviceUuids.hashCode()
        result = 31 * result + manufacturerData.hashCode()
        result = 31 * result + serviceData.hashCode()
        result = 31 * result + rawData.contentHashCode()
        return result
    }
}
@Serializable
data class BleManufacturerData(
    val companyId: Int,
    val payload: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BleManufacturerData

        if (companyId != other.companyId) return false
        if (!payload.contentEquals(other.payload)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = companyId
        result = 31 * result + payload.contentHashCode()
        return result
    }
}
@Serializable
data class BleServiceData(
    val serviceUuid: String,
    val payload: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BleServiceData

        if (serviceUuid != other.serviceUuid) return false
        if (!payload.contentEquals(other.payload)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = serviceUuid.hashCode()
        result = 31 * result + payload.contentHashCode()
        return result
    }
}
@Serializable
enum class BlePhy {
    LE_1M,
    LE_2M,
    LE_CODED,
    UNUSED,
    UNKNOWN
}
