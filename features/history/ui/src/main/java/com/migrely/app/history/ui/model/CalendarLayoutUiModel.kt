package com.migrely.app.history.ui.model

/** Where a month starts in a week grid: the weekday initials in order and the blank cells before the 1st. */
data class CalendarLayoutUiModel(val weekdayInitials: List<String>, val leadingEmptyDays: Int)
