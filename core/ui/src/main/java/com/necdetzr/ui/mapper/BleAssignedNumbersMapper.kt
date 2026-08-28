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
        val name = gattServices[uuid] ?: memberServices[uuid]

        return if (name != null) {
            "$name ($hex)"
        } else {
            hex
        }
    }

    fun getCompanyName(
        companyId: Int,
        unknownText:String,
    ): String {
        val hex = formatHex(companyId)
        val name = knownCompanies[companyId]

        return if (name != null) {
            "$name ($hex)"
        } else {
            "$unknownText ($hex)"
        }
    }

    private fun extract16BitUuid(uuidString: String): Int? {
        val cleanUuid = uuidString
            .removePrefix("0x")
            .removePrefix("0X")
            .replace("-", "")
            .lowercase()

        return when {
            cleanUuid.length == SHORT_UUID_HEX_LENGTH -> {
                cleanUuid.toIntOrNull(radix = HEX_RADIX)
            }

            cleanUuid.length == FULL_UUID_HEX_LENGTH &&
                    cleanUuid.startsWith(BLUETOOTH_BASE_UUID_PREFIX) &&
                    cleanUuid.substring(BASE_UUID_SUFFIX_START_INDEX) == BLUETOOTH_BASE_UUID_SUFFIX -> {

                cleanUuid
                    .substring(EMBEDDED_UUID_START_INDEX, EMBEDDED_UUID_END_INDEX)
                    .toIntOrNull(radix = HEX_RADIX)
            }

            else -> null
        }
    }

    private fun formatHex(value: Int): String {
        return "0x%04X".format(value)
    }

    private const val BLUETOOTH_BASE_UUID_SUFFIX = "00001000800000805f9b34fb"
    private const val SHORT_UUID_HEX_LENGTH = 4
    private const val FULL_UUID_HEX_LENGTH = 32

    private const val EMBEDDED_UUID_START_INDEX = 4
    private const val EMBEDDED_UUID_END_INDEX = 8
    private const val BASE_UUID_SUFFIX_START_INDEX = 8

    private const val HEX_RADIX = 16

    private const val BLUETOOTH_BASE_UUID_PREFIX = "0000"

}
