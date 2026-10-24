package com.migrely.app.attack.presentation.mapper

import com.migrely.app.attack.domain.model.AttackIntakeDomainModel
import com.migrely.app.attack.domain.model.AttackIntakeEffectDomainModel
import com.migrely.app.attack.presentation.model.AttackIntakeEffectPresentationModel
import com.migrely.app.attack.presentation.model.AttackIntakePresentationModel

class AttackIntakePresentationMapper {
    fun toPresentation(intake: AttackIntakeDomainModel) = AttackIntakePresentationModel(
        id = intake.id,
        medicationId = intake.medicationId,
        medicationName = intake.medicationName,
        dose = intake.dose,
        takenAt = intake.takenAt,
        zone = intake.zone,
        effect = when (intake.effect) {
            AttackIntakeEffectDomainModel.NotAtAll -> AttackIntakeEffectPresentationModel.NotAtAll
            AttackIntakeEffectDomainModel.ALittle -> AttackIntakeEffectPresentationModel.ALittle
            AttackIntakeEffectDomainModel.ALot -> AttackIntakeEffectPresentationModel.ALot
            AttackIntakeEffectDomainModel.Completely ->
                AttackIntakeEffectPresentationModel.Completely
            null -> null
        },
        effectQuestionClosed = intake.effectQuestionClosed
    )
}
