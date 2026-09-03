package com.necdetzr.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.common.truth.Truth.assertThat
import com.necdetzr.model.SortType
import com.necdetzr.model.ThemeConfig
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.IOException

class BlePreferencesDataSourceTest {

    @get:Rule
    val  temporaryFolder = TemporaryFolder()

    @Test
    fun `userData returns default preferences when datastore is empty`() = runTest {
        val dataSource = createDataSource(backgroundScope)

        val preferences = dataSource.userData.first()

        assertThat(preferences.themeConfig)
            .isEqualTo(ThemeConfig.FOLLOW_SYSTEM)
        assertThat(preferences.sortType)
            .isEqualTo(SortType.BY_NAME)
        assertThat(preferences.scanPeriod)
            .isEqualTo(30_000L)
        assertThat(preferences.rssiRange)
            .isEqualTo(-90)
    }
    @Test
    fun `updateThemeConfig updates theme preference`() = runTest {
        val dataSource = createDataSource(backgroundScope)

        dataSource.updateThemeConfig(ThemeConfig.DARK)

        val preferences = dataSource.userData.first()

        assertThat(preferences.themeConfig)
            .isEqualTo(ThemeConfig.DARK)
    }
    @Test
    fun `updateSortType updates sort preference`() = runTest {
        val dataSource = createDataSource(backgroundScope)

        dataSource.updateSortType(SortType.BY_RSSI)

        val preferences = dataSource.userData.first()

        assertThat(preferences.sortType)
            .isEqualTo(SortType.BY_RSSI)
    }

    @Test
    fun `updateScanPeriod updates scan period preference`() = runTest {
        val dataSource = createDataSource(backgroundScope)

        dataSource.updateScanPeriod(5_000L)

        val preferences = dataSource.userData.first()

        assertThat(preferences.scanPeriod)
            .isEqualTo(5_000L)
    }

    @Test
    fun `updateRssiRange updates rssi preference`() = runTest {
        val dataSource = createDataSource(backgroundScope)

        dataSource.updateRssiRange(-70)

        val preferences = dataSource.userData.first()

        assertThat(preferences.rssiRange)
            .isEqualTo(-70)
    }

    @Test
    fun `userData uses defaults when stored enum values are invalid`() = runTest {
        val dataStore = createDataStore(backgroundScope)

        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("theme_config")] =
                "REMOVED_THEME"

            preferences[stringPreferencesKey("sort_type")] =
                "REMOVED_SORT_TYPE"
        }

        val dataSource = BlePreferencesDataSource(dataStore)
        val preferences = dataSource.userData.first()

        assertThat(preferences.themeConfig)
            .isEqualTo(ThemeConfig.FOLLOW_SYSTEM)
        assertThat(preferences.sortType)
            .isEqualTo(SortType.BY_NAME)
    }

    @Test
    fun `userData returns defaults when datastore throws IOException`() = runTest {
        val dataStore = mockk<DataStore<Preferences>>()

        every { dataStore.data } returns flow {
            throw IOException("Unable to read preferences")
        }

        val dataSource = BlePreferencesDataSource(dataStore)

        val preferences = dataSource.userData.first()

        assertThat(preferences.themeConfig)
            .isEqualTo(ThemeConfig.FOLLOW_SYSTEM)
        assertThat(preferences.sortType)
            .isEqualTo(SortType.BY_NAME)
        assertThat(preferences.scanPeriod)
            .isEqualTo(30_000L)
        assertThat(preferences.rssiRange)
            .isEqualTo(-90)
    }

    @Test
    fun `userData rethrows non IO exceptions`() {
        val dataStore = mockk<DataStore<Preferences>>()

        every { dataStore.data } returns flow {
            throw IllegalStateException("Unexpected failure")
        }

        val dataSource = BlePreferencesDataSource(dataStore)

        val exception = assertThrows(IllegalStateException::class.java) {
            runTest {
                dataSource.userData.first()
            }
        }

        assertThat(exception)
            .hasMessageThat()
            .isEqualTo("Unexpected failure")
    }

    private fun createDataStore(
        scope: CoroutineScope,
    ): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = {
                temporaryFolder.newFile("test.preferences_pb")
            },
        )

    private fun createDataSource(
        scope: CoroutineScope,
    ): BlePreferencesDataSource =
        BlePreferencesDataSource(createDataStore(scope))
}

