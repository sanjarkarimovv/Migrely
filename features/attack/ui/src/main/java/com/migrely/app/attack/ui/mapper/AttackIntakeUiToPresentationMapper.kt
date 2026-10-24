package com.migrely.app.attack.ui.mapper

import com.migrely.app.attack.presentation.model.AttackIntakeEffectPresentationModel
import com.migrely.app.attack.presentation.model.AttackIntakePresentationModel
import com.migrely.app.attack.ui.model.AttackIntakeEffectUiModel
import com.migrely.app.attack.ui.model.AttackIntakeUiModel
import java.time.Instant
import java.time.ZoneId

class AttackIntakeUiToPresentationMapper {
    fun toPresentation(intake: AttackIntakeUiModel) = AttackIntakePresentationModel(
        id = intake.id,
        medicationId = intake.medicationId,
        medicationName = intake.medicationName,
        dose = intake.dose,
        takenAt = Instant.ofEpochMilli(intake.takenAtEpochMillis),
        zone = ZoneId.of(intake.zoneId),
        effect = intake.effect?.let(::toPresentation),
        effectQuestionClosed = intake.effectQuestionClosed
    )

    fun toPresentation(effect: AttackIntakeEffectUiModel): AttackIntakeEffectPresentationModel =
        when (effect) {
            AttackIntakeEffectUiModel.NotAtAll -> AttackIntakeEffectPresentationModel.NotAtAll
            AttackIntakeEffectUiModel.ALittle -> AttackIntakeEffectPresentationModel.ALittle
            AttackIntakeEffectUiModel.ALot -> AttackIntakeEffectPresentationModel.ALot
            AttackIntakeEffectUiModel.Completely -> AttackIntakeEffectPresentationModel.Completely
        }
}
