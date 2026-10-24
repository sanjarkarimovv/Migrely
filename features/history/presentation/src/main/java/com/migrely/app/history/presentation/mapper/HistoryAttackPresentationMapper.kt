package com.migrely.app.history.presentation.mapper

import com.migrely.app.history.domain.model.HistoryAttackDomainModel
import com.migrely.app.history.presentation.model.HistoryAttackPresentationModel

class HistoryAttackPresentationMapper {
    fun toPresentation(attack: HistoryAttackDomainModel) = HistoryAttackPresentationModel(
        id = attack.id,
        start = attack.start,
        startZone = attack.startZone,
        end = attack.end,
        peakIntensity = attack.peakIntensity
    )
}
