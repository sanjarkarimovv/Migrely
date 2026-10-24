package com.migrely.app.onboarding.data.mapper

import com.migrely.app.datasource.preferences.model.GoalDataModel
import com.migrely.app.onboarding.domain.model.OnboardingGoalDomainModel

class OnboardingGoalDataMapper {
    fun toData(goal: OnboardingGoalDomainModel) = when (goal) {
        OnboardingGoalDomainModel.RecordForDoctor -> GoalDataModel.RecordForDoctor
        OnboardingGoalDomainModel.FindTriggers -> GoalDataModel.FindTriggers
        OnboardingGoalDomainModel.FewerPainkillers -> GoalDataModel.FewerPainkillers
    }
}
