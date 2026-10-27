package com.migrely.app.attack.ui.mapper

import com.migrely.app.attack.presentation.model.AttackRangePresentationModel
import com.migrely.app.attack.ui.model.AttackRangeUiModel

class AttackRangeUiMapper {
    fun toUi(range: AttackRangePresentationModel) = AttackRangeUiModel(
        range.start.toEpochMilli(),
        range.end?.toEpochMilli()
    )
}
