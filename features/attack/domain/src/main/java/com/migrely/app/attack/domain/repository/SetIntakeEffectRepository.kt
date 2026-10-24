package com.migrely.app.attack.domain.repository

import com.migrely.app.attack.domain.model.AttackIntakeEffectDomainModel
import com.migrely.app.attack.domain.model.WriteResultDomainModel

interface SetIntakeEffectRepository {
    fun setIntakeEffect(
        intakeId: String,
        effect: AttackIntakeEffectDomainModel
    ): WriteResultDomainModel
}
