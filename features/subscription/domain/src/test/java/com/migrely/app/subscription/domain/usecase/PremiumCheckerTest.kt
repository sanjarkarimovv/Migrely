package com.migrely.app.subscription.domain.usecase

import com.migrely.app.subscription.domain.model.BillingAvailabilityDomainModel
import com.migrely.app.subscription.domain.model.EntitlementDomainModel
import com.migrely.app.subscription.domain.model.EntitlementKindDomainModel
import com.migrely.app.subscription.domain.model.OwnedPurchasesResultDomainModel
import com.migrely.app.subscription.domain.model.PremiumCheckDomainModel
import com.migrely.app.subscription.domain.model.PremiumStatusDomainModel
import com.migrely.app.subscription.domain.model.PurchaseProductDomainModel
import com.migrely.app.subscription.domain.model.PurchaseStateDomainModel
import com.migrely.app.subscription.domain.repository.AcknowledgePurchaseRepository
import com.migrely.app.subscription.domain.repository.GetCurrentTimeRepository
import com.migrely.app.subscription.domain.repository.GetEntitlementRepository
import com.migrely.app.subscription.domain.repository.GetOwnedPurchasesRepository
import com.migrely.app.subscription.domain.repository.SaveEntitlementRepository
import com.migrely.app.subscription.domain.repository.SchedulePremiumChecksRepository
import java.time.Duration
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

class PremiumCheckerTest {
    private val getEntitlementRepository: GetEntitlementRepository = mock {
        on { entitlement() } doReturn EntitlementDomainModel.NONE
    }
    private val saveEntitlementRepository: SaveEntitlementRepository = mock()
    private val getOwnedPurchasesRepository: GetOwnedPurchasesRepository = mock()
    private val acknowledgePurchaseRepository: AcknowledgePurchaseRepository = mock()
    private val getCurrentTimeRepository: GetCurrentTimeRepository = mock {
        on { now() } doReturn NOW
    }
    private val schedulePremiumChecksRepository: SchedulePremiumChecksRepository = mock()
    private val classUnderTest = PremiumChecker(
        getEntitlementRepository,
        saveEntitlementRepository,
        getOwnedPurchasesRepository,
        acknowledgePurchaseRepository,
        getCurrentTimeRepository,
        schedulePremiumChecksRepository,
        EntitlementRules()
    )

    @Test
    fun `Given a new purchase when checking then it is acknowledged, kept and unlocks`() {
        given(getOwnedPurchasesRepository.ownedPurchases()).willReturn(
            OwnedPurchasesResultDomainModel.Owned(
                listOf(
                    purchase(PurchaseProductDomainModel.Lifetime, token = "l", acknowledged = false)
                )
            )
        )

        val check = classUnderTest.check()

        verify(acknowledgePurchaseRepository).acknowledgePurchase("l")
        val saved = argumentCaptor<EntitlementDomainModel>()
        verify(saveEntitlementRepository).saveEntitlement(saved.capture())
        assertEquals(EntitlementKindDomainModel.Lifetime, saved.firstValue.kind)
        assertEquals(
            PremiumCheckDomainModel(
                PremiumStatusDomainModel.Lifetime(false),
                BillingAvailabilityDomainModel.Available
            ),
            check
        )
    }

    @Test
    fun `Given a pending or forged purchase when checking then neither is acknowledged`() {
        given(getOwnedPurchasesRepository.ownedPurchases()).willReturn(
            OwnedPurchasesResultDomainModel.Owned(
                listOf(
                    purchase(state = PurchaseStateDomainModel.Pending, acknowledged = false),
                    purchase(token = "forged", acknowledged = false, signatureValid = false)
                )
            )
        )

        classUnderTest.check()

        verify(acknowledgePurchaseRepository, never()).acknowledgePurchase(any())
    }

    @Test
    fun `Given Play cannot be reached for 4 days when checking then the subscription ends`() {
        val stale = subscription(lastCheck = NOW.minus(Duration.ofDays(4)))
        given(getEntitlementRepository.entitlement()).willReturn(stale)
        given(getOwnedPurchasesRepository.ownedPurchases())
            .willReturn(OwnedPurchasesResultDomainModel.Unreachable)

        val check = classUnderTest.check()

        verify(saveEntitlementRepository).saveEntitlement(
            stale.copy(kind = EntitlementKindDomainModel.None)
        )
        assertEquals(
            PremiumCheckDomainModel(
                PremiumStatusDomainModel.Free,
                BillingAvailabilityDomainModel.Unknown
            ),
            check
        )
    }

    @Test
    fun `Given billing is unavailable and nothing changes when checking then nothing is written`() {
        given(getOwnedPurchasesRepository.ownedPurchases())
            .willReturn(OwnedPurchasesResultDomainModel.Unavailable)

        val check = classUnderTest.check()

        verifyNoInteractions(saveEntitlementRepository, acknowledgePurchaseRepository)
        assertEquals(BillingAvailabilityDomainModel.Unavailable, check.billing)
    }

