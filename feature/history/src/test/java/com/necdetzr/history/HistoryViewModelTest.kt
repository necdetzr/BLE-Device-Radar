package com.necdetzr.history

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.necdetzr.data.repository.ScanHistoryRepository
import com.necdetzr.model.ScanRecord
import com.necdetzr.model.ScanRecordDetail
import com.necdetzr.model.ScannedBleDevice
import com.necdetzr.testing.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: ScanHistoryRepository
    private lateinit var recentScansFlow: MutableStateFlow<List<ScanRecord>>
    private lateinit var totalScansFlow: MutableStateFlow<Int>
    private lateinit var viewModel: HistoryViewModel

    @Before
    fun setUp(){
        repository = mockk(relaxed = true)
        recentScansFlow = MutableStateFlow(emptyList())
        totalScansFlow = MutableStateFlow(0)

        every {
            repository.getRecentScans()
        } returns recentScansFlow
        every {
            repository.getTotalScanCount()
        } returns totalScansFlow

        viewModel = HistoryViewModel(
            historyRepository = repository
        )
    }

    @Test
    fun `initial state has no selected scan or device`(){
        val state = viewModel.uiState.value

        assertThat(state.selectedDevice).isNull()
        assertThat(state.selectedScan).isNull()
    }

    @Test
    fun `recent scans follow repository updates`() = runTest{
        val scan = createScanRecord()

        viewModel.recentScans.test {
            assertThat(awaitItem()).isEmpty()

            recentScansFlow.value = listOf(scan)

            assertThat(awaitItem()).containsExactly(scan)
        }
    }

    @Test
    fun `total scan count follows repository updates`() = runTest {
        viewModel.totalScans.test {
            assertThat(awaitItem()).isEqualTo(0)

            totalScansFlow.value = 5

            assertThat(awaitItem()).isEqualTo(5)
        }
    }

    @Test
    fun `scan click loads selected scan detail`() = runTest {
        val detail = createScanDetail()

        every {
            repository.getScanWithDevices(detail.scan.scanId)
        } returns flowOf(detail)

        viewModel.onScanClick(detail.scan.scanId)
        runCurrent()

        assertThat(viewModel.uiState.value.selectedScan)
            .isEqualTo(detail)
        assertThat(viewModel.uiState.value.selectedDevice)
            .isNull()
    }

    @Test
    fun `device click selects device and clears it`(){
        val device = createDevice()

        viewModel.onDeviceClick(device)

        assertThat(viewModel.uiState.value.selectedDevice)
            .isEqualTo(device)

        viewModel.onDeviceDetailBack()

        assertThat(viewModel.uiState.value.selectedDevice)
            .isNull()
    }
    @Test
    fun `sheet dismissed clears scan and device selection`() = runTest {
        val detail = createScanDetail()
        val device = detail.devices.first()

        every {
            repository.getScanWithDevices(detail.scan.scanId)
        } returns flowOf(detail)

        viewModel.onScanClick(detail.scan.scanId)
        runCurrent()
        viewModel.onDeviceClick(device)

        viewModel.onSheetDismissed()

        val state = viewModel.uiState.value

        assertThat(state.selectedScan).isNull()
        assertThat(state.selectedDevice).isNull()
    }

    @Test
    fun `dismissing sheet cancels pending scan detail load`() = runTest {
        val detail = createScanDetail()

        every {
            repository.getScanWithDevices(detail.scan.scanId)
        }returns flow {
            delay(1_000L.milliseconds)
            emit(detail)
        }

        viewModel.onScanClick(detail.scan.scanId)
        runCurrent()

        viewModel.onSheetDismissed()
        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertThat(state.selectedScan).isNull()
        assertThat(state.selectedDevice).isNull()
    }

    @Test
    fun `new scan selection cancels previous detail load`() = runTest {
        val firstDetail = createScanDetail(1L)
        val secondDetail = createScanDetail(2L)

        every {
            repository.getScanWithDevices(1L)
        }returns flow {
            delay(1_000L.milliseconds)
            emit(firstDetail)
        }

        every {
            repository.getScanWithDevices(2L)
        }returns flowOf(secondDetail)

        viewModel.onScanClick(1L)
        runCurrent()

        viewModel.onScanClick(2L)
        runCurrent()

        assertThat(viewModel.uiState.value.selectedScan)
            .isEqualTo(secondDetail)

        advanceUntilIdle()

        assertThat(viewModel.uiState.value.selectedScan)
            .isEqualTo(secondDetail)
    }
}

private fun createScanRecord(
    scanId: Long = 1L,
): ScanRecord {
    return ScanRecord(
        scanId = scanId,
        scanName = "Test Scan",
        timestamp = 1_000L,
        deviceCount = 1
    )
}
private fun createScanDetail(
    scanId: Long = 1L,
) : ScanRecordDetail{
    return ScanRecordDetail(
        scan = createScanRecord(scanId),
        devices = listOf(createDevice())
    )
}
private fun createDevice(): ScannedBleDevice {
    return ScannedBleDevice(
        macAddress = "AA:BB:CC:DD:EE:FF",
        name = "Test Device",
        rssi = -50,
        firstSeenAt = 1_000L
    )
}
