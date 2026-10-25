package com.migrely.app.report.data.mapper

import java.time.DayOfWeek
import java.time.temporal.WeekFields
import java.util.Locale

/**
 * The phone's conventions the report follows as the app's screens do (BRD EC-TIM-05). [locale] is a function, so it is
 * read when a report is made and tests can fix it.
 */
class ReportDateTimeFormatter(private val locale: () -> Locale) {
    /** The locale's first day of the week, where the app's month calendar starts its weeks. */
    fun firstDayOfWeek(): DayOfWeek = WeekFields.of(locale()).firstDayOfWeek
}
