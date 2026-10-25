package com.migrely.app.subscription.presentation.viewmodel

import com.migrely.app.architecture.presentation.viewmodel.BaseViewModelTest
import com.migrely.app.subscription.domain.model.BillingAvailabilityDomainModel
import com.migrely.app.subscription.domain.model.PremiumCheckDomainModel
import com.migrely.app.subscription.domain.model.PremiumPlanDomainModel
import com.migrely.app.subscription.domain.model.PremiumStatusDomainModel
import com.migrely.app.subscription.domain.model.RefreshPremiumRequestDomainModel
import com.migrely.app.subscription.domain.usecase.ManageSubscriptionUseCase
import com.migrely.app.subscription.domain.usecase.RefreshPremiumUseCase
import com.migrely.app.subscription.domain.usecase.RestorePurchasesUseCase
import com.migrely.app.subscription.presentation.mapper.PremiumPlanPresentationMapper
import com.migrely.app.subscription.presentation.mapper.PremiumStatusPresentationMapper
import com.migrely.app.subscription.presentation.model.PremiumPlanPresentationModel
import com.migrely.app.subscription.presentation.model.PremiumSectionPresentationNotification
import com.migrely.app.subscription.presentation.model.PremiumSectionViewState
import com.migrely.app.subscription.presentation.model.PremiumStatusPresentationModel
import com.migrely.app.subscription.presentation.model.RestoreNotePresentationModel
import com.migrely.app.subscription.presentation.navigation.PremiumSectionPresentationNavigationEvent
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.async
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

@RunWith(MockitoJUnitRunner::class)
class PremiumSectionViewModelTest :
    BaseViewModelTest<
        PremiumSectionViewState,
        PremiumSectionPresentationNotification,
        PremiumSectionViewModel
        >() {
    @Mock private lateinit var refreshPremiumUseCase: RefreshPremiumUseCase

    @Mock private lateinit var restorePurchasesUseCase: RestorePurchasesUseCase

    @Mock private lateinit var manageSubscriptionUseCase: ManageSubscriptionUseCase

    @Before
    fun setUp() {
        classUnderTest = PremiumSectionViewModel(
            refreshPremiumUseCase,
            restorePurchasesUseCase,
            manageSubscriptionUseCase,
            PremiumStatusPresentationMapper(PremiumPlanPresentationMapper()),
            useCaseExecutor
        )
    }

    @Test
    fun `When entering then the status shows, checked with Play when it is due`() = runTest {
        refreshPremiumUseCase.givenSuccessfulExecution(
            RefreshPremiumRequestDomainModel(force = false),
            PremiumStatusDomainModel.Subscribed(PremiumPlanDomainModel.Yearly, null)
        )
        val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }

        classUnderTest.onEnter()

        assertEquals(
            PremiumSectionViewState.Ready(
                PremiumStatusPresentationModel.Subscribed(
                    PremiumPlanPresentationModel.Yearly,
                    null
                ),
                note = null
            ),
            state.await()
        )
    }

    @Test
    fun `When restoring then Play is asked and what it found shows`() = runTest {
        restorePurchasesUseCase.givenSuccessfulExecution(
            PremiumCheckDomainModel(
                PremiumStatusDomainModel.Free,
                BillingAvailabilityDomainModel.Available
            )
        )
        val states = async(start = UNDISPATCHED) { classUnderTest.viewState.take(2).toList() }

        classUnderTest.onRestoreAction()

        assertEquals(
            listOf(
                PremiumSectionViewState.Checking,
                PremiumSectionViewState.Ready(
                    PremiumStatusPresentationModel.Free,
                    RestoreNotePresentationModel.NothingFound
                )
            ),
            states.await()
        )
    }

    @Test
    fun `Given Play does not answer when restoring then the note says so`() = runTest {
        restorePurchasesUseCase.givenSuccessfulExecution(
            PremiumCheckDomainModel(
                PremiumStatusDomainModel.Lifetime(false),
                BillingAvailabilityDomainModel.Unknown
            )
        )
        val states = async(start = UNDISPATCHED) { classUnderTest.viewState.take(2).toList() }

        classUnderTest.onRestoreAction()

        assertEquals(
            PremiumSectionViewState.Ready(
                PremiumStatusPresentationModel.Lifetime(false),
                RestoreNotePresentationModel.Unreachable
            ),
            states.await().last()
        )
    }

    @Test
    fun `When managing the subscription then the status is read again`() = runTest {
        manageSubscriptionUseCase.givenSuccessfulExecution(PremiumStatusDomainModel.Free)
        val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }

        classUnderTest.onManageAction()

        assertEquals(
            PremiumSectionViewState.Ready(PremiumStatusPresentationModel.Free, null),
            state.await()
        )
    }

    @Test
    fun `When tapping the status then the paywall opens`() = runTest {
        val event = async(start = UNDISPATCHED) { classUnderTest.navigationEvent.first() }

        classUnderTest.onStatusAction()

        assertEquals(PremiumSectionPresentationNavigationEvent.OnOpenPaywall, event.await())
    }

    @Test
    fun `Given a purchase change while Settings shows then the status is read again`() = runTest {
        refreshPremiumUseCase.givenSuccessfulExecution(
            RefreshPremiumRequestDomainModel(force = false),
            PremiumStatusDomainModel.Lifetime(subscriptionRenewsToo = false)
        )
        val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }

        classUnderTest.onPurchaseUpdatedAction()

        assertEquals(
            PremiumSectionViewState.Ready(PremiumStatusPresentationModel.Lifetime(false), null),
            state.await()
        )
    }
}
