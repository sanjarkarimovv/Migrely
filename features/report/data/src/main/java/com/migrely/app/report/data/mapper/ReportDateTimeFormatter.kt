package com.migrely.app.report.data.mapper

import java.time.DayOfWeek
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale

private const val NO_BREAK_SPACE = '\u00A0'

/**
 * The phone's conventions the report follows as the app's screens do (BRD EC-TIM-05): where the week starts, and a
 * time in the locale's pattern for the phone's 12- or 24-hour setting. Inputs are functions, so they are read when a
 * report is made and tests can fix them; [bestDateTimePattern] is Android's `DateFormat.getBestDateTimePattern`.
 */
class ReportDateTimeFormatter(
    private val locale: () -> Locale,
    private val is24HourFormat: () -> Boolean,
    private val bestDateTimePattern: (Locale, String) -> String
) {
    /** The locale's first day of the week, where the app's month calendar starts its weeks. */
    fun firstDayOfWeek(): DayOfWeek = WeekFields.of(locale()).firstDayOfWeek

    /**
     * "9:00 AM" on a 12-hour phone in the US and "09:00" on a 24-hour one, as the app shows it. Its spaces do not
     * break, so a narrow cell never leaves "AM" alone on a line.
     */
    fun formatTime(time: ZonedDateTime): String {
        val locale = locale()
        val skeleton = if (is24HourFormat()) "Hm" else "hm"
        return DateTimeFormatter.ofPattern(bestDateTimePattern(locale, skeleton), locale)
            .format(time)
            .replace(' ', NO_BREAK_SPACE)
    }
}
