package com.migrely.app.attack.ui.mapper

import com.migrely.app.attack.presentation.model.PastAttackDraftPresentationModel
import com.migrely.app.attack.ui.model.PastAttackDraftUiModel

class PastAttackDraftUiMapper {
    fun toUi(draft: PastAttackDraftPresentationModel) = PastAttackDraftUiModel(
        startEpochMillis = draft.start.toEpochMilli(),
        endEpochMillis = draft.end.toEpochMilli(),
        zoneId = draft.zone.id
    )
}
