package com.migrely.app.datasource.doctorvisit.mapper

import com.migrely.app.datasource.doctorvisit.model.DoctorVisitDataModel
import com.migrely.app.datasource.doctorvisit.model.DoctorVisitLocalModel
import java.time.LocalDate
import java.time.format.DateTimeParseException

/** Dates are stored as ISO `yyyy-MM-dd`; a row whose date cannot be read is left out. */
class DoctorVisitLocalMapper {
    fun toData(local: DoctorVisitLocalModel): DoctorVisitDataModel? =
        toDate(local.date)?.let { DoctorVisitDataModel(local.id, it) }

    fun toLocal(visit: DoctorVisitDataModel) = DoctorVisitLocalModel(visit.id, toLocal(visit.date))

    fun toLocal(date: LocalDate): String = date.toString()

    @Suppress("SwallowedException")
    fun toDate(local: String): LocalDate? = try {
        LocalDate.parse(local)
    } catch (exception: DateTimeParseException) {
        null
    }
}
