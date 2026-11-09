package com.migrely.app.reminders.domain.repository

import com.migrely.app.reminders.domain.model.DoctorVisitsResultDomainModel
import java.time.LocalDate

interface GetDoctorVisitsRepository {
    /** The next and last visits by [today]. */
    fun doctorVisits(today: LocalDate): DoctorVisitsResultDomainModel
}
