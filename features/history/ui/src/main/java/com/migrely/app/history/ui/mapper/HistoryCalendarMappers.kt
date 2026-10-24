package com.migrely.app.history.ui.mapper

/** The mappers the month calendar needs, shared by History and the month card. */
data class HistoryCalendarMappers(
    val calendarLayoutUiMapper: CalendarLayoutUiMapper,
    val daySkyAppearanceUiMapper: DaySkyAppearanceUiMapper,
    val historyTimeUiFormatter: HistoryTimeUiFormatter
)
