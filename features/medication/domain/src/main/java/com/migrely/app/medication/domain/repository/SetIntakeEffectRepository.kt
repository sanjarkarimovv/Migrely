package com.migrely.app.medication.domain.repository

import com.migrely.app.medication.domain.model.IntakeEffectDomainModel
import com.migrely.app.medication.domain.model.WriteResultDomainModel

interface SetIntakeEffectRepository {
    fun setIntakeEffect(intakeId: String, effect: IntakeEffectDomainModel): WriteResultDomainModel
}
