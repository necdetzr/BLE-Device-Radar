package com.necdetzr.radar

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.necdetzr.data.repository.BleRadarRepository
import com.necdetzr.data.repository.ScanHistoryRepository
import com.necdetzr.data.repository.UserDataRepository
import com.necdetzr.model.ScannedBleDevice
import com.necdetzr.model.SortType
import com.necdetzr.model.ThemeConfig
import com.necdetzr.model.UserPreferences
import com.necdetzr.testing.MainDispatcherRule
import com.necdetzr.ui.DeviceFeedUiState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds


@OptIn(ExperimentalCoroutinesApi::class)
class RadarViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var bleRadarRepository: BleRadarRepository
    private lateinit var userDataRepository: UserDataRepository
    private lateinit var scanHistoryRepository: ScanHistoryRepository
    private lateinit var viewModel: RadarViewModel

    private lateinit var scanResults: MutableSharedFlow<ScannedBleDevice>

    @Before
    fun setUp(){
        bleRadarRepository = mockk(relaxed = true)
        userDataRepository = mockk(relaxed = true)
        scanHistoryRepository = mockk(relaxed = true)
        scanResults = MutableSharedFlow()
        every {
            bleRadarRepository.startScanning()
        } returns scanResults
        every {
            userDataRepository.userPreferences
        } returns flowOf(
            UserPreferences(
                themeConfig = ThemeConfig.FOLLOW_SYSTEM,
                sortType = SortType.BY_RSSI,
                scanPeriod = 60_000L,
                rssiRange = -80
            )
        )
        viewModel = RadarViewModel(
            bleRadarRepository = bleRadarRepository,
            userDataRepository = userDataRepository,
            scanHistoryRepository = scanHistoryRepository
        )

    }

    @Test
    fun `initial state is idle`(){
        val state = viewModel.uiState.value
        assertThat(state.feedState)
            .isEqualTo(DeviceFeedUiState.Idle)
        assertThat(state.radarMessage).isNull()
        assertThat(state.showAlertDialog).isFalse()
    }
    @Test
    fun `permission denied updates user message`(){
        viewModel.onPermissionDenied()
        assertThat(viewModel.uiState.value.radarMessage)
            .isEqualTo(RadarUserMessage.PermissionDenied)
    }
    @Test
    fun `bluetooth enable denied user message`(){
        viewModel.onBluetoothEnableDenied()
        assertThat(viewModel.uiState.value.radarMessage)
            .isEqualTo(RadarUserMessage.BluetoothEnableDenied)
    }
    @Test
    fun `message shown clears current message`(){
        viewModel.onPermissionDenied()
        viewModel.onUserMessageShown()
        assertThat(viewModel.uiState.value.radarMessage).isNull()
    }
    @Test
    fun `save clicks open save dialog`(){
        viewModel.onSaveClick()
        assertThat(viewModel.uiState.value.showAlertDialog).isTrue()
    }
    @Test
    fun `save dialog dismissed closes save dialog`(){
        viewModel.onSaveClick()
        viewModel.onSaveDialogDismissed()
        assertThat(viewModel.uiState.value.showAlertDialog).isFalse()
    }
    @Test
    fun `start click changes feed state to scanning`() = runTest{
        viewModel.onStartButtonClicked()

        val feedState = viewModel.uiState.value.feedState
        assertThat(feedState)
            .isInstanceOf(DeviceFeedUiState.Scanning::class.java)
        assertThat((feedState as DeviceFeedUiState.Scanning).devices)
            .isEmpty()
        viewModel.onStopButtonClicked()
        runCurrent()
    }
    @Test
    fun `scanned device is added to scanning feed`() = runTest {
        val device = createDevice(
            macAddress = "AA:BB:CC:DD:EE:FF",
            name = "Test Device",
            rssi = -40
        )
        viewModel.onStartButtonClicked()
        runCurrent()

        scanResults.emit(device)
        runCurrent()
        val feedState = viewModel.uiState.value.feedState as DeviceFeedUiState.Scanning

        assertThat(feedState.devices).containsExactly(device)
        viewModel.onStopButtonClicked()
        runCurrent()
    }
    @Test
    fun `devices are filtered and sorted by rssi`() = runTest{
        val weakDevice = createDevice(
            macAddress = "AA:AA:AA:AA:AA:AA",
            name = "Weak",
            rssi = -90,
        )
        val mediumDevice = createDevice(
            macAddress = "BB:BB:BB:BB:BB:BB",
            name = "Medium",
            rssi = -65,
        )
        val strongDevice = createDevice(
            macAddress = "CC:CC:CC:CC:CC:CC",
            name = "Strong",
            rssi = -40,
        )

        viewModel.onStartButtonClicked()
        runCurrent()
        scanResults.emit(weakDevice)
        scanResults.emit(mediumDevice)
        scanResults.emit(strongDevice)
        runCurrent()
        val feedState = viewModel.uiState.value.feedState as DeviceFeedUiState.Scanning

        assertThat(feedState.devices)
            .containsExactly(strongDevice,mediumDevice)
            .inOrder()

        viewModel.onStopButtonClicked()
        runCurrent()
    }
    @Test
    fun `same device packets are merged`() = runTest {
        val firstPacket = createDevice(
            macAddress = "AA:BB:CC:DD:EE:FF",
            name = "Known Device",
            rssi = -60,
            firstSeenAt = 1_000L,
            lastSeenAt = 1_000L,
        )
        val secondPacket = createDevice(
            macAddress = "AA:BB:CC:DD:EE:FF",
            name = null,
            rssi = -45,
            firstSeenAt = 2_000L,
            lastSeenAt = 3_000L,
        )
        viewModel.onStartButtonClicked()
        runCurrent()

        scanResults.emit(firstPacket)
        scanResults.emit(secondPacket)
        runCurrent()
        val feedState =
            viewModel.uiState.value.feedState as DeviceFeedUiState.Scanning
        val mergedDevice = feedState.devices.single()
        assertThat(mergedDevice.macAddress)
            .isEqualTo(firstPacket.macAddress)
        assertThat(mergedDevice.name)
            .isEqualTo("Known Device")
        assertThat(mergedDevice.rssi)
            .isEqualTo(-45)
        assertThat(mergedDevice.firstSeenAt)
            .isEqualTo(1_000L)
        assertThat(mergedDevice.lastSeenAt)
            .isEqualTo(3_000L)
        assertThat(mergedDevice.packetCount)
            .isEqualTo(2)
        viewModel.onStopButtonClicked()
        runCurrent()
    }
    @Test
    fun `scan complete when scan period expires`() = runTest {
        every {
            userDataRepository.userPreferences
        } returns flowOf(
            testUserPreferences.copy(
                scanPeriod = 1_000L,
            )
        )
        viewModel.onStartButtonClicked()
        runCurrent()
        assertThat(viewModel.uiState.value.feedState)
            .isInstanceOf(DeviceFeedUiState.Scanning::class.java)
        advanceTimeBy(1_001L.milliseconds)
        runCurrent()
        assertThat(viewModel.uiState.value.feedState)
            .isInstanceOf(DeviceFeedUiState.Success::class.java)
    }
    @Test
    fun `scan failure returns to idle and shows error message`() = runTest{
        every {
            bleRadarRepository.startScanning()
        } returns flow {
            throw IllegalStateException("Test scan failure")
        }
        viewModel.onStartButtonClicked()
        runCurrent()

        val state = viewModel.uiState.value

        assertThat(state.feedState)
            .isEqualTo(DeviceFeedUiState.Idle)
        assertThat(state.radarMessage)
            .isEqualTo(RadarUserMessage.ScanFailed)
    }
    @Test
    fun `stop click completes scan with collected devices`() = runTest {
        val device = createDevice(
            macAddress = "AA:BB:CC:DD:EE:FF",
            name = "Test Device",
            rssi = -50,
        )
        viewModel.onStartButtonClicked()
        runCurrent()
        scanResults.emit(device)
        runCurrent()
        viewModel.onStopButtonClicked()
        runCurrent()

        val feedState = viewModel.uiState.value.feedState as DeviceFeedUiState.Success
        assertThat(feedState.devices).containsExactly(device)
        scanResults.emit(
            createDevice(
                macAddress = "11:22:33:44:55:66",
                name = "Late Device",
                rssi = -40,
            )
        )
        runCurrent()
        assertThat(viewModel.uiState.value.feedState).isEqualTo(feedState)
    }
    @Test
    fun `save stores completed scan and closes dialog`() = runTest {
        val device = createDevice(
            macAddress = "AA:BB:CC:DD:EE:FF",
            name = "Test Device",
            rssi = -50,
        )

        viewModel.onStartButtonClicked()
        runCurrent()
        scanResults.emit(device)
        viewModel.onStopButtonClicked()
        runCurrent()

        viewModel.onSaveClick()
        viewModel.onSaveRecordClick("   My Scan  ")
        runCurrent()

        coVerify(exactly = 1) {
            scanHistoryRepository.saveFullScan(
                name = "My Scan",
                devices = listOf(device)
            )
        }
        val state = viewModel.uiState.value

        assertThat(state.showAlertDialog).isFalse()
        assertThat(state.saveButtonEnabled).isFalse()
    }
    @Test
    fun `repeated save clicks create only one record`() = runTest {
        val device = createDevice(
            macAddress = "AA:BB:CC:DD:EE:FF",
            name = "Test Device",
            rssi = -50,
        )
        coEvery {
            scanHistoryRepository.saveFullScan(any(),any())
        }coAnswers {
            delay(100L.milliseconds)
        }
        viewModel.onStartButtonClicked()
        runCurrent()
        scanResults.emit(device)
        runCurrent()
        viewModel.onStopButtonClicked()
        runCurrent()

        viewModel.onSaveRecordClick("My Scan")
        viewModel.onSaveRecordClick("My Scan")
        advanceUntilIdle()
        coVerify(exactly = 1) {
            scanHistoryRepository.saveFullScan(
                name = "My Scan",
                devices = listOf(device),
            )
        }
    }
    @Test
    fun `failed save allows retry`() = runTest {
        val device = createDevice(
            macAddress = "AA:BB:CC:DD:EE:FF",
            name = "Test Device",
            rssi = -50,
        )
        viewModel.onStartButtonClicked()
        runCurrent()
        scanResults.emit(device)
        runCurrent()
        viewModel.onStopButtonClicked()
        runCurrent()

        viewModel.onSaveClick()

        coEvery {
            scanHistoryRepository.saveFullScan(any(), any())
        } throws IllegalStateException("Test save failure")
        viewModel.onSaveRecordClick("My Scan")
        runCurrent()

        val failedState = viewModel.uiState.value
        assertThat(failedState.radarMessage).
                isEqualTo(RadarUserMessage.SaveFailed)

        assertThat(failedState.saveButtonEnabled).isTrue()
        assertThat(failedState.showAlertDialog).isTrue()

        coEvery {
            scanHistoryRepository.saveFullScan(any(), any())
        } coAnswers { Unit }
        viewModel.onUserMessageShown()
        viewModel.onSaveRecordClick("My Scan")
        runCurrent()

        coVerify(exactly = 2) {
            scanHistoryRepository.saveFullScan(
                name = "My Scan",
                devices = listOf(device),
            )
        }
        val savedState = viewModel.uiState.value

        assertThat(savedState.saveButtonEnabled).isFalse()
        assertThat(savedState.showAlertDialog).isFalse()
        assertThat(savedState.radarMessage).isNull()
    }
    @Test
    fun `device selection opens detail and dismiss clears selection`() = runTest {
        val device = createDevice(
            macAddress = "AA:BB:CC:DD:EE:FF",
            name = "Test Device",
            rssi = -50,
        )

        viewModel.onStartButtonClicked()
        runCurrent()

        scanResults.emit(device)
        runCurrent()

        viewModel.selectedDevice.test {
            assertThat(awaitItem()).isNull()
            viewModel.onDeviceSelected(device)
            runCurrent()

            assertThat(awaitItem()).isEqualTo(device)

            viewModel.onSheetDismissed()
            runCurrent()

            assertThat(awaitItem()).isNull()
        }
        viewModel.onStopButtonClicked()
        runCurrent()
    }

}
private fun createDevice(
    macAddress: String,
    name: String?,
    rssi: Int,
    firstSeenAt: Long = 1_000L,
    lastSeenAt: Long = firstSeenAt,
): ScannedBleDevice {
    return ScannedBleDevice(
        macAddress = macAddress,
        name = name,
        rssi = rssi,
        firstSeenAt = firstSeenAt,
        lastSeenAt = lastSeenAt,
    )
}
private val testUserPreferences = UserPreferences(
    themeConfig = ThemeConfig.FOLLOW_SYSTEM,
    sortType = SortType.BY_RSSI,
    scanPeriod = 60_000L,
    rssiRange = -80,
)
