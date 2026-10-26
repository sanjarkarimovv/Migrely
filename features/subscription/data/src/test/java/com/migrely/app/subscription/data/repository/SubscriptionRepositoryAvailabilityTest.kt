package com.migrely.app.subscription.data.repository

import com.migrely.app.datasource.attack.datasource.AttackDataSource
import com.migrely.app.datasource.billing.datasource.BillingDataSource
import com.migrely.app.datasource.billing.model.BillingConnectionDataModel
import com.migrely.app.datasource.billing.model.BillingOfferDataModel
import com.migrely.app.datasource.billing.model.BillingProductDataModel
import com.migrely.app.datasource.billing.model.BillingProductKindDataModel
import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.datasource.exception.BillingDataException
import com.migrely.app.datasource.preferences.datasource.PreferencesDataSource
import com.migrely.app.datasource.schedule.datasource.PremiumCheckScheduleDataSource
import com.migrely.app.subscription.data.mapper.SubscriptionDataMappers
import com.migrely.app.subscription.domain.model.BillingAvailabilityDomainModel
import com.migrely.app.subscription.domain.model.PremiumProductsResultDomainModel
import java.time.Duration
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

private val START: Instant = Instant.parse("2026-10-25T09:00:00Z")

private val LIFETIME = BillingProductDataModel(
    "migrely_lifetime",
    BillingProductKindDataModel.OneTime,
    listOf(BillingOfferDataModel(null, null, "t-lifetime", "€59.99", null, null))
)

/** A paywall shows itself only when Google Play sells Premium here (BRD D110). */
class SubscriptionRepositoryAvailabilityTest {
    private var now = START
    private val billingDataSource: BillingDataSource = mock {
        on { connect() } doAnswer { BillingConnectionDataModel.Available }
    }
    private val clockDataSource: ClockDataSource = mock { on { now() } doAnswer { now } }
    private val classUnderTest = SubscriptionRepository(
        billingDataSource,
        mock<PreferencesDataSource>(),
        mock<AttackDataSource>(),
        clockDataSource,
        mock<PremiumCheckScheduleDataSource>(),
        SubscriptionDataMappers.create()
    )

    @Test
    fun `Given Play connects but offers none of the products then Premium is unavailable`() {
        given(billingDataSource.products(any(), any())).willReturn(emptyList())

        assertEquals(
            BillingAvailabilityDomainModel.Unavailable,
            classUnderTest.billingAvailability()
        )
    }

    @Test
    fun `Given Play offers the products then billing is available`() {
        given(billingDataSource.products(any(), any())).willReturn(listOf(LIFETIME))

        assertEquals(BillingAvailabilityDomainModel.Available, classUnderTest.billingAvailability())
    }

    @Test
    fun `Given no billing on the phone then unavailable without asking for products`() {
        given(billingDataSource.connect()).willReturn(BillingConnectionDataModel.Unavailable)

        assertEquals(
            BillingAvailabilityDomainModel.Unavailable,
            classUnderTest.billingAvailability()
        )
        verify(billingDataSource, never()).products(any(), any())
    }

    @Test
    fun `Given an answer within the hour when asking again then Play is not asked`() {
        given(billingDataSource.products(any(), any())).willReturn(emptyList())

        classUnderTest.billingAvailability()
        now = START.plus(PREMIUM_OFFER_MEMORY).minusSeconds(1)

        assertEquals(
            BillingAvailabilityDomainModel.Unavailable,
            classUnderTest.billingAvailability()
        )
        verify(billingDataSource, times(1)).connect()
        verify(billingDataSource, times(1)).products(any(), any())
    }

    @Test
    fun `Given an answer an hour old when asking again then Play is asked again`() {
        given(billingDataSource.products(any(), any()))
            .willReturn(emptyList())
            .willReturn(listOf(LIFETIME))

        classUnderTest.billingAvailability()
        now = START.plus(PREMIUM_OFFER_MEMORY)

        assertEquals(BillingAvailabilityDomainModel.Available, classUnderTest.billingAvailability())
        verify(billingDataSource, times(2)).products(any(), any())
    }

    @Test
    fun `Given the clock was set back when asking again then Play is asked again`() {
        given(billingDataSource.products(any(), any())).willReturn(emptyList())

        classUnderTest.billingAvailability()
        now = START.minus(Duration.ofMinutes(1))
        classUnderTest.billingAvailability()

        verify(billingDataSource, times(2)).products(any(), any())
    }

    @Test
    fun `Given Play did not answer for the products then it is unknown and asked again`() {
        given(billingDataSource.products(any(), any()))
            .willThrow(BillingDataException())
            .willReturn(listOf(LIFETIME))

        assertEquals(BillingAvailabilityDomainModel.Unknown, classUnderTest.billingAvailability())
        assertEquals(BillingAvailabilityDomainModel.Available, classUnderTest.billingAvailability())
    }

    @Test
    fun `Given the paywall just read the products then billing asks Play nothing`() {
        given(billingDataSource.products(any(), any())).willReturn(listOf(LIFETIME))

        assertEquals(
            PremiumProductsResultDomainModel.Loaded::class,
            classUnderTest.premiumProducts()::class
        )

        assertEquals(BillingAvailabilityDomainModel.Available, classUnderTest.billingAvailability())
        verify(billingDataSource, never()).connect()
        verify(billingDataSource, times(1)).products(any(), any())
    }

    @Test
    fun `Given a remembered answer when the paywall opens then it still reads Play afresh`() {
        given(billingDataSource.products(any(), any()))
            .willReturn(emptyList())
            .willReturn(listOf(LIFETIME))

        classUnderTest.billingAvailability()

        assertEquals(
            PremiumProductsResultDomainModel.Loaded::class,
            classUnderTest.premiumProducts()::class
        )
        assertEquals(BillingAvailabilityDomainModel.Available, classUnderTest.billingAvailability())
    }
}
