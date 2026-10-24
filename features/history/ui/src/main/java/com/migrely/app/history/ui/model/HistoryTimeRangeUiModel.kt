package com.migrely.app.history.ui.model

/**
 * The formatted start and end of a listed attack; [end] is null while it is in progress. The last time carries the city
 * when there is one (BRD D36).
 */
data class HistoryTimeRangeUiModel(val start: String, val end: String?)
