package com.migrely.app.subscription.presentation.viewmodel

import com.migrely.app.architecture.presentation.viewmodel.BaseViewModelTest
import com.migrely.app.subscription.domain.model.BillingAvailabilityDomainModel
import com.migrely.app.subscription.domain.model.BuyPremiumResultDomainModel
import com.migrely.app.subscription.domain.model.PaywallDomainModel
import com.migrely.app.subscription.domain.model.PaywallOfferDomainModel
import com.migrely.app.subscription.domain.model.PaywallOffersDomainModel
import com.migrely.app.subscription.domain.model.PaywallTriggerDomainModel
import com.migrely.app.subscription.domain.model.PremiumCheckDomainModel
import com.migrely.app.subscription.domain.model.PremiumPlanDomainModel
import com.migrely.app.subscription.domain.model.PremiumProductDomainModel
import com.migrely.app.subscription.domain.model.PremiumStatusDomainModel
import com.migrely.app.subscription.domain.model.PurchaseOutcomeDomainModel
import com.migrely.app.subscription.domain.usecase.BuyPremiumUseCase
import com.migrely.app.subscription.domain.usecase.GetPaywallUseCase
import com.migrely.app.subscription.domain.usecase.ManageSubscriptionUseCase
import com.migrely.app.subscription.domain.usecase.MarkPaywallShownUseCase
import com.migrely.app.subscription.domain.usecase.RestorePurchasesUseCase
import com.migrely.app.subscription.presentation.mapper.PaywallPresentationMapper
import com.migrely.app.subscription.presentation.mapper.PaywallProductDomainMapper
import com.migrely.app.subscription.presentation.mapper.PaywallTriggerDomainMapper
import com.migrely.app.subscription.presentation.mapper.PremiumPlanPresentationMapper
import com.migrely.app.subscription.presentation.mapper.PremiumStatusPresentationMapper
import com.migrely.app.subscription.presentation.model.PaywallContextPresentationModel
import com.migrely.app.subscription.presentation.model.PaywallPresentationNotification
import com.migrely.app.subscription.presentation.model.PaywallProductPresentationModel
import com.migrely.app.subscription.presentation.model.PaywallViewState
import com.migrely.app.subscription.presentation.model.PremiumPlanPresentationModel
import com.migrely.app.subscription.presentation.model.PremiumStatusPresentationModel
import com.migrely.app.subscription.presentation.navigation.PaywallPresentationNavigationEvent
import java.time.Period
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

private val YEARLY =
    PremiumProductDomainModel(PremiumPlanDomainModel.Yearly, "€29.99", Period.ofDays(7), "t")
private val YEARLY_CARD = PaywallProductPresentationModel(
    PremiumPlanPresentationModel.Yearly,
    "€29.99",
    freeTrial = Period.ofDays(7),
    offerToken = "t",
    current = false,
    choosable = true
)
private val FREE_PAYWALL = PaywallDomainModel(
    PremiumStatusDomainModel.Free,
    PaywallOffersDomainModel.Loaded(listOf(PaywallOfferDomainModel(YEARLY, false, true)))
)
private val READY = PaywallViewState.Ready(PremiumStatusPresentationModel.Free, listOf(YEARLY_CARD))
private val YEARLY_STATUS = PremiumStatusDomainModel.Subscribed(PremiumPlanDomainModel.Yearly, null)
private val REPORT = PaywallContextPresentationModel.Report
private val UNLOCKED = PaywallPresentationNotification.Unlocked(
    PremiumStatusPresentationModel.Subscribed(PremiumPlanPresentationModel.Yearly, null)
)

