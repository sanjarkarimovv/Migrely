package com.migrely.app.reminders.domain.repository

import com.migrely.app.reminders.domain.model.RemoveDoctorVisitResultDomainModel
import java.time.LocalDate

interface RemoveNextVisitRepository {
    fun removeNextVisit(today: LocalDate): RemoveDoctorVisitResultDomainModel
}
