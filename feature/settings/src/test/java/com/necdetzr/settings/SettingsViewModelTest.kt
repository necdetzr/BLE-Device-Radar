package com.necdetzr.settings

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.necdetzr.data.repository.ScanHistoryRepository
import com.necdetzr.data.repository.UserDataRepository
import com.necdetzr.model.SortType
import com.necdetzr.model.ThemeConfig
import com.necdetzr.model.UserPreferences
import com.necdetzr.testing.MainDispatcherRule
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var userDataRepository: UserDataRepository
    private lateinit var scanHistoryRepository: ScanHistoryRepository
    private lateinit var viewModel: SettingsViewModel

    private val userPreferences = MutableStateFlow(
        UserPreferences(
            themeConfig = ThemeConfig.LIGHT,
            sortType = SortType.BY_RSSI,
            scanPeriod = 5_000L,
            rssiRange = -70,
        )
    )
    @Before
    fun setUp(){
        userDataRepository = mockk(relaxed = true)
        scanHistoryRepository = mockk(relaxed = true)

        every {
            userDataRepository.userPreferences
        } returns userPreferences

        viewModel = SettingsViewModel(
            userDataRepository = userDataRepository,
            scanHistoryRepository = scanHistoryRepository,
        )
    }
    @Test
    fun `initial state is loading`() {
        assertThat(viewModel.settingsUiState.value.isLoading).isTrue()
    }
    @Test
    fun `repository preferences are mapped to loaded ui state`() = runTest {
        viewModel.settingsUiState.test {
            val initialState = awaitItem()

            assertThat(initialState.isLoading).isTrue()

            runCurrent()

            val loadedState = awaitItem()

            assertThat(loadedState.isLoading).isFalse()
            assertThat(loadedState.theme).isEqualTo(ThemeConfig.LIGHT)
            assertThat(loadedState.scanPeriod).isEqualTo(5_000L)
            assertThat(loadedState.rssi).isEqualTo(-70)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `preference changes update ui state`() = runTest {
        viewModel.settingsUiState.test {
            awaitItem()
            runCurrent()
            awaitItem()

            userPreferences.value = UserPreferences(
                themeConfig = ThemeConfig.DARK,
                sortType = SortType.BY_NAME,
                scanPeriod = 10_000L,
                rssiRange = -60,
            )
            runCurrent()

            val updatedState = awaitItem()

            assertThat(updatedState.isLoading).isFalse()
            assertThat(updatedState.theme).isEqualTo(ThemeConfig.DARK)
            assertThat(updatedState.scanPeriod).isEqualTo(10_000L)
            assertThat(updatedState.rssi).isEqualTo(-60)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setRssiRange updates repository`() = runTest {
        viewModel.setRssiRange(-65)

        runCurrent()

        coVerify(exactly = 1) {
            userDataRepository.setRssiRange(-65)
        }
    }

    @Test
    fun `setScanPeriod updates repository`() = runTest {
        viewModel.setScanPeriod(15_000L)

        runCurrent()

        coVerify(exactly = 1) {
            userDataRepository.setScanPeriod(15_000L)
        }
    }

    @Test
    fun `setTheme updates repository`() = runTest {
        viewModel.setTheme(ThemeConfig.DARK)

        runCurrent()

        coVerify(exactly = 1) {
            userDataRepository.setThemeConfig(ThemeConfig.DARK)
        }
    }

    @Test
    fun `deleteAllScans deletes scan history`() = runTest {
        viewModel.deleteAllScans()

        runCurrent()

        coVerify(exactly = 1) {
            scanHistoryRepository.deleteAllScans()
        }
    }
}
