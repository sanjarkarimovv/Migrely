package com.migrely.app.attack.presentation.mapper

import com.migrely.app.attack.domain.model.TimeFieldDomainModel
import com.migrely.app.attack.presentation.model.TimeFieldPresentationModel

class TimeFieldPresentationMapper {
    fun toPresentation(field: TimeFieldDomainModel) = when (field) {
        TimeFieldDomainModel.Start -> TimeFieldPresentationModel.Start
        TimeFieldDomainModel.End -> TimeFieldPresentationModel.End
    }
}
