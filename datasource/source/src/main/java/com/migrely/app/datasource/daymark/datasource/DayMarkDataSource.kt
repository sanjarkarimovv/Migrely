package com.migrely.app.datasource.daymark.datasource

import com.migrely.app.datasource.daymark.model.DayMarkDataModel
import java.time.LocalDate

/**
 * Period and pain-free marks by calendar date (BRD DAY-F1, DAY-F2, D47). Only dates with a mark are stored. Every
 * function blocks and throws the storage exceptions described on
 * [com.migrely.app.datasource.attack.datasource.AttackDataSource].
 */
interface DayMarkDataSource {
    /** The marked dates from [from] to [to], both included. */
    fun marksBetween(from: LocalDate, to: LocalDate): List<DayMarkDataModel>

    fun setPeriod(date: LocalDate, marked: Boolean)

    fun setPainFree(date: LocalDate, marked: Boolean)
}