@RunWith(MockitoJUnitRunner::class)
class PaywallViewModelTest :
    BaseViewModelTest<PaywallViewState, PaywallPresentationNotification, PaywallViewModel>() {
    @Mock private lateinit var getPaywallUseCase: GetPaywallUseCase

    @Mock private lateinit var buyPremiumUseCase: BuyPremiumUseCase

    @Mock private lateinit var restorePurchasesUseCase: RestorePurchasesUseCase

    @Mock private lateinit var manageSubscriptionUseCase: ManageSubscriptionUseCase

    @Mock private lateinit var markPaywallShownUseCase: MarkPaywallShownUseCase

    @Before
    fun setUp() {
        val planMapper = PremiumPlanPresentationMapper()
        val statusMapper = PremiumStatusPresentationMapper(planMapper)
        classUnderTest = PaywallViewModel(
            PaywallUseCases(
                getPaywallUseCase,
                buyPremiumUseCase,
                restorePurchasesUseCase,
                manageSubscriptionUseCase,
                markPaywallShownUseCase
            ),
            PaywallPresentationMapper(statusMapper, planMapper),
            statusMapper,
            PaywallProductDomainMapper(),
            PaywallTriggerDomainMapper(),
            useCaseExecutor
        )
    }

    private suspend fun notificationOf(action: () -> Unit): PaywallPresentationNotification =
        coroutineScope {
            val notification = async(start = UNDISPATCHED) { classUnderTest.notification.first() }
            action()
            notification.await()
        }

    @Test
    fun `Given the paywall showed itself after onboarding when entering then it is marked`() =
        runTest {
            getPaywallUseCase.givenSuccessfulExecution(FREE_PAYWALL)
            val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }

            classUnderTest.onEnter(PaywallContextPresentationModel.Onboarding)

            assertEquals(READY, state.await())
            verify(useCaseExecutor).execute(
                eq(markPaywallShownUseCase),
                eq(PaywallTriggerDomainModel.Onboarding),
                any(),
                any()
            )
        }

    @Test
    fun `Given a tap on the doctor report when entering then nothing is marked`() = runTest {
        getPaywallUseCase.givenSuccessfulExecution(FREE_PAYWALL)

        classUnderTest.onEnter(PaywallContextPresentationModel.Report)

        verify(useCaseExecutor, never()).execute(eq(markPaywallShownUseCase), any(), any(), any())
    }

    @Test
    fun `When buying then the sheet works, and Premium unlocking shows its success`() = runTest {
        getPaywallUseCase.givenSuccessfulExecution(FREE_PAYWALL)
        buyPremiumUseCase.givenSuccessfulExecution(
            YEARLY,
            BuyPremiumResultDomainModel(PurchaseOutcomeDomainModel.Unlocked, YEARLY_STATUS)
        )
        val states = async(start = UNDISPATCHED) { classUnderTest.viewState.take(2).toList() }

        val notification = notificationOf { classUnderTest.onBuyAction(REPORT, YEARLY_CARD) }

        assertEquals(UNLOCKED, notification)
        assertEquals(listOf(PaywallViewState.Working, READY), states.await())
    }

    @Test
    fun `Given buying failed then the paywall says so`() = runTest {
        getPaywallUseCase.givenSuccessfulExecution(FREE_PAYWALL)
        buyPremiumUseCase.givenSuccessfulExecution(
            YEARLY,
            BuyPremiumResultDomainModel(
                PurchaseOutcomeDomainModel.Failed,
                PremiumStatusDomainModel.Free
            )
        )

        assertEquals(
            PaywallPresentationNotification.SomethingWentWrong,
            notificationOf { classUnderTest.onBuyAction(REPORT, YEARLY_CARD) }
        )
    }

    @Test
    fun `Given a pending payment then the paywall reloads to show it, with no message`() = runTest {
        val pending = FREE_PAYWALL.copy(status = PremiumStatusDomainModel.Pending)
        getPaywallUseCase.givenSuccessfulExecution(pending)
        buyPremiumUseCase.givenSuccessfulExecution(
            YEARLY,
            BuyPremiumResultDomainModel(
                PurchaseOutcomeDomainModel.Pending,
                PremiumStatusDomainModel.Pending
            )
        )
        val states = async(start = UNDISPATCHED) { classUnderTest.viewState.take(2).toList() }

        classUnderTest.onBuyAction(REPORT, YEARLY_CARD)

        assertEquals(
            READY.copy(status = PremiumStatusPresentationModel.Pending),
            states.await().last()
        )
    }

    @Test
    fun `Given the sheet is open when buying again then a second sheet does not open`() = runTest {
        classUnderTest.onBuyAction(REPORT, YEARLY_CARD)
        classUnderTest.onBuyAction(REPORT, YEARLY_CARD)

        verify(useCaseExecutor, times(1)).execute(eq(buyPremiumUseCase), any(), any(), any())
    }

    @Test
    fun `Given nothing was bought when restoring then the paywall says so`() = runTest {
        getPaywallUseCase.givenSuccessfulExecution(FREE_PAYWALL)
        restorePurchasesUseCase.givenSuccessfulExecution(
            PremiumCheckDomainModel(
                PremiumStatusDomainModel.Free,
                BillingAvailabilityDomainModel.Available
            )
        )

        assertEquals(
            PaywallPresentationNotification.NothingToRestore,
            notificationOf { classUnderTest.onRestoreAction(REPORT) }
        )
    }

    @Test
    fun `Given Play does not answer when restoring then the paywall says so`() = runTest {
        getPaywallUseCase.givenSuccessfulExecution(FREE_PAYWALL)
        restorePurchasesUseCase.givenSuccessfulExecution(
            PremiumCheckDomainModel(
                PremiumStatusDomainModel.Free,
                BillingAvailabilityDomainModel.Unknown
            )
        )

        assertEquals(
            PaywallPresentationNotification.PlayUnreachable,
            notificationOf { classUnderTest.onRestoreAction(REPORT) }
        )
    }

    @Test
    fun `Given a purchase on another phone when restoring then Premium unlocks`() = runTest {
        getPaywallUseCase.givenSuccessfulExecution(FREE_PAYWALL)
        restorePurchasesUseCase.givenSuccessfulExecution(
            PremiumCheckDomainModel(YEARLY_STATUS, BillingAvailabilityDomainModel.Available)
        )

        assertEquals(UNLOCKED, notificationOf { classUnderTest.onRestoreAction(REPORT) })
    }

    @Test
    fun `Given an outcome the screen missed when it listens again then it gets it once`() =
        runTest {
            getPaywallUseCase.givenSuccessfulExecution(FREE_PAYWALL)
            buyPremiumUseCase.givenSuccessfulExecution(
                YEARLY,
                BuyPremiumResultDomainModel(PurchaseOutcomeDomainModel.Unlocked, YEARLY_STATUS)
            )
            classUnderTest.onBuyAction(REPORT, YEARLY_CARD)

            assertEquals(UNLOCKED, notificationOf { classUnderTest.onResume(REPORT) })

            classUnderTest.onOutcomeShownAction()
            val after = async(start = UNDISPATCHED) { classUnderTest.notification.first() }
            classUnderTest.onResume(REPORT)
            assertEquals(false, after.isCompleted)
            after.cancel()
        }

    @Test
    fun `Given lifetime over a subscription when managing it then the success shows again`() =
        runTest {
            val lifetime = PremiumStatusDomainModel.Lifetime(subscriptionRenewsToo = false)
            manageSubscriptionUseCase.givenSuccessfulExecution(lifetime)

            assertEquals(
                PaywallPresentationNotification.Unlocked(
                    PremiumStatusPresentationModel.Lifetime(subscriptionRenewsToo = false)
                ),
                notificationOf { classUnderTest.onManageAction() }
            )
        }

    @Test
    fun `When closing or continuing then the context goes with the event`() = runTest {
        val events = async(start = UNDISPATCHED) {
            classUnderTest.navigationEvent.take(2).toList()
        }

        classUnderTest.onCloseAction(PaywallContextPresentationModel.Onboarding)
        classUnderTest.onContinueAction(PaywallContextPresentationModel.Report)

        assertEquals(
            listOf(
                PaywallPresentationNavigationEvent.OnClose(
                    PaywallContextPresentationModel.Onboarding
                ),
                PaywallPresentationNavigationEvent.OnPremiumUnlocked(
                    PaywallContextPresentationModel.Report
                )
            ),
            events.await()
        )
    }

    @Test
    fun `Given a premium user reaches the report's paywall then its success shows`() = runTest {
        getPaywallUseCase.givenSuccessfulExecution(FREE_PAYWALL.copy(status = YEARLY_STATUS))

        assertEquals(UNLOCKED, notificationOf { classUnderTest.onEnter(REPORT) })
    }

    @Test
    fun `Given a subscriber opens the paywall from Settings then the plans show`() = runTest {
        getPaywallUseCase.givenSuccessfulExecution(FREE_PAYWALL.copy(status = YEARLY_STATUS))
        val notification = async(start = UNDISPATCHED) { classUnderTest.notification.first() }
        val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }

        classUnderTest.onEnter(PaywallContextPresentationModel.Settings)

        assertEquals(
            READY.copy(
                status = PremiumStatusPresentationModel.Subscribed(
                    PremiumPlanPresentationModel.Yearly,
                    null
                )
            ),
            state.await()
        )
        assertEquals(false, notification.isCompleted)
        notification.cancel()
    }

    @Test
    fun `Given a pending payment completes while the paywall shows then its success shows`() =
        runTest {
            getPaywallUseCase.givenSuccessfulExecution(FREE_PAYWALL.copy(status = YEARLY_STATUS))

            assertEquals(
                UNLOCKED,
                notificationOf {
                    classUnderTest.onPurchaseUpdatedAction(PaywallContextPresentationModel.Settings)
                }
            )
        }

    @Test
    fun `Given buying fails before it starts then the paywall leaves Waiting and says so`() =
        runTest {
            getPaywallUseCase.givenSuccessfulExecution(FREE_PAYWALL)
            buyPremiumUseCase.givenFailedExecution(
                YEARLY,
                com.migrely.app.architecture.domain.exception.UnknownDomainException(
                    IllegalStateException("no sheet")
                )
            )
            val states = async(start = UNDISPATCHED) { classUnderTest.viewState.take(2).toList() }

            val notification = notificationOf { classUnderTest.onBuyAction(REPORT, YEARLY_CARD) }

            assertEquals(PaywallPresentationNotification.SomethingWentWrong, notification)
            assertEquals(listOf(PaywallViewState.Working, READY), states.await())
        }
}
