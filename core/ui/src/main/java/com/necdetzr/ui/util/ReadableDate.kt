package com.necdetzr.ui.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun Long.toReadableDateTime(): String {
    val zone = ZoneId.systemDefault()
    val dateTime = Instant.ofEpochMilli(this).atZone(zone)
    val today = LocalDate.now(zone)

    val pattern = if (dateTime.year == today.year) {
        "d MMM, HH:mm"
    } else {
        "d MMM yyyy, HH:mm"
    }

    return dateTime.format(
        DateTimeFormatter.ofPattern(
            pattern,
            Locale.getDefault()
        )
    )
}
