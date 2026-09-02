package com.necdetzr.history

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.necdetzr.data.repository.ScanHistoryRepository
import com.necdetzr.history.search.HistorySearchContentState
import com.necdetzr.history.search.HistorySearchViewModel
import com.necdetzr.history.search.SearchCategory
import com.necdetzr.model.ScanRecord
import com.necdetzr.model.ScanRecordDetail
import com.necdetzr.model.ScannedBleDevice
import com.necdetzr.testing.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class HistorySearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: ScanHistoryRepository
    private lateinit var viewModel: HistorySearchViewModel

    @Before
    fun setUp(){
        repository = mockk()

        every {
            repository.searchScans(any(),any())
        }returns flowOf(emptyList())

        every {
            repository.searchDevices(any(),any())
        }returns flowOf(emptyList())

        viewModel = HistorySearchViewModel(
            scanHistoryRepository = repository
        )
    }

    @Test
    fun `initial state is loading with empty query`(){
        val state = viewModel.uiState.value

        assertThat(state.contentState)
            .isEqualTo(HistorySearchContentState.Loading)
        assertThat(state.query).isEmpty()
        assertThat(state.selectedCategory)
            .isEqualTo(SearchCategory.ALL)
        assertThat(state.selectedScan).isNull()
        assertThat(state.selectedDevice).isNull()
        assertThat(state.expandedDeviceMac).isNull()
    }
    @Test
    fun `empty search results change loading state to empty`() = runTest {
        viewModel.uiState.test {
            assertThat(awaitItem().contentState)
                .isEqualTo(HistorySearchContentState.Loading)

            runCurrent()
            advanceTimeBy(300L.milliseconds)
            runCurrent()

            assertThat(awaitItem().contentState)
                .isEqualTo(HistorySearchContentState.Empty)
        }
    }

    @Test
    fun `blank query requests ten scans and ten devices`() = runTest {
        viewModel.uiState.test {
            awaitItem()

            runCurrent()
            advanceTimeBy(300L.milliseconds)
            runCurrent()

            assertThat(awaitItem().contentState)
                .isEqualTo(HistorySearchContentState.Empty)

            verify(exactly = 1){
                repository.searchScans(
                    query = "",
                    limit = 10
                )
            }
            verify(exactly = 1){
                repository.searchDevices(
                    query = "",
                    limit = 10
                )
            }

        }
    }

    @Test
    fun `search waits until user stops typing for 300 milliseconds`() = runTest {
        viewModel.uiState.test {
            awaitItem()

            runCurrent()
            advanceTimeBy(300L.milliseconds)
            runCurrent()
            awaitItem()

            viewModel.onQueryChange("nec")
            runCurrent()

            assertThat(awaitItem().query).isEqualTo(("nec"))

            advanceTimeBy(200L.milliseconds)

            viewModel.onQueryChange("necdet")
            runCurrent()

            assertThat(awaitItem().query).isEqualTo("necdet")

            advanceTimeBy(299L.milliseconds)
            runCurrent()

            verify(exactly = 0) {
                repository.searchScans(query = "necdet", limit = 30)
            }
            verify(exactly = 0) {
                repository.searchDevices(query = "necdet", limit = 30)
            }

            advanceTimeBy(1L.milliseconds)
            runCurrent()

            verify(exactly = 1) {
                repository.searchScans(query = "necdet", limit = 30)
            }
            verify(exactly = 1) {
                repository.searchDevices(query = "necdet", limit = 30)
            }

            verify(exactly = 0) {
                repository.searchScans(query = "nec", limit = any())
            }
            verify(exactly = 0) {
                repository.searchDevices(query = "nec", limit = any())
            }
        }
    }

    @Test
    fun `search trims query and uses limit thirty`() = runTest {
        viewModel.uiState.test {
            awaitItem()

            runCurrent()
            advanceTimeBy(300L.milliseconds)
            runCurrent()
            awaitItem()

            viewModel.onQueryChange("  necdet  ")
            runCurrent()

            assertThat(awaitItem().query).isEqualTo("  necdet  ")

            advanceTimeBy(300L.milliseconds)
            runCurrent()

            verify(exactly = 1){
                repository.searchScans(query = "necdet", limit = 30)
            }
            verify(exactly = 1) {
                repository.searchDevices(query = "necdet", limit = 30)
            }

            verify(exactly = 0) {
                repository.searchDevices(query = "  necdet  ", limit = any())
            }
            verify(exactly = 0) {
                repository.searchScans(query = "  necdet  ", limit = any())
            }
        }
    }

    @Test
    fun `scan category searchs only scan`() = runTest {
        viewModel.onCategoryClick(SearchCategory.SCAN)

        viewModel.uiState.test {
            awaitItem()

            runCurrent()
            advanceTimeBy(300L.milliseconds)
            runCurrent()

            val state = expectMostRecentItem()

            assertThat(state.selectedCategory)
                .isEqualTo(SearchCategory.SCAN)
            assertThat(state.contentState)
                .isEqualTo(HistorySearchContentState.Empty)

            verify(exactly = 1){
                repository.searchScans(query = "", limit = 10)
            }
            verify(exactly = 0){
                repository.searchDevices(query = "", limit = 10)
            }
        }
    }
    @Test
    fun `device category searches only devices`() = runTest {
        viewModel.onCategoryClick(SearchCategory.DEVICE)

        viewModel.uiState.test {
            awaitItem()
            runCurrent()
            advanceTimeBy(300L)
            runCurrent()

            val state = expectMostRecentItem()

            assertThat(state.selectedCategory)
                .isEqualTo(SearchCategory.DEVICE)
            assertThat(state.contentState)
                .isEqualTo(HistorySearchContentState.Empty)

            verify(exactly = 1) {
                repository.searchDevices(
                    query = "",
                    limit = 10,
                )
            }

            verify(exactly = 0) {
                repository.searchScans(any(), any())
            }
        }
    }

    @Test
    fun `scan results produce success state`() = runTest {
        val scan = ScanRecord(
            scanId = 1L,
            scanName = "Test Scan",
            timestamp = 1_000L,
            deviceCount = 2,
        )

        every {
            repository.searchScans(any(),any())
        }returns flowOf(listOf(scan))

        viewModel.uiState.test {
            assertThat(awaitItem().contentState)
                .isEqualTo(HistorySearchContentState.Loading)

            runCurrent()
            advanceTimeBy(300L.milliseconds)
            runCurrent()

            val contentState = expectMostRecentItem().contentState

            assertThat(contentState)
                .isInstanceOf(HistorySearchContentState.Success::class.java)

            val success = contentState as HistorySearchContentState.Success

            assertThat(success.scans).containsExactly(scan)
            assertThat(success.devices).isEmpty()
        }
    }
    @Test
    fun `search failure produces error state`() = runTest {
        every {
            repository.searchScans(any(), any())
        } returns flow {
            throw IllegalStateException("Test search failure")
        }

        viewModel.uiState.test {
            assertThat(awaitItem().contentState)
                .isEqualTo(HistorySearchContentState.Loading)

            runCurrent()
            advanceTimeBy(300L.milliseconds)
            runCurrent()

            assertThat(expectMostRecentItem().contentState)
                .isEqualTo(HistorySearchContentState.Error)
        }
    }
    @Test
    fun `new query can recover after search failure`() = runTest {
        val scan = ScanRecord(
            scanId = 1L,
            scanName = "Office",
            timestamp = 1_000L,
            deviceCount = 2,
        )
        every {
            repository.searchScans(query = "", limit = 10)
        } returns flow {
            throw IllegalStateException("Test search failure")
        }
        every {
            repository.searchScans(query = "Office", limit = 30)
        } returns flowOf(listOf(scan))

        viewModel.uiState.test {
            awaitItem()

            runCurrent()
            advanceTimeBy(300L.milliseconds)
            runCurrent()
            assertThat(expectMostRecentItem().contentState)
                .isEqualTo(HistorySearchContentState.Error)
            viewModel.onQueryChange("Office")
            runCurrent()

            advanceTimeBy(300L.milliseconds)
            runCurrent()

            val state = expectMostRecentItem()

            assertThat(state.query).isEqualTo("Office")
            assertThat(state.contentState).isEqualTo(
                HistorySearchContentState.Success(
                    scans = listOf(scan),
                    devices = emptyList(),
                )
            )
        }
    }

    @Test
    fun  `new query cancels previous search`() = runTest{
        val oldScan = ScanRecord(
            scanId = 1L,
            scanName = "Old",
            timestamp = 1_000L,
            deviceCount = 1,
        )
        val newScan = oldScan.copy(
            scanId = 2L,
            scanName = "New",
        )
        var oldSearchCancelled = false

        every {
            repository.searchScans(query = "old", limit = 30)
        } returns flow {
            try {
                delay(1_000L.milliseconds)
                emit(listOf(oldScan))
            } finally {
                oldSearchCancelled = true
            }
        }
        every {
            repository.searchScans(query = "new", limit = 30)
        } returns flowOf(listOf(newScan))

        viewModel.uiState.test {
            awaitItem()

            runCurrent()
            advanceTimeBy(300L.milliseconds)
            runCurrent()
            awaitItem()

            viewModel.onQueryChange("old")
            runCurrent()
            assertThat(expectMostRecentItem().query).isEqualTo("old")

            advanceTimeBy(300L.milliseconds)
            runCurrent()

            viewModel.onQueryChange("new")
            runCurrent()
            assertThat(expectMostRecentItem().query).isEqualTo("new")

            advanceTimeBy(300L.milliseconds)
            runCurrent()

            assertThat(oldSearchCancelled).isTrue()

            val expectedContent = HistorySearchContentState.Success(
                scans = listOf(newScan),
                devices = emptyList(),
            )

            assertThat(expectMostRecentItem().contentState)
                .isEqualTo(expectedContent)


            advanceTimeBy(1_000L.milliseconds)
            runCurrent()

            expectNoEvents()

            assertThat(viewModel.uiState.value.contentState)
                .isEqualTo(expectedContent)
        }
    }
    @Test
    fun `expanding device loads history and second click collapses it`() = runTest {
        val macAddress = "AA:BB:CC:DD:EE:FF"
        val scan = ScanRecord(
            scanId = 1L,
            scanName = "Office",
            timestamp = 1_000L,
            deviceCount = 1,
        )

        every {
            repository.getScansForDevice(macAddress)
        } returns flowOf(listOf(scan))

        viewModel.uiState.test {
            awaitItem()

            runCurrent()
            advanceTimeBy(300L.milliseconds)
            runCurrent()
            awaitItem()

            viewModel.onDeviceExpandClick(macAddress)
            runCurrent()

            val expandedState = expectMostRecentItem()

            assertThat(expandedState.expandedDeviceMac)
                .isEqualTo(macAddress)
            assertThat(expandedState.expandedDeviceScans)
                .containsExactly(scan)

            viewModel.onDeviceExpandClick(macAddress)
            runCurrent()

            val collapsedState = expectMostRecentItem()

            assertThat(collapsedState.expandedDeviceMac).isNull()
            assertThat(collapsedState.expandedDeviceScans).isEmpty()

            verify(exactly = 1) {
                repository.getScansForDevice(macAddress)
            }
        }
    }
    @Test
    fun `expanding another device cancels previous history load`() = runTest {
        val firstMac = "AA:AA:AA:AA:AA:AA"
        val secondMac = "BB:BB:BB:BB:BB:BB"

        val firstScan = ScanRecord(
            scanId = 1L,
            scanName = "First Scan",
            timestamp = 1_000L,
            deviceCount = 1,
        )
        val secondScan = firstScan.copy(
            scanId = 2L,
            scanName = "Second Scan",
        )

        var firstLoadCancelled = false

        every {
            repository.getScansForDevice(firstMac)
        } returns flow {
            try {
                delay(1_000L)
                emit(listOf(firstScan))
            } finally {
                firstLoadCancelled = true
            }
        }

        every {
            repository.getScansForDevice(secondMac)
        } returns flowOf(listOf(secondScan))

        viewModel.uiState.test {
            awaitItem()

            runCurrent()
            advanceTimeBy(300L.milliseconds)
            runCurrent()
            awaitItem()

            viewModel.onDeviceExpandClick(firstMac)
            runCurrent()

            val loadingState = expectMostRecentItem()
            assertThat(loadingState.expandedDeviceMac)
                .isEqualTo(firstMac)
            assertThat(loadingState.expandedDeviceScans).isEmpty()

            viewModel.onDeviceExpandClick(secondMac)
            runCurrent()

            val secondState = expectMostRecentItem()

            assertThat(firstLoadCancelled).isTrue()
            assertThat(secondState.expandedDeviceMac)
                .isEqualTo(secondMac)
            assertThat(secondState.expandedDeviceScans)
                .containsExactly(secondScan)

            advanceTimeBy(1_000L.milliseconds)
            runCurrent()

            expectNoEvents()

            assertThat(viewModel.uiState.value.expandedDeviceMac)
                .isEqualTo(secondMac)
            assertThat(viewModel.uiState.value.expandedDeviceScans)
                .containsExactly(secondScan)
        }
    }
    @Test
    fun `sheet dismissal clears selected scan and device`() = runTest {
        val device = ScannedBleDevice(
            macAddress = "AA:BB:CC:DD:EE:FF",
            name = "Test Device",
            rssi = -50,
            firstSeenAt = 1_000L,
        )
        val detail = ScanRecordDetail(
            scan = ScanRecord(
                scanId = 1L,
                scanName = "Office",
                timestamp = 1_000L,
                deviceCount = 1,
            ),
            devices = listOf(device),
        )

        every {
            repository.getScanWithDevices(1L)
        } returns flowOf(detail)

        viewModel.uiState.test {
            awaitItem()

            runCurrent()
            advanceTimeBy(300L.milliseconds)
            runCurrent()
            awaitItem()

            viewModel.onScanClick(1L)
            runCurrent()

            val scanState = expectMostRecentItem()

            assertThat(scanState.selectedScan).isEqualTo(detail)
            assertThat(scanState.selectedDevice).isNull()

            viewModel.onDeviceClick(device)
            runCurrent()

            val deviceState = expectMostRecentItem()

            assertThat(deviceState.selectedScan).isEqualTo(detail)
            assertThat(deviceState.selectedDevice).isEqualTo(device)

            viewModel.onSheetDismissed()
            runCurrent()

            val dismissedState = expectMostRecentItem()

            assertThat(dismissedState.selectedScan).isNull()
            assertThat(dismissedState.selectedDevice).isNull()
        }
    }
    @Test
    fun `sheet dismissal cancels pending scan detail load`() = runTest {
        val detail = ScanRecordDetail(
            scan = ScanRecord(
                scanId = 1L,
                scanName = "Office",
                timestamp = 1_000L,
                deviceCount = 0,
            ),
            devices = emptyList(),
        )

        var loadCancelled = false

        every {
            repository.getScanWithDevices(1L)
        } returns flow {
            try {
                delay(1_000L)
                emit(detail)
            } finally {
                loadCancelled = true
            }
        }

        viewModel.uiState.test {
            awaitItem()

            runCurrent()
            advanceTimeBy(300L.milliseconds)
            runCurrent()
            awaitItem()

            viewModel.onScanClick(1L)
            runCurrent()

            viewModel.onSheetDismissed()
            runCurrent()

            assertThat(loadCancelled).isTrue()

            advanceTimeBy(1_000L.milliseconds)
            runCurrent()

            expectNoEvents()

            val state = viewModel.uiState.value

            assertThat(state.selectedScan).isNull()
            assertThat(state.selectedDevice).isNull()
        }
    }
}
