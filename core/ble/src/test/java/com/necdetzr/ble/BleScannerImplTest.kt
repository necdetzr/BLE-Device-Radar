package com.necdetzr.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import com.google.common.truth.Truth.assertThat
import com.necdetzr.ble.data.BleScannerImpl
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test


@OptIn(ExperimentalCoroutinesApi::class)
class BleScannerImplTest {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothScanner: BluetoothLeScanner
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var scanner: BleScannerImpl

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        bluetoothAdapter = mockk()
        bluetoothScanner = mockk(relaxed = true)
        bluetoothManager = mockk()

        every { bluetoothManager.adapter } returns bluetoothAdapter
        every { bluetoothAdapter.isEnabled } returns true
        every {
            bluetoothAdapter.bluetoothLeScanner
        } returns bluetoothScanner

        val settingsBuilder = mockk<ScanSettings.Builder>()
        val scanSettings = mockk<ScanSettings>()

        mockkConstructor(ScanSettings.Builder::class)

        every {
            anyConstructed<ScanSettings.Builder>().setScanMode(any())
        } returns settingsBuilder

        every {
            settingsBuilder.build()
        } returns scanSettings

        scanner = BleScannerImpl(
            bluetoothManager = bluetoothManager,
            dispatcher = testDispatcher,
        )
    }

    @Test
    fun `startScanning emits mapped device when scan result arrives`() =
        runTest(testDispatcher) {
            val callbackSlot = slot<ScanCallback>()

            every {
                bluetoothScanner.startScan(
                    null,
                    any<ScanSettings>(),
                    capture(callbackSlot),
                )
            } returns Unit

            val bluetoothDevice = mockk<BluetoothDevice> {
                every { name } returns "Mock Device"
                every { address } returns "AA:BB:CC:DD:EE:FF"
            }
            val scanResult = mockk<ScanResult>(relaxed = true) {
                every { device } returns bluetoothDevice
                every { rssi } returns -65
            }

            val result = async {
                scanner.startScanning().first()
            }

            runCurrent()
            callbackSlot.captured.onScanResult(1, scanResult)
            runCurrent()

            val device = result.await()

            assertThat(device.name).isEqualTo("Mock Device")
            assertThat(device.macAddress)
                .isEqualTo("AA:BB:CC:DD:EE:FF")
            assertThat(device.rssi).isEqualTo(-65)
        }

    @Test
    fun `startScanning fails when bluetooth is unsupported`() =
        runTest(testDispatcher) {
            every { bluetoothManager.adapter } returns null

            val exception = captureScanningException()

            assertThat(exception)
                .hasMessageThat()
                .isEqualTo("Bluetooth is not supported.")
        }

    @Test
    fun `startScanning fails when bluetooth is disabled`() =
        runTest(testDispatcher) {
            every { bluetoothAdapter.isEnabled } returns false

            val exception = captureScanningException()

            assertThat(exception)
                .hasMessageThat()
                .isEqualTo("Bluetooth is disabled.")
        }

    @Test
    fun `startScanning fails when bluetooth scanner is unavailable`() =
        runTest(testDispatcher) {
            every {
                bluetoothAdapter.bluetoothLeScanner
            } returns null

            val exception = captureScanningException()

            assertThat(exception)
                .hasMessageThat()
                .isEqualTo("Bluetooth hardware not available")
        }

    @Test
    fun `onScanFailed closes flow with exception`() =
        runTest(testDispatcher) {
            val callbackSlot = slot<ScanCallback>()

            every {
                bluetoothScanner.startScan(
                    null,
                    any<ScanSettings>(),
                    capture(callbackSlot),
                )
            } returns Unit

            val result = async {
                captureScanningException()
            }

            runCurrent()
            callbackSlot.captured.onScanFailed(7)
            runCurrent()

            val exception = result.await()

            assertThat(exception)
                .hasMessageThat()
                .isEqualTo("Scan failed with error code 7")
        }

    @Test
    fun `cancelling collection stops bluetooth scan`() =
        runTest(testDispatcher) {
            val callbackSlot = slot<ScanCallback>()

            every {
                bluetoothScanner.startScan(
                    null,
                    any<ScanSettings>(),
                    capture(callbackSlot),
                )
            } returns Unit

            val collectionJob = launch {
                scanner.startScanning().collect()
            }

            runCurrent()
            collectionJob.cancelAndJoin()

            verify(exactly = 1) {
                bluetoothScanner.stopScan(callbackSlot.captured)
            }
        }

    private suspend fun captureScanningException(): Throwable? =
        try {
            scanner.startScanning().first()
            null
        } catch (throwable: Throwable) {
            throwable
        }

    @After
    fun tearDown() {
        unmockkAll()
    }
}
