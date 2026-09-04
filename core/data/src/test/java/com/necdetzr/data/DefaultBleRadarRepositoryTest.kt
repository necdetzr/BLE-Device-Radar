package com.necdetzr.data.repository

import com.google.common.truth.Truth.assertThat
import com.necdetzr.ble.domain.BleScanner
import com.necdetzr.model.ScannedBleDevice
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultBleRadarRepositoryTest {

    @Test
    fun `startScanning exposes scanner flow`() = runTest {
        val scanner = mockk<BleScanner>()
        val device = ScannedBleDevice(
            macAddress = "AA:BB:CC:DD:EE:FF",
            name = "Test Device",
            rssi = -65,
            firstSeenAt = 1_000L,
        )
        every {
            scanner.startScanning()
        } returns flowOf(device)

        val repository = DefaultBleRadarRepository(scanner)

        val result = repository.startScanning().first()

        assertThat(result).isEqualTo(device)
        verify(exactly = 1) {
            scanner.startScanning()
        }
    }
}
