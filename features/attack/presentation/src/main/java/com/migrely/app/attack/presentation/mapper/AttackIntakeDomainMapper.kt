package com.migrely.app.attack.presentation.mapper

import com.migrely.app.attack.domain.model.AttackIntakeDomainModel
import com.migrely.app.attack.domain.model.AttackIntakeEffectDomainModel
import com.migrely.app.attack.presentation.model.AttackIntakeEffectPresentationModel
import com.migrely.app.attack.presentation.model.AttackIntakePresentationModel

/** Turns a removed intake back into its domain form for "Undo", and an answer into its domain form (BRD D39). */
class AttackIntakeDomainMapper {
    fun toDomain(intake: AttackIntakePresentationModel) = AttackIntakeDomainModel(
        id = intake.id,
        medicationId = intake.medicationId,
        medicationName = intake.medicationName,
        dose = intake.dose,
        takenAt = intake.takenAt,
        zone = intake.zone,
        effect = intake.effect?.let(::toDomain)
    )

    fun toDomain(effect: AttackIntakeEffectPresentationModel): AttackIntakeEffectDomainModel =
        when (effect) {
            AttackIntakeEffectPresentationModel.NotAtAll -> AttackIntakeEffectDomainModel.NotAtAll
            AttackIntakeEffectPresentationModel.ALittle -> AttackIntakeEffectDomainModel.ALittle
            AttackIntakeEffectPresentationModel.ALot -> AttackIntakeEffectDomainModel.ALot
            AttackIntakeEffectPresentationModel.Completely ->
                AttackIntakeEffectDomainModel.Completely
        }
}
