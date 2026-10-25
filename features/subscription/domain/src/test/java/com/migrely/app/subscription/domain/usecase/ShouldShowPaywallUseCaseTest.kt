package com.migrely.app.subscription.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.subscription.domain.model.BillingAvailabilityDomainModel
import com.migrely.app.subscription.domain.model.EntitlementDomainModel
import com.migrely.app.subscription.domain.model.EntitlementKindDomainModel
import com.migrely.app.subscription.domain.model.MigraineStartDomainModel
import com.migrely.app.subscription.domain.model.PaywallMarksDomainModel
import com.migrely.app.subscription.domain.model.PaywallTriggerDomainModel
import com.migrely.app.subscription.domain.repository.GetAttackInProgressRepository
import com.migrely.app.subscription.domain.repository.GetBillingAvailabilityRepository
import com.migrely.app.subscription.domain.repository.GetCurrentTimeRepository
import com.migrely.app.subscription.domain.repository.GetEntitlementRepository
import com.migrely.app.subscription.domain.repository.GetMigraineStartsRepository
import com.migrely.app.subscription.domain.repository.GetPaywallMarksRepository
import com.migrely.app.subscription.domain.repository.GetTimeZoneRepository
import java.time.Duration
import java.time.ZoneId
import java.time.ZoneOffset
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoInteractions

class ShouldShowPaywallUseCaseTest {
    private val getEntitlementRepository: GetEntitlementRepository = mock {
        on { entitlement() } doReturn EntitlementDomainModel.NONE
    }
    private val getPaywallMarksRepository: GetPaywallMarksRepository = mock {
        on { paywallMarks() } doReturn PaywallMarksDomainModel(emptySet(), null)
    }
    private val getAttackInProgressRepository: GetAttackInProgressRepository = mock {
        on { attackInProgress() } doReturn false
    }
    private val getMigraineStartsRepository: GetMigraineStartsRepository = mock {
        on { migraineStarts() } doReturn startsDaysAgo(40, 20, 10, 5, 1)
    }
    private val getTimeZoneRepository: GetTimeZoneRepository = mock {
        on { zone() } doReturn ZoneOffset.UTC
    }
    private val getBillingAvailabilityRepository: GetBillingAvailabilityRepository = mock {
        on { billingAvailability() } doReturn BillingAvailabilityDomainModel.Available
    }
    private val getCurrentTimeRepository: GetCurrentTimeRepository = mock {
        on { now() } doReturn
            NOW
    }
    private val classUnderTest = ShouldShowPaywallUseCase(
        getEntitlementRepository,
        getPaywallMarksRepository,
        getAttackInProgressRepository,
        getMigraineStartsRepository,
        getTimeZoneRepository,
        getBillingAvailabilityRepository,
        getCurrentTimeRepository,
        EntitlementRules(),
        CoroutineContextProvider.Default
    )

    @Test
    fun `Given a free user finishing onboarding then the paywall shows`() {
        assertTrue(classUnderTest.executeInBackground(PaywallTriggerDomainModel.Onboarding))
    }

    @Test
    fun `Given the 5th migraine and no paywall in 7 days then it shows`() {
        given(getPaywallMarksRepository.paywallMarks()).willReturn(
            PaywallMarksDomainModel(
                setOf(PaywallTriggerDomainModel.Onboarding),
                NOW.minus(Duration.ofDays(7))
            )
        )

        assertTrue(classUnderTest.executeInBackground(PaywallTriggerDomainModel.FifthMigraine))
    }

    @Test
    fun `Given the onboarding paywall 3 days ago when logging the 5th migraine then none shows`() {
        given(getPaywallMarksRepository.paywallMarks()).willReturn(
            PaywallMarksDomainModel(
                setOf(PaywallTriggerDomainModel.Onboarding),
                NOW.minus(Duration.ofDays(3))
            )
        )

        assertFalse(classUnderTest.executeInBackground(PaywallTriggerDomainModel.FifthMigraine))
    }

    @Test
    fun `Given the trigger showed one before then it does not show again`() {
        given(getPaywallMarksRepository.paywallMarks()).willReturn(
            PaywallMarksDomainModel(
                setOf(PaywallTriggerDomainModel.FifthMigraine),
                NOW.minus(Duration.ofDays(30))
            )
        )

        assertFalse(classUnderTest.executeInBackground(PaywallTriggerDomainModel.FifthMigraine))
    }

    @Test
    fun `Given 4 migraines then the patterns paywall waits`() {
        given(getMigraineStartsRepository.migraineStarts()).willReturn(startsDaysAgo(40, 20, 10, 5))

        assertFalse(classUnderTest.executeInBackground(PaywallTriggerDomainModel.FifthMigraine))
    }

    @Test
    fun `Given 5 migraines within the first 30 days then no paywall, as Insights show none yet`() {
        given(
            getMigraineStartsRepository.migraineStarts()
        ).willReturn(startsDaysAgo(25, 20, 10, 5, 1))

        assertFalse(classUnderTest.executeInBackground(PaywallTriggerDomainModel.FifthMigraine))
    }

    @Test
    fun `Given 5 migraines but one older than 6 months then the paywall waits as Insights do`() {
        given(getMigraineStartsRepository.migraineStarts())
            .willReturn(startsDaysAgo(250, 40, 20, 10, 5))

        assertFalse(classUnderTest.executeInBackground(PaywallTriggerDomainModel.FifthMigraine))
    }

    @Test
    fun `Given the migraines cannot be read then the patterns paywall waits`() {
        given(getMigraineStartsRepository.migraineStarts()).willReturn(null)

        assertFalse(classUnderTest.executeInBackground(PaywallTriggerDomainModel.FifthMigraine))
    }

    private fun startsDaysAgo(vararg days: Long) = days.map {
        MigraineStartDomainModel(NOW.minus(Duration.ofDays(it)), ZoneId.of("Asia/Tashkent"))
    }

    @Test
    fun `Given an attack in progress, or one that cannot be told, then no paywall shows`() {
        given(getAttackInProgressRepository.attackInProgress()).willReturn(true)
        assertFalse(classUnderTest.executeInBackground(PaywallTriggerDomainModel.Onboarding))

        given(getAttackInProgressRepository.attackInProgress()).willReturn(null)
        assertFalse(classUnderTest.executeInBackground(PaywallTriggerDomainModel.Onboarding))
    }

    @Test
    fun `Given a premium user then no paywall shows and Play is not asked`() {
        given(getEntitlementRepository.entitlement()).willReturn(subscription())

        assertFalse(classUnderTest.executeInBackground(PaywallTriggerDomainModel.Onboarding))
        verifyNoInteractions(getBillingAvailabilityRepository)
    }

    @Test
    fun `Given a pending payment then no paywall shows`() {
        given(getEntitlementRepository.entitlement())
            .willReturn(subscription().copy(kind = EntitlementKindDomainModel.Pending))

        assertFalse(classUnderTest.executeInBackground(PaywallTriggerDomainModel.Onboarding))
    }

    @Test
    fun `Given billing is unavailable or cannot be told then no paywall shows by itself`() {
        given(getBillingAvailabilityRepository.billingAvailability())
            .willReturn(BillingAvailabilityDomainModel.Unavailable)
        assertFalse(classUnderTest.executeInBackground(PaywallTriggerDomainModel.Onboarding))

        // A paywall that could show only an error is no reason to interrupt (walk B-1).
        given(getBillingAvailabilityRepository.billingAvailability())
            .willReturn(BillingAvailabilityDomainModel.Unknown)
        assertFalse(classUnderTest.executeInBackground(PaywallTriggerDomainModel.Onboarding))
    }
}
