package com.migrely.app.attack.data.mapper

import com.migrely.app.attack.domain.model.AttackIntakeEffectDomainModel
import com.migrely.app.datasource.medication.model.IntakeEffectDataModel

class AttackIntakeEffectDataMapper {
    fun toData(effect: AttackIntakeEffectDomainModel): IntakeEffectDataModel = when (effect) {
        AttackIntakeEffectDomainModel.NotAtAll -> IntakeEffectDataModel.NotAtAll
        AttackIntakeEffectDomainModel.ALittle -> IntakeEffectDataModel.ALittle
        AttackIntakeEffectDomainModel.ALot -> IntakeEffectDataModel.ALot
        AttackIntakeEffectDomainModel.Completely -> IntakeEffectDataModel.Completely
    }
}
