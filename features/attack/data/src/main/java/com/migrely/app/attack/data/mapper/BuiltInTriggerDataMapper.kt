package com.migrely.app.attack.data.mapper

import com.migrely.app.attack.domain.model.BuiltInTriggerDomainModel
import com.migrely.app.datasource.attack.model.BuiltInTriggerDataModel

class BuiltInTriggerDataMapper {
    fun toData(trigger: BuiltInTriggerDomainModel): BuiltInTriggerDataModel = when (trigger) {
        BuiltInTriggerDomainModel.Stress -> BuiltInTriggerDataModel.Stress
        BuiltInTriggerDomainModel.TooLittleSleep -> BuiltInTriggerDataModel.TooLittleSleep
        BuiltInTriggerDomainModel.TooMuchSleep -> BuiltInTriggerDataModel.TooMuchSleep
        BuiltInTriggerDomainModel.SkippedMeal -> BuiltInTriggerDataModel.SkippedMeal
        BuiltInTriggerDomainModel.Dehydration -> BuiltInTriggerDataModel.Dehydration
        BuiltInTriggerDomainModel.Alcohol -> BuiltInTriggerDataModel.Alcohol
        BuiltInTriggerDomainModel.Caffeine -> BuiltInTriggerDataModel.Caffeine
        BuiltInTriggerDomainModel.Period -> BuiltInTriggerDataModel.Period
        BuiltInTriggerDomainModel.Weather -> BuiltInTriggerDataModel.Weather
        BuiltInTriggerDomainModel.BrightLightOrScreens ->
            BuiltInTriggerDataModel.BrightLightOrScreens

        BuiltInTriggerDomainModel.Smell -> BuiltInTriggerDataModel.Smell
        BuiltInTriggerDomainModel.PhysicalEffort -> BuiltInTriggerDataModel.PhysicalEffort
        BuiltInTriggerDomainModel.Travel -> BuiltInTriggerDataModel.Travel
    }
}
