package com.migrely.app.attack.ui.mapper

import com.migrely.app.attack.presentation.model.AttackIntakeEffectPresentationModel
import com.migrely.app.attack.presentation.model.AttackIntakePresentationModel
import com.migrely.app.attack.ui.model.AttackIntakeEffectUiModel
import com.migrely.app.attack.ui.model.AttackIntakeUiModel

class AttackIntakeUiMapper {
    fun toUi(intake: AttackIntakePresentationModel) = AttackIntakeUiModel(
        id = intake.id,
        medicationId = intake.medicationId,
        medicationName = intake.medicationName,
        dose = intake.dose,
        takenAtEpochMillis = intake.takenAt.toEpochMilli(),
        zoneId = intake.zone.id,
        effect = when (intake.effect) {
            AttackIntakeEffectPresentationModel.NotAtAll -> AttackIntakeEffectUiModel.NotAtAll
            AttackIntakeEffectPresentationModel.ALittle -> AttackIntakeEffectUiModel.ALittle
            AttackIntakeEffectPresentationModel.ALot -> AttackIntakeEffectUiModel.ALot
            AttackIntakeEffectPresentationModel.Completely -> AttackIntakeEffectUiModel.Completely
            null -> null
        },
        effectQuestionClosed = intake.effectQuestionClosed
    )
}
