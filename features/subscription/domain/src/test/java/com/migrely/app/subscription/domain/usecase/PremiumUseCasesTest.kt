package com.migrely.app.subscription.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.subscription.domain.model.BillingAvailabilityDomainModel
import com.migrely.app.subscription.domain.model.PaywallDomainModel
import com.migrely.app.subscription.domain.model.PaywallOfferDomainModel
import com.migrely.app.subscription.domain.model.PaywallOffersDomainModel
import com.migrely.app.subscription.domain.model.PaywallTriggerDomainModel
import com.migrely.app.subscription.domain.model.PremiumCheckDomainModel
import com.migrely.app.subscription.domain.model.PremiumPlanDomainModel
import com.migrely.app.subscription.domain.model.PremiumProductsResultDomainModel
import com.migrely.app.subscription.domain.model.PremiumStatusDomainModel
import com.migrely.app.subscription.domain.model.RefreshPremiumRequestDomainModel
import com.migrely.app.subscription.domain.repository.GetAttackInProgressRepository
import com.migrely.app.subscription.domain.repository.GetCurrentTimeRepository
import com.migrely.app.subscription.domain.repository.GetPremiumProductsRepository
import com.migrely.app.subscription.domain.repository.ManageSubscriptionRepository
import com.migrely.app.subscription.domain.repository.MarkPaywallShownRepository
import com.migrely.app.subscription.domain.repository.ShowPaymentMessagesRepository
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.given
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

private val CHECKED = PremiumCheckDomainModel(
    PremiumStatusDomainModel.Lifetime(subscriptionRenewsToo = false),
    BillingAvailabilityDomainModel.Available
)

/** The smaller use cases around the entitlement. */
class PremiumUseCasesTest {
    private val premiumChecker: PremiumChecker = mock { on { check(null) } doReturn CHECKED }
    private val coroutines = CoroutineContextProvider.Default

    @Test
    fun `Given a refresh that is not forced then the checker decides whether to ask Play`() {
        given(premiumChecker.current()).willReturn(PremiumStatusDomainModel.Free)

        val status = RefreshPremiumUseCase(premiumChecker, coroutines)
            .executeInBackground(RefreshPremiumRequestDomainModel(force = false))

        assertEquals(PremiumStatusDomainModel.Free, status)
        verify(premiumChecker, never()).check(null)
    }

    @Test
    fun `Given a forced refresh then Play is asked`() {
        val status = RefreshPremiumUseCase(premiumChecker, coroutines)
            .executeInBackground(RefreshPremiumRequestDomainModel(force = true))

        assertEquals(CHECKED.status, status)
    }

    @Test
    fun `When restoring purchases then Play is asked again`() {
        assertEquals(
            CHECKED,
            RestorePurchasesUseCase(premiumChecker, coroutines).executeInBackground(Unit)
        )
    }

    @Test
    fun `When managing the subscription then its page opens and the status is read again`() {
        val repository: ManageSubscriptionRepository = mock()

        val status = ManageSubscriptionUseCase(repository, premiumChecker, coroutines)
            .executeInBackground(Unit)

        inOrder(repository, premiumChecker) {
            verify(repository).manageSubscription()
            verify(premiumChecker).check(null)
        }
        assertEquals(CHECKED.status, status)
    }

    @Test
    fun `When a paywall shows itself then its trigger is marked at the current time`() {
        val repository: MarkPaywallShownRepository = mock()
        val time: GetCurrentTimeRepository = mock { on { now() } doReturn NOW }

        MarkPaywallShownUseCase(repository, time, coroutines)
            .executeInBackground(PaywallTriggerDomainModel.FifthMigraine)

        verify(repository).markPaywallShown(PaywallTriggerDomainModel.FifthMigraine, NOW)
    }

    @Test
    fun `Given the user fixed the payment in Play's message then Play is asked again`() {
        val attack: GetAttackInProgressRepository =
            mock { on { attackInProgress() } doReturn false }
        val messages: ShowPaymentMessagesRepository = mock {
            on { showPaymentMessages() } doReturn true
        }

        ShowPaymentMessagesUseCase(attack, messages, premiumChecker, coroutines)
            .executeInBackground(Unit)

        verify(premiumChecker).check(null)
    }

    @Test
    fun `Given an attack in progress then Play's messages wait`() {
        val attack: GetAttackInProgressRepository = mock { on { attackInProgress() } doReturn true }
        val messages: ShowPaymentMessagesRepository = mock()

        ShowPaymentMessagesUseCase(attack, messages, premiumChecker, coroutines)
            .executeInBackground(Unit)

        verifyNoInteractions(messages)
    }

    @Test
    fun `Given a monthly subscriber then the paywall offers yearly and lifetime only`() {
        given(premiumChecker.current())
            .willReturn(PremiumStatusDomainModel.Subscribed(PremiumPlanDomainModel.Monthly, null))
        val products: GetPremiumProductsRepository = mock {
            on { premiumProducts() } doReturn PremiumProductsResultDomainModel.Loaded(
                listOf(YEARLY_TRIAL, MONTHLY, LIFETIME)
            )
        }

        val paywall = GetPaywallUseCase(
            premiumChecker,
            products,
            coroutines
        ).executeInBackground(Unit)

        assertEquals(
            PaywallOffersDomainModel.Loaded(
                listOf(
                    PaywallOfferDomainModel(YEARLY_TRIAL, current = false, choosable = true),
                    PaywallOfferDomainModel(MONTHLY, current = true, choosable = false),
                    PaywallOfferDomainModel(LIFETIME, current = false, choosable = true)
                )
            ),
            paywall.offers
        )
    }

    @Test
    fun `Given a yearly subscriber then only lifetime can be chosen`() {
        given(premiumChecker.current())
            .willReturn(PremiumStatusDomainModel.Subscribed(PremiumPlanDomainModel.Yearly, null))
        val products: GetPremiumProductsRepository = mock {
            on { premiumProducts() } doReturn
                PremiumProductsResultDomainModel.Loaded(listOf(YEARLY_TRIAL, MONTHLY, LIFETIME))
        }

        val offers = GetPaywallUseCase(premiumChecker, products, coroutines)
            .executeInBackground(Unit).offers as PaywallOffersDomainModel.Loaded

        assertEquals(listOf(false, false, true), offers.offers.map { it.choosable })
    }

    @Test
    fun `Given billing is unavailable then the paywall says so with the status`() {
        given(premiumChecker.current()).willReturn(PremiumStatusDomainModel.Free)
        val products: GetPremiumProductsRepository = mock {
            on { premiumProducts() } doReturn PremiumProductsResultDomainModel.Unavailable
        }

        assertEquals(
            PaywallDomainModel(PremiumStatusDomainModel.Free, PaywallOffersDomainModel.Unavailable),
            GetPaywallUseCase(premiumChecker, products, coroutines).executeInBackground(Unit)
        )
    }
}
