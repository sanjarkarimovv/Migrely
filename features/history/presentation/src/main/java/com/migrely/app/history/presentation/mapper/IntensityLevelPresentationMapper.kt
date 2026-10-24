package com.migrely.app.history.presentation.mapper

import com.migrely.app.history.domain.model.PainLevelDomainModel
import com.migrely.app.history.presentation.model.IntensityLevelPresentationModel

/** Maps a 1–10 pain value to the three levels the skies show, by the domain's rule (BRD EC-UX-09). */
class IntensityLevelPresentationMapper(
    private val painLevelPresentationMapper: PainLevelPresentationMapper
) {
    fun toPresentation(value: Int): IntensityLevelPresentationModel =
        painLevelPresentationMapper.toPresentation(PainLevelDomainModel.of(value))
}
