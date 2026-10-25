package com.migrely.app.subscription.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.subscription.domain.model.EntitlementDomainModel
import com.migrely.app.subscription.domain.model.EntitlementKindDomainModel
import com.migrely.app.subscription.domain.model.PremiumPlanDomainModel
import com.migrely.app.subscription.domain.model.TrialReminderDomainModel
import com.migrely.app.subscription.domain.repository.DismissTrialBannerRepository
import com.migrely.app.subscription.domain.repository.GetCurrentTimeRepository
import com.migrely.app.subscription.domain.repository.GetEntitlementRepository
import com.migrely.app.subscription.domain.repository.GetTimeZoneRepository
import com.migrely.app.subscription.domain.repository.GetTrialBannerDismissalRepository
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class TrialReminderUseCasesTest {
    private val getEntitlementRepository: GetEntitlementRepository = mock()
    private val getTrialBannerDismissalRepository: GetTrialBannerDismissalRepository = mock()
    private val getCurrentTimeRepository: GetCurrentTimeRepository = mock {
        on { now() } doReturn NOW
    }
    private val getTimeZoneRepository: GetTimeZoneRepository = mock {
        on { zone() } doReturn ZoneOffset.UTC
    }
    private val classUnderTest = GetTrialReminderUseCase(
        getEntitlementRepository,
        getTrialBannerDismissalRepository,
        getCurrentTimeRepository,
        getTimeZoneRepository,
        CoroutineContextProvider.Default
    )

    private fun givenTrialEndingIn(left: Duration): Instant {
        val end = NOW.plus(left)
        given(getEntitlementRepository.entitlement())
            .willReturn(subscription(PremiumPlanDomainModel.Yearly, trialEnd = end))
        return end
    }

    @Test
    fun `Given the trial started 6 days ago then home reminds of it`() {
        val end = givenTrialEndingIn(Duration.ofHours(23))

        // NOW is 09:00 UTC, so 23 hours on is tomorrow.
        assertEquals(
            TrialReminderDomainModel.EndsSoon(end, endsToday = false, renews = true),
            classUnderTest.executeInBackground(Unit)
        )
    }

    @Test
    fun `Given the trial ends tonight and was cancelled then it ends today, with Premium`() {
        val end = NOW.plus(Duration.ofHours(10))
        given(getEntitlementRepository.entitlement()).willReturn(
            subscription(PremiumPlanDomainModel.Yearly, trialEnd = end).copy(autoRenewing = false)
        )

        assertEquals(
            TrialReminderDomainModel.EndsSoon(end, endsToday = true, renews = false),
            classUnderTest.executeInBackground(Unit)
        )
    }

    @Test
    fun `Given more than a day left then there is no reminder yet`() {
        givenTrialEndingIn(Duration.ofHours(25))

        assertEquals(TrialReminderDomainModel.None, classUnderTest.executeInBackground(Unit))
    }

    @Test
    fun `Given the trial has ended then there is no reminder`() {
        givenTrialEndingIn(Duration.ZERO)

        assertEquals(TrialReminderDomainModel.None, classUnderTest.executeInBackground(Unit))
    }

    @Test
    fun `Given it was dismissed for this trial then it does not come back`() {
        val end = givenTrialEndingIn(Duration.ofHours(2))
        given(getTrialBannerDismissalRepository.dismissedTrialEnd()).willReturn(end)

        assertEquals(TrialReminderDomainModel.None, classUnderTest.executeInBackground(Unit))
    }

    @Test
    fun `Given the subscription ended during the trial then there is no reminder`() {
        given(getEntitlementRepository.entitlement()).willReturn(
            subscription(
                trialEnd = NOW.plusSeconds(60)
            ).copy(kind = EntitlementKindDomainModel.None)
        )

        assertEquals(TrialReminderDomainModel.None, classUnderTest.executeInBackground(Unit))
    }

    @Test
    fun `Given a trial unknown on this phone then there is no reminder`() {
        given(getEntitlementRepository.entitlement()).willReturn(EntitlementDomainModel.NONE)

        assertEquals(TrialReminderDomainModel.None, classUnderTest.executeInBackground(Unit))
    }

    @Test
    fun `When dismissing then the trial end it was for is kept`() {
        val repository: DismissTrialBannerRepository = mock()

        DismissTrialReminderUseCase(repository, CoroutineContextProvider.Default)
            .executeInBackground(NOW)

        verify(repository).dismissTrialBanner(NOW)
    }
}
