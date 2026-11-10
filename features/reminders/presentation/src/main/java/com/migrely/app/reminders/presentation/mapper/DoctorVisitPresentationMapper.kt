package com.migrely.app.reminders.presentation.mapper

import com.migrely.app.reminders.domain.model.DoctorVisitDomainModel
import com.migrely.app.reminders.presentation.model.DoctorVisitPresentationModel

class DoctorVisitPresentationMapper {
    fun toPresentation(visit: DoctorVisitDomainModel) =
        DoctorVisitPresentationModel(visit.id, visit.date)

    fun toDomain(visit: DoctorVisitPresentationModel) = DoctorVisitDomainModel(visit.id, visit.date)
}
