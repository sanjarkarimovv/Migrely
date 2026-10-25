package com.migrely.app.datasource.report.layout

import com.migrely.app.datasource.report.model.ReportDayLevelDataModel
import com.migrely.app.datasource.report.model.ReportHeatMonthDataModel

private const val DAYS_PER_WEEK = 7

/**
 * The week rows a row of month calendars needs: the most any of its months needs to reach its last day in the period.
 * Days after the period are not drawn, so they take no rows, and the legend under the last month follows it without a
 * gap (store polish). Days before the period keep their rows, so every month starts on its own weekday.
 */
internal fun weekRowsOf(months: List<ReportHeatMonthDataModel>): Int = months.maxOfOrNull { month ->
    val lastDrawn = month.levels.indexOfLast { it != ReportDayLevelDataModel.Outside }
    if (lastDrawn < 0) 0 else (month.leadingEmptyDays + lastDrawn) / DAYS_PER_WEEK + 1
} ?: 0
