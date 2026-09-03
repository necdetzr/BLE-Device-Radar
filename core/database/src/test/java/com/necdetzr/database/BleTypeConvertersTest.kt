package com.necdetzr.database

import com.google.common.truth.Truth.assertThat
import com.necdetzr.model.BleAdvertisement
import com.necdetzr.model.BleManufacturerData
import com.necdetzr.model.BlePhy
import com.necdetzr.model.BleServiceData
import org.junit.Test

class BleTypeConvertersTest {
    private val converter = BleTypeConverters()

    @Test
    fun `advertisement survives json round trip`() {
        val advertisement = BleAdvertisement(
            txPower = -10,
            isConnectable = true,
            primaryPhy = BlePhy.LE_1M,
            secondaryPhy = BlePhy.LE_2M,
            advertisingSid = 3,
            periodicAdvertisingInterval = 160,
            serviceUuids = listOf("180D", "180F"),
            manufacturerData = listOf(
                BleManufacturerData(
                    companyId = 76,
                    payload = byteArrayOf(1, 2, 3),
                )
            ),
            serviceData = listOf(
                BleServiceData(
                    serviceUuid = "180D",
                    payload = byteArrayOf(4, 5, 6),
                )
            ),
            rawData = byteArrayOf(7, 8, 9),
        )

        val json = converter.fromAdvertisement(advertisement)
        val restoredAdvertisement = converter.toAdvertisement(json)

        assertThat(restoredAdvertisement).isEqualTo(advertisement)
    }

    @Test
    fun `toAdvertisement ignores unknown json fields`() {
        val json = """
        {
            "txPower": -20,
            "isConnectable": true,
            "unknownFutureField": "future value"
        }
    """.trimIndent()

        val advertisement = converter.toAdvertisement(json)

        assertThat(advertisement.txPower).isEqualTo(-20)
        assertThat(advertisement.isConnectable).isTrue()
    }
}
