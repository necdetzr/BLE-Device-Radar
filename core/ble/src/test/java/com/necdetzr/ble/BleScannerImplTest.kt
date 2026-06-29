package com.necdetzr.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import com.necdetzr.ble.data.BleScannerImpl
import com.necdetzr.ble.domain.model.BleDevice
import com.necdetzr.common.result.Result
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.slot
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test



class BleScannerImplTest {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private lateinit var bleScannerImpl: BleScannerImpl
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup(){
        bluetoothAdapter = mockk()
        bluetoothLeScanner = mockk(relaxed = true)
        every {bluetoothAdapter.bluetoothLeScanner} returns bluetoothLeScanner
        val mockBuilder = mockk<ScanSettings.Builder>()
        mockkConstructor(ScanSettings.Builder::class)
        every{anyConstructed<ScanSettings.Builder>().setScanMode(any())} returns mockBuilder
        every { mockBuilder.build() } returns mockk<ScanSettings>()
        bleScannerImpl = BleScannerImpl(bluetoothAdapter,testDispatcher)
    }

    @Test
    fun startScanningReturnsSuccessWhenDeviceFound() = runTest(testDispatcher) {
        val scanCallbackSlot = slot<ScanCallback>()
        every {
            bluetoothLeScanner.startScan(
                null,
                any<ScanSettings>(),
                capture(scanCallbackSlot)
            )
        } returns Unit

        val mockDevice = mockk<BluetoothDevice> {
            every {name} returns "Mock Device"
            every {address} returns "AA:BB:CC:DD:EE:FF"
        }
        val mockScanResult = mockk<ScanResult> {
            every{device} returns mockDevice
            every{rssi} returns -65
        }
        var capturedResult : Result.Success<BleDevice>? = null

        val job = launch {
            val result = bleScannerImpl.startScanning().first()
            capturedResult = result as Result.Success
        }
        runCurrent()
        scanCallbackSlot.captured.onScanResult(1,mockScanResult)
        runCurrent()

        assertEquals("Mock Device",capturedResult?.data?.name)
        assertEquals("AA:BB:CC:DD:EE:FF",capturedResult?.data?.macAddress)
        assertEquals(-65,capturedResult?.data?.rssi)

        job.cancel()
    }
    @Test
    fun startScanningThrowsExceptionWhenScannerIsNull() = runTest(testDispatcher) {
        every{bluetoothAdapter.bluetoothLeScanner} returns null
        try {
            bleScannerImpl.startScanning().first()
        }catch (e: Exception){
            assertEquals("Bluetooth hardware not available", e.message)
        }
    }
    @After
    fun tearDown(){
        unmockkAll()
    }
}
