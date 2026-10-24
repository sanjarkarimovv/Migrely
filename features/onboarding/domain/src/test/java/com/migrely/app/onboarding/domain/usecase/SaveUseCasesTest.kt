package com.migrely.app.onboarding.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.onboarding.domain.model.AttackFrequencyDomainModel
import com.migrely.app.onboarding.domain.model.OnboardingGoalDomainModel
import com.migrely.app.onboarding.domain.model.OnboardingWriteResultDomainModel
import com.migrely.app.onboarding.domain.repository.FinishOnboardingRepository
import com.migrely.app.onboarding.domain.repository.SetAttackFrequencyRepository
import com.migrely.app.onboarding.domain.repository.SetGoalRepository
import com.migrely.app.onboarding.domain.repository.SetPeriodTrackingRepository
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock

class SaveUseCasesTest {
    private val context = CoroutineContextProvider.Default

    @Test
    fun `When an answer is saved then the repository saves it, with its result`() {
        val goals: SetGoalRepository = mock()
        val frequencies: SetAttackFrequencyRepository = mock()
        val periods: SetPeriodTrackingRepository = mock()
        val finish: FinishOnboardingRepository = mock()
        given(
            goals.setGoal(OnboardingGoalDomainModel.FindTriggers)
        ).willReturn(OnboardingWriteResultDomainModel.Done)
        given(frequencies.setAttackFrequency(AttackFrequencyDomainModel.FifteenOrMore))
            .willReturn(OnboardingWriteResultDomainModel.Failed)
        given(periods.setPeriodTracking(false)).willReturn(OnboardingWriteResultDomainModel.Done)
        given(finish.finishOnboarding()).willReturn(OnboardingWriteResultDomainModel.Done)

        assertEquals(
            OnboardingWriteResultDomainModel.Done,
            SaveGoalUseCase(
                goals,
                context
            ).executeInBackground(OnboardingGoalDomainModel.FindTriggers)
        )
        assertEquals(
            OnboardingWriteResultDomainModel.Failed,
            SaveAttackFrequencyUseCase(frequencies, context)
                .executeInBackground(AttackFrequencyDomainModel.FifteenOrMore)
        )
        assertEquals(
            OnboardingWriteResultDomainModel.Done,
            SavePeriodTrackingUseCase(periods, context).executeInBackground(false)
        )
        assertEquals(
            OnboardingWriteResultDomainModel.Done,
            FinishOnboardingUseCase(finish, context).executeInBackground(Unit)
        )
    }
}
