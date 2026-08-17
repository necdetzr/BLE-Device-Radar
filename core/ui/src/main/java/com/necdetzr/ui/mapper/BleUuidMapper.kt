package com.necdetzr.ui.mapper


object BleAssignedNumbersMapper {

    private val gattServices = mapOf(
        0x1800 to "Generic Access",
        0x1801 to "Generic Attribute",
        0x1805 to "Current Time",
        0x180A to "Device Information",
        0x180D to "Heart Rate",
        0x180F to "Battery Service",
        0x1812 to "Human Interface Device (HID)"
    )

    private val memberServices = mapOf(
        0xFD5A to "Samsung Electronics Co., Ltd.",
        0xFD69 to "Samsung Electronics Co., Ltd.",
        0xFD6F to "Apple, Inc.",

        0xFE08 to "Microsoft",
        0xFE09 to "Pillsy, Inc.",
        0xFE22 to "Zoll Medical Corporation",
        0xFE2C to "Google LLC",
        0xFE35 to "HUAWEI Technologies Co., Ltd.",
        0xFE3B to "Dolby Laboratories",
        0xFE9B to "Samsara Networks, Inc.",
        0xFE9E to "Renesas Design Netherlands B.V.",
        0xFE9F to "Google LLC",
        0xFEE8 to "Quintic Corp.",
        0xFEE9 to "Quintic Corp."
    )

    private val knownCompanies = mapOf(
        0x0006 to "Microsoft",
        0x000A to "Qualcomm Technologies",
        0x004C to "Apple, Inc.",
        0x0059 to "Nordic Semiconductor ASA",
        0x0075 to "Samsung Electronics Co. Ltd.",
        0x00E0 to "Google LLC",
        0x01DA to "Logitech International SA",
        0x038F to "Xiaomi Inc.",
        0x0CC2 to "Anker Innovations Limited"
    )

    fun getReadableName(uuidString: String): String {
        val uuid = extract16BitUuid(uuidString)
            ?: return uuidString

        val hex = formatHex(uuid)

        val name = gattServices[uuid]
            ?: memberServices[uuid]
            ?: return hex

        return "$name ($hex)"
    }

    fun getCompanyName(companyId: Int): String {
        val hex = formatHex(companyId)
        val name = knownCompanies[companyId]

        return if (name != null) {
            "$name ($hex)"
        } else {
            "Unknown ($hex)"
        }
    }

    private fun extract16BitUuid(uuidString: String): Int? {
        val cleanUuid = uuidString
            .removePrefix("0x")
            .removePrefix("0X")
            .replace("-", "")
            .lowercase()

        return when {
            cleanUuid.length == 4 -> {
                cleanUuid.toIntOrNull(radix = 16)
            }

            cleanUuid.length == 32 &&
                    cleanUuid.startsWith("0000") &&
                    cleanUuid.substring(8) == BLUETOOTH_BASE_UUID_SUFFIX -> {

                cleanUuid
                    .substring(4, 8)
                    .toIntOrNull(radix = 16)
            }

            else -> null
        }
    }

    private fun formatHex(value: Int): String {
        return "0x%04X".format(value)
    }

    private const val BLUETOOTH_BASE_UUID_SUFFIX =
        "00001000800000805f9b34fb"
}
