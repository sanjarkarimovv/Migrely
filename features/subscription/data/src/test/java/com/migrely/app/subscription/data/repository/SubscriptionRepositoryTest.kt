package com.migrely.app.subscription.data.repository

import com.migrely.app.datasource.attack.datasource.AttackDataSource
import com.migrely.app.datasource.attack.model.AttackDataModel
import com.migrely.app.datasource.billing.datasource.BillingDataSource
import com.migrely.app.datasource.billing.model.BillingConnectionDataModel
import com.migrely.app.datasource.billing.model.BillingPurchaseDataModel
import com.migrely.app.datasource.billing.model.BillingPurchaseStateDataModel
import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.datasource.exception.BillingDataException
import com.migrely.app.datasource.exception.BillingUnavailableDataException
import com.migrely.app.datasource.exception.DatabaseDataException
import com.migrely.app.datasource.exception.FileDataException
import com.migrely.app.datasource.preferences.datasource.PreferencesDataSource
import com.migrely.app.datasource.preferences.model.PaywallTriggerDataModel
import com.migrely.app.datasource.schedule.datasource.PremiumCheckScheduleDataSource
import com.migrely.app.subscription.data.mapper.SubscriptionDataMappers
import com.migrely.app.subscription.domain.model.BillingAvailabilityDomainModel
import com.migrely.app.subscription.domain.model.EntitlementDomainModel
import com.migrely.app.subscription.domain.model.MigraineStartDomainModel
import com.migrely.app.subscription.domain.model.OwnedPurchasesResultDomainModel
import com.migrely.app.subscription.domain.model.PaywallMarksDomainModel
import com.migrely.app.subscription.domain.model.PaywallTriggerDomainModel
import com.migrely.app.subscription.domain.model.PremiumProductsResultDomainModel
import com.migrely.app.subscription.domain.model.PurchaseProductDomainModel
import com.migrely.app.subscription.domain.model.SubscriptionWriteResultDomainModel
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

private val NOW: Instant = Instant.parse("2026-10-25T09:00:00Z")

class SubscriptionRepositoryTest {
    private val billingDataSource: BillingDataSource = mock()
    private val preferencesDataSource: PreferencesDataSource = mock()
    private val attackDataSource: AttackDataSource = mock()
    private val clockDataSource: ClockDataSource = mock { on { now() } doReturn NOW }
    private val premiumCheckScheduleDataSource: PremiumCheckScheduleDataSource = mock()
    private val classUnderTest = SubscriptionRepository(
        billingDataSource,
        preferencesDataSource,
        attackDataSource,
        clockDataSource,
        premiumCheckScheduleDataSource,
        SubscriptionDataMappers.create()
    )

    @Test
    fun `Given owned purchases when asking Play then they come back as Migrely's products`() {
        given(billingDataSource.purchases()).willReturn(
            listOf(
                BillingPurchaseDataModel(
                    listOf("migrely_lifetime"),
                    BillingPurchaseStateDataModel.Purchased,
                    1_000L,
                    false,
                    "t",
                    true,
                    true
                )
            )
        )

        val owned = classUnderTest.ownedPurchases() as OwnedPurchasesResultDomainModel.Owned

        assertEquals(PurchaseProductDomainModel.Lifetime, owned.purchases.single().product)
    }

    @Test
    fun `Given no billing or no answer when asking Play then each comes back as a value`() {
        given(billingDataSource.purchases())
            .willThrow(BillingUnavailableDataException())
            .willThrow(BillingDataException())

        assertEquals(OwnedPurchasesResultDomainModel.Unavailable, classUnderTest.ownedPurchases())
        assertEquals(OwnedPurchasesResultDomainModel.Unreachable, classUnderTest.ownedPurchases())
    }

    @Test
    fun `Given Play offers none of the products then Premium is unavailable here`() {
        given(billingDataSource.products(any(), any())).willReturn(emptyList())

        assertEquals(PremiumProductsResultDomainModel.Unavailable, classUnderTest.premiumProducts())
        verify(billingDataSource).products(listOf("migrely_premium"), listOf("migrely_lifetime"))
    }

