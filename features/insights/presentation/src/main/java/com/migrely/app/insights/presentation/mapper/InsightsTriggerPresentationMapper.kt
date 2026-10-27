package com.migrely.app.insights.presentation.mapper

import com.migrely.app.insights.domain.model.InsightsBuiltInTriggerDomainModel
import com.migrely.app.insights.domain.model.InsightsTriggerDomainModel
import com.migrely.app.insights.presentation.model.InsightsBuiltInTriggerPresentationModel
import com.migrely.app.insights.presentation.model.InsightsTriggerPresentationModel

class InsightsTriggerPresentationMapper {
    fun toPresentation(trigger: InsightsTriggerDomainModel): InsightsTriggerPresentationModel =
        when (trigger) {
            is InsightsTriggerDomainModel.BuiltIn ->
                InsightsTriggerPresentationModel.BuiltIn(builtInOf(trigger.trigger))
            is InsightsTriggerDomainModel.Custom ->
                InsightsTriggerPresentationModel.Custom(trigger.name)
        }

    private fun builtInOf(trigger: InsightsBuiltInTriggerDomainModel) = when (trigger) {
        InsightsBuiltInTriggerDomainModel.Stress -> InsightsBuiltInTriggerPresentationModel.Stress
        InsightsBuiltInTriggerDomainModel.TooLittleSleep ->
            InsightsBuiltInTriggerPresentationModel.TooLittleSleep
        InsightsBuiltInTriggerDomainModel.TooMuchSleep ->
            InsightsBuiltInTriggerPresentationModel.TooMuchSleep
        InsightsBuiltInTriggerDomainModel.SkippedMeal ->
            InsightsBuiltInTriggerPresentationModel.SkippedMeal
        InsightsBuiltInTriggerDomainModel.Dehydration ->
            InsightsBuiltInTriggerPresentationModel.Dehydration
        InsightsBuiltInTriggerDomainModel.Alcohol -> InsightsBuiltInTriggerPresentationModel.Alcohol
        InsightsBuiltInTriggerDomainModel.Caffeine ->
            InsightsBuiltInTriggerPresentationModel.Caffeine
        InsightsBuiltInTriggerDomainModel.Period -> InsightsBuiltInTriggerPresentationModel.Period
        InsightsBuiltInTriggerDomainModel.Weather -> InsightsBuiltInTriggerPresentationModel.Weather
        InsightsBuiltInTriggerDomainModel.BrightLightOrScreens ->
            InsightsBuiltInTriggerPresentationModel.BrightLightOrScreens
        InsightsBuiltInTriggerDomainModel.Smell -> InsightsBuiltInTriggerPresentationModel.Smell
        InsightsBuiltInTriggerDomainModel.PhysicalEffort ->
            InsightsBuiltInTriggerPresentationModel.PhysicalEffort
        InsightsBuiltInTriggerDomainModel.Travel -> InsightsBuiltInTriggerPresentationModel.Travel
    }
}
