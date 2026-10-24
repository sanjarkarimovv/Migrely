package com.migrely.app.onboarding.domain.repository

import com.migrely.app.onboarding.domain.model.AttackFrequencyDomainModel
import com.migrely.app.onboarding.domain.model.OnboardingWriteResultDomainModel

interface SetAttackFrequencyRepository {
    fun setAttackFrequency(frequency: AttackFrequencyDomainModel): OnboardingWriteResultDomainModel
}
