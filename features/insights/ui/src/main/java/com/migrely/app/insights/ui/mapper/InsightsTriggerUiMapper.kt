package com.migrely.app.insights.ui.mapper

import com.migrely.app.insights.presentation.model.InsightsBuiltInTriggerPresentationModel
import com.migrely.app.insights.presentation.model.InsightsTriggerPresentationModel
import com.migrely.app.insights.ui.model.InsightsBuiltInTriggerUiModel
import com.migrely.app.insights.ui.model.InsightsTriggerUiModel

class InsightsTriggerUiMapper {
    fun toUi(trigger: InsightsTriggerPresentationModel): InsightsTriggerUiModel = when (trigger) {
        is InsightsTriggerPresentationModel.BuiltIn ->
            InsightsTriggerUiModel.BuiltIn(builtInOf(trigger.trigger))
        is InsightsTriggerPresentationModel.Custom -> InsightsTriggerUiModel.Custom(trigger.name)
    }

    private fun builtInOf(trigger: InsightsBuiltInTriggerPresentationModel) = when (trigger) {
        InsightsBuiltInTriggerPresentationModel.Stress -> InsightsBuiltInTriggerUiModel.Stress
        InsightsBuiltInTriggerPresentationModel.TooLittleSleep ->
            InsightsBuiltInTriggerUiModel.TooLittleSleep
        InsightsBuiltInTriggerPresentationModel.TooMuchSleep ->
            InsightsBuiltInTriggerUiModel.TooMuchSleep
        InsightsBuiltInTriggerPresentationModel.SkippedMeal ->
            InsightsBuiltInTriggerUiModel.SkippedMeal
        InsightsBuiltInTriggerPresentationModel.Dehydration ->
            InsightsBuiltInTriggerUiModel.Dehydration
        InsightsBuiltInTriggerPresentationModel.Alcohol -> InsightsBuiltInTriggerUiModel.Alcohol
        InsightsBuiltInTriggerPresentationModel.Caffeine -> InsightsBuiltInTriggerUiModel.Caffeine
        InsightsBuiltInTriggerPresentationModel.Period -> InsightsBuiltInTriggerUiModel.Period
        InsightsBuiltInTriggerPresentationModel.Weather -> InsightsBuiltInTriggerUiModel.Weather
        InsightsBuiltInTriggerPresentationModel.BrightLightOrScreens ->
            InsightsBuiltInTriggerUiModel.BrightLightOrScreens
        InsightsBuiltInTriggerPresentationModel.Smell -> InsightsBuiltInTriggerUiModel.Smell
        InsightsBuiltInTriggerPresentationModel.PhysicalEffort ->
            InsightsBuiltInTriggerUiModel.PhysicalEffort
        InsightsBuiltInTriggerPresentationModel.Travel -> InsightsBuiltInTriggerUiModel.Travel
    }
}
