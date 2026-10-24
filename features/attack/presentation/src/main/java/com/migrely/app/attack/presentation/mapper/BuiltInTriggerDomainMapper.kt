package com.migrely.app.attack.presentation.mapper

import com.migrely.app.attack.domain.model.BuiltInTriggerDomainModel
import com.migrely.app.attack.presentation.model.BuiltInTriggerPresentationModel

class BuiltInTriggerDomainMapper {
    fun toDomain(builtInTrigger: BuiltInTriggerPresentationModel): BuiltInTriggerDomainModel =
        when (
            builtInTrigger
        ) {
            BuiltInTriggerPresentationModel.Stress -> BuiltInTriggerDomainModel.Stress
            BuiltInTriggerPresentationModel.TooLittleSleep ->
                BuiltInTriggerDomainModel.TooLittleSleep
            BuiltInTriggerPresentationModel.TooMuchSleep -> BuiltInTriggerDomainModel.TooMuchSleep
            BuiltInTriggerPresentationModel.SkippedMeal -> BuiltInTriggerDomainModel.SkippedMeal
            BuiltInTriggerPresentationModel.Dehydration -> BuiltInTriggerDomainModel.Dehydration
            BuiltInTriggerPresentationModel.Alcohol -> BuiltInTriggerDomainModel.Alcohol
            BuiltInTriggerPresentationModel.Caffeine -> BuiltInTriggerDomainModel.Caffeine
            BuiltInTriggerPresentationModel.Period -> BuiltInTriggerDomainModel.Period
            BuiltInTriggerPresentationModel.Weather -> BuiltInTriggerDomainModel.Weather
            BuiltInTriggerPresentationModel.BrightLightOrScreens ->
                BuiltInTriggerDomainModel.BrightLightOrScreens

            BuiltInTriggerPresentationModel.Smell -> BuiltInTriggerDomainModel.Smell
            BuiltInTriggerPresentationModel.PhysicalEffort ->
                BuiltInTriggerDomainModel.PhysicalEffort
            BuiltInTriggerPresentationModel.Travel -> BuiltInTriggerDomainModel.Travel
        }
}
