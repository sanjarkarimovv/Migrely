package com.migrely.app.onboarding.domain.repository

import com.migrely.app.onboarding.domain.model.OnboardingWriteResultDomainModel

interface SetPeriodTrackingRepository {
    fun setPeriodTracking(on: Boolean): OnboardingWriteResultDomainModel
}
