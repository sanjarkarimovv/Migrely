package com.migrely.app.onboarding.presentation.mapper

import com.migrely.app.onboarding.domain.model.OnboardingGoalDomainModel
import com.migrely.app.onboarding.presentation.model.OnboardingGoalPresentationModel

class OnboardingGoalDomainMapper {
    fun toDomain(goal: OnboardingGoalPresentationModel) = when (goal) {
        OnboardingGoalPresentationModel.RecordForDoctor -> OnboardingGoalDomainModel.RecordForDoctor
        OnboardingGoalPresentationModel.FindTriggers -> OnboardingGoalDomainModel.FindTriggers
        OnboardingGoalPresentationModel.FewerPainkillers ->
            OnboardingGoalDomainModel.FewerPainkillers
    }
}
