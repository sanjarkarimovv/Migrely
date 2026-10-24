package com.migrely.app.attack.data.mapper

import com.migrely.app.attack.domain.model.BuiltInTriggerDomainModel
import com.migrely.app.datasource.attack.model.BuiltInTriggerDataModel

class BuiltInTriggerDomainMapper {
    fun toDomain(trigger: BuiltInTriggerDataModel): BuiltInTriggerDomainModel = when (trigger) {
        BuiltInTriggerDataModel.Stress -> BuiltInTriggerDomainModel.Stress
        BuiltInTriggerDataModel.TooLittleSleep -> BuiltInTriggerDomainModel.TooLittleSleep
        BuiltInTriggerDataModel.TooMuchSleep -> BuiltInTriggerDomainModel.TooMuchSleep
        BuiltInTriggerDataModel.SkippedMeal -> BuiltInTriggerDomainModel.SkippedMeal
        BuiltInTriggerDataModel.Dehydration -> BuiltInTriggerDomainModel.Dehydration
        BuiltInTriggerDataModel.Alcohol -> BuiltInTriggerDomainModel.Alcohol
        BuiltInTriggerDataModel.Caffeine -> BuiltInTriggerDomainModel.Caffeine
        BuiltInTriggerDataModel.Period -> BuiltInTriggerDomainModel.Period
        BuiltInTriggerDataModel.Weather -> BuiltInTriggerDomainModel.Weather
        BuiltInTriggerDataModel.BrightLightOrScreens ->
            BuiltInTriggerDomainModel.BrightLightOrScreens

        BuiltInTriggerDataModel.Smell -> BuiltInTriggerDomainModel.Smell
        BuiltInTriggerDataModel.PhysicalEffort -> BuiltInTriggerDomainModel.PhysicalEffort
        BuiltInTriggerDataModel.Travel -> BuiltInTriggerDomainModel.Travel
    }
}
