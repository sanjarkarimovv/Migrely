package com.migrely.app.attack.ui.mapper

import com.migrely.app.attack.presentation.model.BuiltInTriggerPresentationModel
import com.migrely.app.attack.ui.model.BuiltInTriggerUiModel

class BuiltInTriggerUiMapper {
    fun toUi(builtInTrigger: BuiltInTriggerPresentationModel): BuiltInTriggerUiModel = when (
        builtInTrigger
    ) {
        BuiltInTriggerPresentationModel.Stress -> BuiltInTriggerUiModel.Stress
        BuiltInTriggerPresentationModel.TooLittleSleep -> BuiltInTriggerUiModel.TooLittleSleep
        BuiltInTriggerPresentationModel.TooMuchSleep -> BuiltInTriggerUiModel.TooMuchSleep
        BuiltInTriggerPresentationModel.SkippedMeal -> BuiltInTriggerUiModel.SkippedMeal
        BuiltInTriggerPresentationModel.Dehydration -> BuiltInTriggerUiModel.Dehydration
        BuiltInTriggerPresentationModel.Alcohol -> BuiltInTriggerUiModel.Alcohol
        BuiltInTriggerPresentationModel.Caffeine -> BuiltInTriggerUiModel.Caffeine
        BuiltInTriggerPresentationModel.Period -> BuiltInTriggerUiModel.Period
        BuiltInTriggerPresentationModel.Weather -> BuiltInTriggerUiModel.Weather
        BuiltInTriggerPresentationModel.BrightLightOrScreens ->
            BuiltInTriggerUiModel.BrightLightOrScreens

        BuiltInTriggerPresentationModel.Smell -> BuiltInTriggerUiModel.Smell
        BuiltInTriggerPresentationModel.PhysicalEffort -> BuiltInTriggerUiModel.PhysicalEffort
        BuiltInTriggerPresentationModel.Travel -> BuiltInTriggerUiModel.Travel
    }
}
