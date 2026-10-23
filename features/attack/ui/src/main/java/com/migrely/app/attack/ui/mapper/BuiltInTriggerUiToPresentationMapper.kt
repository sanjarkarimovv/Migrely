package com.migrely.app.attack.ui.mapper

import com.migrely.app.attack.presentation.model.BuiltInTriggerPresentationModel
import com.migrely.app.attack.ui.model.BuiltInTriggerUiModel

class BuiltInTriggerUiToPresentationMapper {
    fun toPresentation(builtInTrigger: BuiltInTriggerUiModel): BuiltInTriggerPresentationModel =
        when (
            builtInTrigger
        ) {
            BuiltInTriggerUiModel.Stress -> BuiltInTriggerPresentationModel.Stress
            BuiltInTriggerUiModel.TooLittleSleep -> BuiltInTriggerPresentationModel.TooLittleSleep
            BuiltInTriggerUiModel.TooMuchSleep -> BuiltInTriggerPresentationModel.TooMuchSleep
            BuiltInTriggerUiModel.SkippedMeal -> BuiltInTriggerPresentationModel.SkippedMeal
            BuiltInTriggerUiModel.Dehydration -> BuiltInTriggerPresentationModel.Dehydration
            BuiltInTriggerUiModel.Alcohol -> BuiltInTriggerPresentationModel.Alcohol
            BuiltInTriggerUiModel.Caffeine -> BuiltInTriggerPresentationModel.Caffeine
            BuiltInTriggerUiModel.Period -> BuiltInTriggerPresentationModel.Period
            BuiltInTriggerUiModel.Weather -> BuiltInTriggerPresentationModel.Weather
            BuiltInTriggerUiModel.BrightLightOrScreens ->
                BuiltInTriggerPresentationModel.BrightLightOrScreens

            BuiltInTriggerUiModel.Smell -> BuiltInTriggerPresentationModel.Smell
            BuiltInTriggerUiModel.PhysicalEffort -> BuiltInTriggerPresentationModel.PhysicalEffort
            BuiltInTriggerUiModel.Travel -> BuiltInTriggerPresentationModel.Travel
        }
}