    @Test
    fun `Given a check a minute ago when asking for the current status then Play is not asked`() {
        given(getEntitlementRepository.entitlement())
            .willReturn(subscription(lastCheck = NOW.minusSeconds(60)))

        val status = classUnderTest.current()

        verifyNoInteractions(getOwnedPurchasesRepository)
        assertEquals(PremiumStatusDomainModel.Subscribed(MONTHLY.plan, null), status)
    }

    @Test
    fun `Given an old check when asking for the current status then Play is asked`() {
        given(getEntitlementRepository.entitlement())
            .willReturn(subscription(lastCheck = NOW.minus(Duration.ofHours(1))))
        given(getOwnedPurchasesRepository.ownedPurchases())
            .willReturn(OwnedPurchasesResultDomainModel.Owned(emptyList()))

        assertEquals(PremiumStatusDomainModel.Free, classUnderTest.current())
    }

    @Test
    fun `Given a fresh install with no cache then Free until Play reports the purchase`() {
        // A reinstall or a restored backup: the entitlement is never backed up (BRD D115).
        given(getOwnedPurchasesRepository.ownedPurchases())
            .willReturn(OwnedPurchasesResultDomainModel.Unreachable)
            .willReturn(
                OwnedPurchasesResultDomainModel.Owned(
                    listOf(purchase(PurchaseProductDomainModel.Lifetime, token = "life"))
                )
            )

        assertEquals(PremiumStatusDomainModel.Free, classUnderTest.current())
        verifyNoInteractions(saveEntitlementRepository)

        assertEquals(PremiumStatusDomainModel.Lifetime(false), classUnderTest.current())
    }

    @Test
    fun `Given a phone on another Google account then Play's empty answer keeps it Free`() {
        given(getOwnedPurchasesRepository.ownedPurchases())
            .willReturn(OwnedPurchasesResultDomainModel.Owned(emptyList()))

        assertEquals(PremiumStatusDomainModel.Free, classUnderTest.current())
    }

    @Test
    fun `Given two checks at once then they run one after the other`() {
        val inside = java.util.concurrent.atomic.AtomicInteger()
        var most = 0
        given(getOwnedPurchasesRepository.ownedPurchases()).willAnswer {
            most = maxOf(most, inside.incrementAndGet())
            Thread.sleep(50L)
            inside.decrementAndGet()
            OwnedPurchasesResultDomainModel.Owned(emptyList())
        }

        val threads = List(4) { kotlin.concurrent.thread { classUnderTest.check() } }
        threads.forEach { it.join(5_000L) }

        assertEquals(1, most)
    }

    @Test
    fun `Given acknowledging fails then it is marked and checks run in the background`() {
        given(getOwnedPurchasesRepository.ownedPurchases()).willReturn(
            OwnedPurchasesResultDomainModel.Owned(
                listOf(purchase(PurchaseProductDomainModel.Lifetime, acknowledged = false))
            )
        )
        given(acknowledgePurchaseRepository.acknowledgePurchase(any())).willReturn(false)

        classUnderTest.check()

        val saved = argumentCaptor<EntitlementDomainModel>()
        verify(saveEntitlementRepository).saveEntitlement(saved.capture())
        assertEquals(true, saved.firstValue.acknowledgementPending)
        assertEquals(true, EntitlementRules().isDue(saved.firstValue, NOW))
        verify(schedulePremiumChecksRepository).schedulePremiumChecks(true)
    }

    @Test
    fun `Given the acknowledgement goes through then no background checks are planned`() {
        given(getOwnedPurchasesRepository.ownedPurchases()).willReturn(
            OwnedPurchasesResultDomainModel.Owned(
                listOf(purchase(PurchaseProductDomainModel.Lifetime, acknowledged = false))
            )
        )
        given(acknowledgePurchaseRepository.acknowledgePurchase(any())).willReturn(true)

        classUnderTest.check()

        val saved = argumentCaptor<EntitlementDomainModel>()
        verify(saveEntitlementRepository).saveEntitlement(saved.capture())
        assertEquals(false, saved.firstValue.acknowledgementPending)
        verify(schedulePremiumChecksRepository).schedulePremiumChecks(false)
    }

    @Test
    fun `Given a pending payment then background checks are planned`() {
        given(getOwnedPurchasesRepository.ownedPurchases()).willReturn(
            OwnedPurchasesResultDomainModel.Owned(
                listOf(purchase(state = PurchaseStateDomainModel.Pending))
            )
        )

        classUnderTest.check()

        verify(schedulePremiumChecksRepository).schedulePremiumChecks(true)
    }
}
