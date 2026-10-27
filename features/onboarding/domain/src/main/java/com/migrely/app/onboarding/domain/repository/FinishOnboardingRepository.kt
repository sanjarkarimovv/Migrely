package com.migrely.app.onboarding.domain.repository

import com.migrely.app.onboarding.domain.model.OnboardingWriteResultDomainModel

interface FinishOnboardingRepository {
    fun finishOnboarding(): OnboardingWriteResultDomainModel
}
