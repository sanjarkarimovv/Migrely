package com.migrely.app.history.ui.mapper

import com.migrely.app.history.ui.model.CalendarLayoutUiModel
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

private const val DAYS_PER_WEEK = 7

/** Weeks start on the locale's first day, with its weekday initials (BRD EC-TIM-05); read at every call. */
class CalendarLayoutUiMapper(private val locale: () -> Locale) {
    fun toUi(year: Int, month: Int): CalendarLayoutUiModel {
        val locale = locale()
        val firstDayOfWeek = WeekFields.of(locale).firstDayOfWeek
        val firstOfMonth = YearMonth.of(year, month).atDay(1).dayOfWeek
        return CalendarLayoutUiModel(
            weekdayInitials = (0 until DAYS_PER_WEEK).map { offset ->
                firstDayOfWeek.plus(offset.toLong()).getDisplayName(TextStyle.NARROW, locale)
            },
            leadingEmptyDays = Math.floorMod(
                firstOfMonth.value - firstDayOfWeek.value,
                DAYS_PER_WEEK
            )
        )
    }
}
