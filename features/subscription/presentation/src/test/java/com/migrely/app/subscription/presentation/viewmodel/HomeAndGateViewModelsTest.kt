package com.migrely.app.subscription.presentation.viewmodel

import com.migrely.app.architecture.presentation.viewmodel.BaseViewModelTest
import com.migrely.app.subscription.domain.model.PaywallTriggerDomainModel
import com.migrely.app.subscription.domain.model.TrialReminderDomainModel
import com.migrely.app.subscription.domain.usecase.DismissTrialReminderUseCase
import com.migrely.app.subscription.domain.usecase.GetTrialReminderUseCase
import com.migrely.app.subscription.domain.usecase.ManageSubscriptionUseCase
import com.migrely.app.subscription.domain.usecase.ShouldShowPaywallUseCase
import com.migrely.app.subscription.presentation.model.PaywallTriggerPresentationNotification
import com.migrely.app.subscription.presentation.model.TrialBannerPresentationNotification
import com.migrely.app.subscription.presentation.model.TrialBannerViewState
import com.migrely.app.subscription.presentation.navigation.PaywallGatePresentationNavigationEvent
import com.migrely.app.subscription.presentation.navigation.PaywallTriggerPresentationNavigationEvent
import java.time.Instant
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

private val TRIAL_END: Instant = Instant.parse("2026-10-26T09:00:00Z")

@RunWith(MockitoJUnitRunner::class)
class TrialBannerViewModelTest :
    BaseViewModelTest<
        TrialBannerViewState,
        TrialBannerPresentationNotification,
        TrialBannerViewModel
        >() {
    @Mock private lateinit var getTrialReminderUseCase: GetTrialReminderUseCase

    @Mock private lateinit var dismissTrialReminderUseCase: DismissTrialReminderUseCase

    @Mock private lateinit var manageSubscriptionUseCase: ManageSubscriptionUseCase

    private fun create() {
        classUnderTest = TrialBannerViewModel(
            getTrialReminderUseCase,
            dismissTrialReminderUseCase,
            manageSubscriptionUseCase,
            useCaseExecutor
        )
    }

    @Test
    fun `Given the trial ends tomorrow when entering then the banner shows`() = runTest {
        create()
        getTrialReminderUseCase.givenSuccessfulExecution(
            TrialReminderDomainModel.EndsSoon(TRIAL_END)
        )
        val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }

        classUnderTest.onEnter()

        assertEquals(TrialBannerViewState.Shown(TRIAL_END), state.await())
    }

    @Test
    fun `When dismissing then it hides at once and the dismissal is kept`() = runTest {
        create()
        val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }

        classUnderTest.onDismissAction(TRIAL_END)

        assertEquals(TrialBannerViewState.Hidden, state.await())
        verify(
            useCaseExecutor
        ).execute(eq(dismissTrialReminderUseCase), eq(TRIAL_END), any(), any())
    }
}

@RunWith(MockitoJUnitRunner::class)
class PaywallTriggerViewModelTest :
    BaseViewModelTest<Unit, PaywallTriggerPresentationNotification, PaywallTriggerViewModel>() {
    @Mock private lateinit var shouldShowPaywallUseCase: ShouldShowPaywallUseCase

    @Test
    fun `Given the 5th migraine allows it when home loads then the paywall opens`() = runTest {
        classUnderTest = PaywallTriggerViewModel(shouldShowPaywallUseCase, useCaseExecutor)
        shouldShowPaywallUseCase.givenSuccessfulExecution(
            PaywallTriggerDomainModel.FifthMigraine,
            true
        )
        val event = async(start = UNDISPATCHED) { classUnderTest.navigationEvent.first() }

        classUnderTest.onEnter()

        assertEquals(PaywallTriggerPresentationNavigationEvent.OnOpenPaywall, event.await())
    }

    @Test
    fun `Given the rules do not allow it when home loads then nothing opens`() = runTest {
        classUnderTest = PaywallTriggerViewModel(shouldShowPaywallUseCase, useCaseExecutor)
        shouldShowPaywallUseCase.givenSuccessfulExecution(
            PaywallTriggerDomainModel.FifthMigraine,
            false
        )
        val event = async(start = UNDISPATCHED) { classUnderTest.navigationEvent.first() }

        classUnderTest.onEnter()

        assertFalse(event.isCompleted)
        event.cancel()
    }

    @Test
    fun `Given a question is open when home loads again then it is not asked twice`() = runTest {
        classUnderTest = PaywallTriggerViewModel(shouldShowPaywallUseCase, useCaseExecutor)

        classUnderTest.onEnter()
        classUnderTest.onEnter()

        verify(useCaseExecutor, times(1)).execute(eq(shouldShowPaywallUseCase), any(), any(), any())
    }
}

@RunWith(MockitoJUnitRunner::class)
class PaywallGateViewModelTest :
    BaseViewModelTest<Unit, PaywallTriggerPresentationNotification, PaywallGateViewModel>() {
    @Mock private lateinit var shouldShowPaywallUseCase: ShouldShowPaywallUseCase

    @Test
    fun `Given a free user after onboarding then the paywall shows itself`() = runTest {
        classUnderTest = PaywallGateViewModel(shouldShowPaywallUseCase, useCaseExecutor)
        shouldShowPaywallUseCase.givenSuccessfulExecution(
            PaywallTriggerDomainModel.Onboarding,
            true
        )
        val event = async(start = UNDISPATCHED) { classUnderTest.navigationEvent.first() }

        classUnderTest.onEnter()

        assertEquals(PaywallGatePresentationNavigationEvent.OnShowPaywall, event.await())
    }

    @Test
    fun `Given no paywall is due after onboarding then home follows`() = runTest {
        classUnderTest = PaywallGateViewModel(shouldShowPaywallUseCase, useCaseExecutor)
        shouldShowPaywallUseCase.givenSuccessfulExecution(
            PaywallTriggerDomainModel.Onboarding,
            false
        )
        val event = async(start = UNDISPATCHED) { classUnderTest.navigationEvent.first() }

        classUnderTest.onEnter()

        assertEquals(PaywallGatePresentationNavigationEvent.OnContinue, event.await())
    }
}
