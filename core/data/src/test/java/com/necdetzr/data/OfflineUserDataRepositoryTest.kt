package com.necdetzr.data

import com.google.common.truth.Truth.assertThat
import com.necdetzr.data.repository.OfflineUserDataRepository
import com.necdetzr.datastore.BlePreferencesDataSource
import com.necdetzr.model.SortType
import com.necdetzr.model.ThemeConfig
import com.necdetzr.model.UserPreferences
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class OfflineUserDataRepositoryTest {

    private lateinit var dataSource: BlePreferencesDataSource
    private lateinit var repository: OfflineUserDataRepository

    @Before
    fun setUp() {
        dataSource = mockk(relaxed = true)
        repository = OfflineUserDataRepository(dataSource)
    }

    @Test
    fun `userPreferences exposes data source flow`() = runTest {
        val preferences = UserPreferences(
            themeConfig = ThemeConfig.DARK,
            sortType = SortType.BY_RSSI,
            scanPeriod = 5_000L,
            rssiRange = -70,
        )

        every {
            dataSource.userData
        } returns flowOf(preferences)

        repository = OfflineUserDataRepository(dataSource)

        val result = repository.userPreferences.first()

        assertThat(result).isEqualTo(preferences)
    }
    @Test
    fun `preference updates are forwarded to data source`() = runTest {
        repository.setThemeConfig(ThemeConfig.LIGHT)
        repository.setSortType(SortType.BY_NAME)
        repository.setScanPeriod(10_000L)
        repository.setRssiRange(-65)

        coVerify(exactly = 1) {
            dataSource.updateThemeConfig(ThemeConfig.LIGHT)
        }
        coVerify(exactly = 1) {
            dataSource.updateSortType(SortType.BY_NAME)
        }
        coVerify(exactly = 1) {
            dataSource.updateScanPeriod(10_000L)
        }
        coVerify(exactly = 1) {
            dataSource.updateRssiRange(-65)
        }
    }
}
