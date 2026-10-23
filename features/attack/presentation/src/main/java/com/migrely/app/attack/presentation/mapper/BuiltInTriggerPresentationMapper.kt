package com.migrely.app.attack.presentation.mapper

import com.migrely.app.attack.domain.model.BuiltInTriggerDomainModel
import com.migrely.app.attack.presentation.model.BuiltInTriggerPresentationModel

class BuiltInTriggerPresentationMapper {
    fun toPresentation(builtInTrigger: BuiltInTriggerDomainModel): BuiltInTriggerPresentationModel =
        when (
            builtInTrigger
        ) {
            BuiltInTriggerDomainModel.Stress -> BuiltInTriggerPresentationModel.Stress
            BuiltInTriggerDomainModel.TooLittleSleep ->
                BuiltInTriggerPresentationModel.TooLittleSleep
            BuiltInTriggerDomainModel.TooMuchSleep -> BuiltInTriggerPresentationModel.TooMuchSleep
            BuiltInTriggerDomainModel.SkippedMeal -> BuiltInTriggerPresentationModel.SkippedMeal
            BuiltInTriggerDomainModel.Dehydration -> BuiltInTriggerPresentationModel.Dehydration
            BuiltInTriggerDomainModel.Alcohol -> BuiltInTriggerPresentationModel.Alcohol
            BuiltInTriggerDomainModel.Caffeine -> BuiltInTriggerPresentationModel.Caffeine
            BuiltInTriggerDomainModel.Period -> BuiltInTriggerPresentationModel.Period
            BuiltInTriggerDomainModel.Weather -> BuiltInTriggerPresentationModel.Weather
            BuiltInTriggerDomainModel.BrightLightOrScreens ->
                BuiltInTriggerPresentationModel.BrightLightOrScreens

            BuiltInTriggerDomainModel.Smell -> BuiltInTriggerPresentationModel.Smell
            BuiltInTriggerDomainModel.PhysicalEffort ->
                BuiltInTriggerPresentationModel.PhysicalEffort
            BuiltInTriggerDomainModel.Travel -> BuiltInTriggerPresentationModel.Travel
        }
}
