package com.migrely.app.history.data.mapper

import com.migrely.app.datasource.daymark.model.DayMarkDataModel
import com.migrely.app.history.domain.model.DayMarkDomainModel

class DayMarkDomainMapper {
    fun toDomain(mark: DayMarkDataModel) = DayMarkDomainModel(mark.date, mark.period, mark.painFree)
}
