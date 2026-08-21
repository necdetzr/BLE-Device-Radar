package com.necdetzr.ble.mapper

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.os.ParcelUuid
import android.util.SparseArray
import androidx.annotation.RequiresPermission
import com.necdetzr.model.BleAdvertisement
import com.necdetzr.model.BleManufacturerData
import com.necdetzr.model.BlePhy
import com.necdetzr.model.BleServiceData
import com.necdetzr.model.ScannedBleDevice
import kotlin.collections.map


@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
internal fun ScanResult.toScannedBleDevice(): ScannedBleDevice {
    val record = this.scanRecord
    val now = System.currentTimeMillis()
    val advertisedName = record
        ?.deviceName
        ?.takeUnless(String::isBlank)
    val cachedName = try {
        device?.name?.takeUnless(String::isBlank)
    }catch (_: SecurityException){
        null
    }
    val deviceName = advertisedName ?: cachedName
    val resolvedTxPower = txPower.takeUnless { it == ScanResult.TX_POWER_NOT_PRESENT }
        ?: record?.txPowerLevel?.takeUnless { it == Int.MIN_VALUE }
    return ScannedBleDevice(
        macAddress = device.address,
        name = deviceName,
        rssi = rssi,
        advertisement = BleAdvertisement(
            txPower = resolvedTxPower,
            isConnectable = isConnectable,
            primaryPhy =  primaryPhy.toBlePhy(),
            secondaryPhy = secondaryPhy.toBlePhy(),
            advertisingSid = advertisingSid.takeUnless {
                it == ScanResult.SID_NOT_PRESENT
            },
            periodicAdvertisingInterval =
                periodicAdvertisingInterval.takeUnless {
                    it == ScanResult.PERIODIC_INTERVAL_NOT_PRESENT
                },
            serviceUuids = record
                ?.serviceUuids
                ?.map{it.uuid.toString()}
                .orEmpty(),
            manufacturerData = record
                ?.manufacturerSpecificData
                ?.toManufacturerData()
                .orEmpty(),
            serviceData = record
                ?.serviceData
                ?.toServiceData()
                .orEmpty(),
            rawData = record?.bytes ?: byteArrayOf()
        ),
        firstSeenAt = now

    )
}


private fun Int.toBlePhy(): BlePhy {
    return when (this) {
        BluetoothDevice.PHY_LE_1M -> BlePhy.LE_1M
        BluetoothDevice.PHY_LE_2M -> BlePhy.LE_2M
        BluetoothDevice.PHY_LE_CODED -> BlePhy.LE_CODED
        else -> BlePhy.UNKNOWN
    }
}
private fun Map<ParcelUuid, ByteArray>.toServiceData(): List<BleServiceData> =
    map { (uuid, payload) ->
        BleServiceData(
            serviceUuid = uuid.uuid.toString(),
            payload = payload
        )
    }
private fun SparseArray<ByteArray>.toManufacturerData(): List<BleManufacturerData> =
    buildList {
        for (i in indices) {
            add(
                BleManufacturerData(
                    companyId = keyAt(i),
                    payload = valueAt(i)
                )
            )
        }
    }
