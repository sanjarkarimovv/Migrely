package com.migrely.app.attack.presentation.mapper

import com.migrely.app.attack.domain.model.AttackRangeDomainModel
import com.migrely.app.attack.presentation.model.AttackRangePresentationModel

class AttackRangePresentationMapper {
    fun toPresentation(range: AttackRangeDomainModel) =
        AttackRangePresentationModel(range.start, range.end)
}
