package com.migrely.app.medication.data.mapper

import com.migrely.app.datasource.medication.model.IntakeEffectDataModel
import com.migrely.app.medication.domain.model.IntakeEffectDomainModel

class IntakeEffectDataMapper {
    fun toData(effect: IntakeEffectDomainModel): IntakeEffectDataModel = when (effect) {
        IntakeEffectDomainModel.NotAtAll -> IntakeEffectDataModel.NotAtAll
        IntakeEffectDomainModel.ALittle -> IntakeEffectDataModel.ALittle
        IntakeEffectDomainModel.ALot -> IntakeEffectDataModel.ALot
        IntakeEffectDomainModel.Completely -> IntakeEffectDataModel.Completely
    }
}
