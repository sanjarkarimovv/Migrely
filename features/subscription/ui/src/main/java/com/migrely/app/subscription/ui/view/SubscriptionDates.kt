package com.migrely.app.subscription.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** A date such as "1 November 2026", in the phone's zone and language. */
@Composable
internal fun longDate(epochMillis: Long): String {
    val locale = LocalConfiguration.current.locales[0]
    return DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
        .withLocale(locale)
        .format(Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()))
}

/** Whether [epochMillis] falls on today's date in the phone's zone. */
internal fun isToday(epochMillis: Long, nowMillis: Long = System.currentTimeMillis()): Boolean {
    val zone = ZoneId.systemDefault()
    return Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate() ==
        Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
}
