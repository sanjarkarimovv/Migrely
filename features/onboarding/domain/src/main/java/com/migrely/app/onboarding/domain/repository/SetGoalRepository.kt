package com.migrely.app.onboarding.domain.repository

import com.migrely.app.onboarding.domain.model.OnboardingGoalDomainModel
import com.migrely.app.onboarding.domain.model.OnboardingWriteResultDomainModel

interface SetGoalRepository {
    fun setGoal(goal: OnboardingGoalDomainModel): OnboardingWriteResultDomainModel
}
