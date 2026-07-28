package com.necdetzr.ui.mapper

object BleUuidMapper {
    private val knownServices = mapOf(
        "180D" to "Heart Rate",
        "180F" to "Battery Service",
        "180A" to "Device Information",
        "1805" to "Current Time",
        "1800" to "Generic Access",
        "1801" to "Generic Attribute",
        "1812" to "Human Interface Device (HID)",
        "FD6F" to "Apple Proximity / Exposure Notification",
        "FE2C" to "Google Fast Pair Service",
        "FE9F" to "Google Fast Pair Service",
        "FD5A" to "Samsung Electronics Co., Ltd.",
        "FD69" to "Samsung Electronics Co., Ltd.",
        "FE3B" to "Dolby Laboratories / Sony",
        "FE35" to "HTC Corporation",
        "FE9E" to "Intel Corp.",
        "FE9B" to "Samsara Networks",
        "FE22" to "Zimmer Biomet",
        "FEE8" to "QCY / Quintic Audio Service",
        "FEE9" to "QCY / Fast Pair & Config",
        "FE08" to "Microsoft Corporation",
        "FE09" to "Microsoft Corporation"
    )
    private val knownCompanies = mapOf(
        0x004C to "Apple, Inc.",
        0x0075 to "Samsung Electronics Co. Ltd.",
        0x00E0 to "Google LLC",
        0x0006 to "Microsoft",
        0x002D to "Sony Corporation",
        0x000A to "Qualcomm Technologies",
        0x0059 to "Nordic Semiconductor ASA",
        0x01DA to "Logitech Europe S.A.",
        0x038F to "Xiaomi Inc.",
        0x003A to "Anker Innovations Limited"
    )
    fun getReadableName(uuidString: String): String {
        val short16BitHex = extract16BitUuid(uuidString)?.uppercase() ?: return uuidString
        val name = knownServices[short16BitHex]

        return if (name != null) {
            "$name (0x$short16BitHex)"
        } else {
            uuidString
        }
    }
    private fun extract16BitUuid(uuid: String): String? {
        val cleanUuid = uuid.replace("-", "").lowercase()
        return when (cleanUuid.length) {
            32 if cleanUuid.endsWith("00805f9b34fb") -> {
                cleanUuid.substring(4, 8)
            }
            4 -> {
                cleanUuid
            }
            else -> {
                null
            }
        }
    }
    fun getCompanyName(companyId: Int): String {
        val name = knownCompanies[companyId]
        val hexString = String.format("0x%04X", companyId)
        return name?.let { "$it ($hexString)" } ?: "Unknown ($hexString)"
    }
}
