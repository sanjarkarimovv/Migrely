package com.migrely.app.subscription.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * A date such as "1 November 2026", in the phone's zone and language, as every formatter of the app
 * reads the zone when called (ARCHITECTURE section 6a). Which day is today is the domain's.
 */
@Composable
internal fun longDate(epochMillis: Long): String {
    val locale = LocalConfiguration.current.locales[0]
    return DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
        .withLocale(locale)
        .format(Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()))
}
