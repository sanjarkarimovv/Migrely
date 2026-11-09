package com.migrely.app.designsystem.format

import android.text.format.DateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val SHORT = "EEEMMMd"
private const val SHORT_WITH_YEAR = "EEEMMMdy"
private const val SPOKEN = "EEEEMMMMdy"
private const val MONTH_DAY = "MMMd"
private const val MONTH_DAY_WITH_YEAR = "MMMdy"

/**
 * A day written as the locale writes it, never with a fixed pattern (BRD EC-TIM-05, design D5): short, as "Fri, Nov 20"
 * in en-US and "Fri 20 Nov" in en-GB, with the year when it is not [today]'s; and spoken in full, as "Friday, November
 * 20, 2026", for screen readers. The weekday is there because a visit is planned by it.
 */
object MigrelyDateFormat {
    fun short(date: LocalDate, today: LocalDate, locale: Locale): String =
        format(date, if (date.year == today.year) SHORT else SHORT_WITH_YEAR, locale)

    fun spoken(date: LocalDate, locale: Locale): String = format(date, SPOKEN, locale)

    /** Without the weekday, as "Aug 20", with the year when it is not [today]'s: for a short label such as a chip. */
    fun monthDay(date: LocalDate, today: LocalDate, locale: Locale): String =
        format(date, if (date.year == today.year) MONTH_DAY else MONTH_DAY_WITH_YEAR, locale)

    private fun format(date: LocalDate, skeleton: String, locale: Locale): String = date.format(
        DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, skeleton), locale)
    )
}
