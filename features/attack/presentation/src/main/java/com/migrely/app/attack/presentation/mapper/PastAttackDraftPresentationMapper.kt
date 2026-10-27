package com.migrely.app.attack.presentation.mapper

import com.migrely.app.attack.domain.model.PastAttackDraftDomainModel
import com.migrely.app.attack.presentation.model.PastAttackDraftPresentationModel

class PastAttackDraftPresentationMapper {
    fun toPresentation(draft: PastAttackDraftDomainModel) =
        PastAttackDraftPresentationModel(draft.start, draft.end, draft.zone)
}
