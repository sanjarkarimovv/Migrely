package com.migrely.app.subscription.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.subscription.domain.model.BillingAvailabilityDomainModel
import com.migrely.app.subscription.domain.model.BoughtDomainModel
import com.migrely.app.subscription.domain.model.BuyPremiumResultDomainModel
import com.migrely.app.subscription.domain.model.EntitlementDomainModel
import com.migrely.app.subscription.domain.model.PremiumCheckDomainModel
import com.migrely.app.subscription.domain.model.PremiumPlanDomainModel
import com.migrely.app.subscription.domain.model.PremiumStatusDomainModel
import com.migrely.app.subscription.domain.model.PurchaseLaunchDomainModel
import com.migrely.app.subscription.domain.model.PurchaseOutcomeDomainModel
import com.migrely.app.subscription.domain.model.PurchaseProductDomainModel
import com.migrely.app.subscription.domain.model.PurchaseSheetResultDomainModel
import com.migrely.app.subscription.domain.model.PurchaseStateDomainModel
import com.migrely.app.subscription.domain.repository.GetEntitlementRepository
import com.migrely.app.subscription.domain.repository.LaunchPurchaseRepository
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

private val YEARLY_STATUS = PremiumStatusDomainModel.Subscribed(PremiumPlanDomainModel.Yearly, null)

class BuyPremiumUseCaseTest {
    private val getEntitlementRepository: GetEntitlementRepository = mock {
        on { entitlement() } doReturn EntitlementDomainModel.NONE
    }
    private val launchPurchaseRepository: LaunchPurchaseRepository = mock()
    private val premiumChecker: PremiumChecker = mock {
        on { check(anyOrNull()) } doReturn
            PremiumCheckDomainModel(YEARLY_STATUS, BillingAvailabilityDomainModel.Available)
    }
    private val classUnderTest = BuyPremiumUseCase(
        getEntitlementRepository,
        launchPurchaseRepository,
        premiumChecker,
        CoroutineContextProvider.Default
    )

    @Test
    fun `Given a free user buys the yearly trial then it unlocks and the trial is kept`() {
        given(
            launchPurchaseRepository.launchPurchase(PurchaseLaunchDomainModel(YEARLY_TRIAL, null))
        )
            .willReturn(PurchaseSheetResultDomainModel.Completed(listOf(purchase(token = "new"))))

        val result = classUnderTest.executeInBackground(YEARLY_TRIAL)

        verify(premiumChecker).check(
            BoughtDomainModel("new", PremiumPlanDomainModel.Yearly, freeTrial = true)
        )
        assertEquals(
            BuyPremiumResultDomainModel(PurchaseOutcomeDomainModel.Unlocked, YEARLY_STATUS),
            result
        )
    }

    @Test
    fun `Given a monthly subscriber chooses yearly then the monthly purchase is replaced`() {
        given(getEntitlementRepository.entitlement())
            .willReturn(subscription(PremiumPlanDomainModel.Monthly, token = "monthly"))
        given(launchPurchaseRepository.launchPurchase(any()))
            .willReturn(PurchaseSheetResultDomainModel.Cancelled)

        classUnderTest.executeInBackground(YEARLY_TRIAL)

        verify(launchPurchaseRepository)
            .launchPurchase(
                PurchaseLaunchDomainModel(YEARLY_TRIAL, replacedPurchaseToken = "monthly")
            )
    }

    @Test
    fun `Given a subscriber buys lifetime then no subscription is replaced`() {
        given(getEntitlementRepository.entitlement()).willReturn(subscription(token = "monthly"))
        given(launchPurchaseRepository.launchPurchase(any()))
            .willReturn(PurchaseSheetResultDomainModel.Cancelled)

        classUnderTest.executeInBackground(LIFETIME)

        verify(launchPurchaseRepository).launchPurchase(PurchaseLaunchDomainModel(LIFETIME, null))
    }

    @Test
    fun `Given the payment completes later then the outcome is pending`() {
        given(launchPurchaseRepository.launchPurchase(any())).willReturn(
            PurchaseSheetResultDomainModel.Completed(
                listOf(
                    purchase(
                        PurchaseProductDomainModel.Lifetime,
                        state = PurchaseStateDomainModel.Pending
                    )
                )
            )
        )
        given(premiumChecker.check(anyOrNull())).willReturn(
            PremiumCheckDomainModel(
                PremiumStatusDomainModel.Pending,
                BillingAvailabilityDomainModel.Available
            )
        )

        assertEquals(
            PurchaseOutcomeDomainModel.Pending,
            classUnderTest.executeInBackground(LIFETIME).outcome
        )
    }

    @Test
    fun `Given a completed purchase that does not check out then buying failed`() {
        given(launchPurchaseRepository.launchPurchase(any())).willReturn(
            PurchaseSheetResultDomainModel.Completed(listOf(purchase(signatureValid = false)))
        )
        given(premiumChecker.check(anyOrNull())).willReturn(
            PremiumCheckDomainModel(
                PremiumStatusDomainModel.Free,
                BillingAvailabilityDomainModel.Available
            )
        )

        assertEquals(
            PurchaseOutcomeDomainModel.Failed,
            classUnderTest.executeInBackground(MONTHLY).outcome
        )
    }

    @Test
    fun `Given Play says it is already owned then the check unlocks it`() {
        given(launchPurchaseRepository.launchPurchase(any()))
            .willReturn(PurchaseSheetResultDomainModel.AlreadyOwned)

        assertEquals(
            PurchaseOutcomeDomainModel.Unlocked,
            classUnderTest.executeInBackground(LIFETIME).outcome
        )
    }

    @Test
    fun `Given the sheet is cancelled then the outcome says so with the status Play holds`() {
        given(launchPurchaseRepository.launchPurchase(any()))
            .willReturn(PurchaseSheetResultDomainModel.Cancelled)

        assertEquals(
            BuyPremiumResultDomainModel(PurchaseOutcomeDomainModel.Cancelled, YEARLY_STATUS),
            classUnderTest.executeInBackground(MONTHLY)
        )
        verify(premiumChecker).check(null)
    }

    @Test
    fun `Given billing is unavailable then the outcome says so`() {
        given(launchPurchaseRepository.launchPurchase(any()))
            .willReturn(PurchaseSheetResultDomainModel.Unavailable)

        assertEquals(
            PurchaseOutcomeDomainModel.Unavailable,
            classUnderTest.executeInBackground(MONTHLY).outcome
        )
    }
}
