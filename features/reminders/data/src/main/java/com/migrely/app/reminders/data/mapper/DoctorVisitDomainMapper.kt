package com.migrely.app.reminders.data.mapper

import com.migrely.app.datasource.doctorvisit.model.DoctorVisitDataModel
import com.migrely.app.reminders.domain.model.DoctorVisitDomainModel

class DoctorVisitDomainMapper {
    fun toDomain(visit: DoctorVisitDataModel) = DoctorVisitDomainModel(visit.id, visit.date)

    fun toData(visit: DoctorVisitDomainModel) = DoctorVisitDataModel(visit.id, visit.date)
}
