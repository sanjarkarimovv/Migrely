package com.migrely.app.datasource.daymark.mapper

import com.migrely.app.datasource.daymark.model.DayMarkDataModel
import com.migrely.app.datasource.daymark.model.DayMarkLocalModel
import java.time.LocalDate
import java.time.format.DateTimeParseException

/** Dates are stored as ISO `yyyy-MM-dd`; a row whose date cannot be read is skipped. */
class DayMarkLocalMapper {
    @Suppress("SwallowedException")
    fun toData(local: DayMarkLocalModel): DayMarkDataModel? = try {
        DayMarkDataModel(LocalDate.parse(local.date), local.period, local.painFree)
    } catch (exception: DateTimeParseException) {
        null
    }

    fun toLocal(date: LocalDate): String = date.toString()
}
