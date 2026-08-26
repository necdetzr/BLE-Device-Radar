package com.necdetzr.ble.data

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import com.necdetzr.ble.domain.BleScanner
import com.necdetzr.ble.mapper.toScannedBleDevice
import com.necdetzr.common.network.BleDispatchers
import com.necdetzr.common.network.Dispatcher
import com.necdetzr.model.ScannedBleDevice
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject


class BleScannerImpl @Inject constructor(
    private val bluetoothManager: BluetoothManager,
    @Dispatcher(BleDispatchers.Default)
    private val dispatcher : CoroutineDispatcher
) : BleScanner {


    @SuppressLint("MissingPermission")
    override fun startScanning(): Flow<ScannedBleDevice> = callbackFlow{
        val adapter = bluetoothManager.adapter
        if(adapter == null){
            close(Exception("Bluetooth is not supported."))
            return@callbackFlow
        }
        if(!adapter.isEnabled){
            close(Exception("Bluetooth is disabled."))
            return@callbackFlow
        }
        val scanner = adapter.bluetoothLeScanner
        if (scanner == null){
            close(Exception("Bluetooth hardware not available"))
            return@callbackFlow
        }
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType:Int,result: ScanResult?){
                result?.let {scanResult->
                    val device = scanResult.toScannedBleDevice()
                    trySend(device)

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
