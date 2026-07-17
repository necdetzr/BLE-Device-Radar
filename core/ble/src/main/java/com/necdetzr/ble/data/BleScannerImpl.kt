package com.necdetzr.ble.data

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import com.necdetzr.ble.domain.BleScanner
import com.necdetzr.common.network.BleDispatchers
import com.necdetzr.common.network.Dispatcher
import com.necdetzr.common.result.Result
import com.necdetzr.model.BleDevice
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject


const val SCAN_PERIOD = 15000L

class BleScannerImpl @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter,
    @Dispatcher(BleDispatchers.Default) private val dispatcher : CoroutineDispatcher
) : BleScanner {

    private val bluetoothLeScanner : BluetoothLeScanner?
        get() = bluetoothAdapter.bluetoothLeScanner

    @SuppressLint("MissingPermission")
    override fun startScanning(): Flow<Result<BleDevice>> = callbackFlow{
        val scanner = bluetoothLeScanner
        if (scanner == null){
            close(Exception("Bluetooth hardware not available"))
            return@callbackFlow
        }
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType:Int,result: ScanResult?){
                result?.let {
                    val deviceName = try {
                        when {
                            !it.device.name.isNullOrBlank() -> it.device.name
                            !it.scanRecord?.deviceName.isNullOrBlank() -> it.scanRecord?.deviceName
                            else -> "Unknown Device"
                        }
                    } catch (e: SecurityException) {
                        "Unknown Device"
                    }
                    val device = BleDevice(
                        name = deviceName,
                        macAddress = it.device.address,
                        rssi = it.rssi
                    )
                    trySend(Result.Success(device))
                }
            }
            override fun onScanFailed(errorCode:Int){
                close(Exception("Scan failed with error code $errorCode"))
            }
        }
        scanner.startScan(null,settings,callback)
        awaitClose {
            scanner.stopScan(callback)
        }
    }.flowOn(dispatcher)

}