    @Test
    fun `Given Play does not answer for products then they are unreachable`() {
        given(billingDataSource.products(any(), any())).willThrow(BillingDataException())

        assertEquals(PremiumProductsResultDomainModel.Unreachable, classUnderTest.premiumProducts())
    }

    @Test
    fun `Given the disk refuses when saving the entitlement then the write failed`() {
        given(preferencesDataSource.setEntitlement(any())).willThrow(FileDataException())

        assertEquals(
            SubscriptionWriteResultDomainModel.Failed,
            classUnderTest.saveEntitlement(EntitlementDomainModel.NONE)
        )
    }

    @Test
    fun `Given nothing stored when reading the entitlement then it is none`() {
        assertEquals(EntitlementDomainModel.NONE, classUnderTest.entitlement())
    }

    @Test
    fun `When asking for billing then the connection answers`() {
        given(billingDataSource.connect()).willReturn(BillingConnectionDataModel.Failed)

        assertEquals(BillingAvailabilityDomainModel.Unknown, classUnderTest.billingAvailability())
    }

    @Test
    fun `Given the attacks cannot be read then neither the count nor an attack is known`() {
        val failing: AttackDataSource = mock {
            on { inProgress() } doThrow DatabaseDataException()
            on { overlapping(anyOrNull(), any(), any()) } doThrow DatabaseDataException()
        }
        val repository = SubscriptionRepository(
            billingDataSource,
            preferencesDataSource,
            failing,
            clockDataSource,
            premiumCheckScheduleDataSource,
            SubscriptionDataMappers.create()
        )

        assertNull(repository.attackInProgress())
        assertNull(repository.migraineStarts())
    }

    @Test
    fun `When reading when migraines started then each keeps its zone, up to now`() {
        given(attackDataSource.overlapping(null, Instant.EPOCH, NOW)).willReturn(
            listOf(
                AttackDataModel(
                    id = "a",
                    start = NOW.minusSeconds(60),
                    startZoneId = "Asia/Tashkent",
                    end = null,
                    intensityPoints = emptyList(),
                    longCheckConfirmedAt = null,
                    longCheckDisabled = false
                )
            )
        )

        assertEquals(
            listOf(MigraineStartDomainModel(NOW.minusSeconds(60), ZoneId.of("Asia/Tashkent"))),
            classUnderTest.migraineStarts()
        )
    }

    @Test
    fun `When reading and marking paywalls then each trigger and time is kept`() {
        given(
            preferencesDataSource.paywallShown(PaywallTriggerDataModel.Onboarding)
        ).willReturn(true)
        given(preferencesDataSource.lastPaywallShownMillis()).willReturn(NOW.toEpochMilli())

        assertEquals(
            PaywallMarksDomainModel(setOf(PaywallTriggerDomainModel.Onboarding), NOW),
            classUnderTest.paywallMarks()
        )

        classUnderTest.markPaywallShown(PaywallTriggerDomainModel.FifthMigraine, NOW)

        verify(preferencesDataSource)
            .markPaywallShown(PaywallTriggerDataModel.FifthMigraine, NOW.toEpochMilli())
    }

    @Test
    fun `When dismissing the trial banner then its trial end is kept`() {
        classUnderTest.dismissTrialBanner(NOW)

        verify(preferencesDataSource).dismissTrialBanner(NOW.toEpochMilli())
    }

    @Test
    fun `When managing the subscription then Play's page for Premium opens`() {
        classUnderTest.manageSubscription()

        verify(billingDataSource).manageSubscription("migrely_premium")
    }

    @Test
    fun `When asking for purchase updates then they are Play's own`() {
        val updates = kotlinx.coroutines.flow.flowOf(Unit)
        given(billingDataSource.purchaseUpdates()).willReturn(updates)

        assertEquals(updates, classUnderTest.purchaseUpdates())
    }

    @Test
    fun `Given checks are needed or not then they are planned or cancelled, quietly`() {
        classUnderTest.schedulePremiumChecks(needed = true)
        verify(premiumCheckScheduleDataSource).schedule()

        given(premiumCheckScheduleDataSource.cancel()).willThrow(DatabaseDataException())
        classUnderTest.schedulePremiumChecks(needed = false)
        verify(premiumCheckScheduleDataSource).cancel()
    }
}
