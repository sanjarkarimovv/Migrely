package com.migrely.app.onboarding.presentation.viewmodel

import com.migrely.app.architecture.presentation.viewmodel.BaseViewModelTest
import com.migrely.app.onboarding.domain.model.AttackFrequencyDomainModel
import com.migrely.app.onboarding.domain.model.OnboardingGoalDomainModel
import com.migrely.app.onboarding.domain.model.OnboardingWriteResultDomainModel
import com.migrely.app.onboarding.domain.usecase.FinishOnboardingUseCase
import com.migrely.app.onboarding.domain.usecase.SaveAttackFrequencyUseCase
import com.migrely.app.onboarding.domain.usecase.SaveGoalUseCase
import com.migrely.app.onboarding.domain.usecase.SavePeriodTrackingUseCase
import com.migrely.app.onboarding.presentation.mapper.AttackFrequencyDomainMapper
import com.migrely.app.onboarding.presentation.mapper.OnboardingGoalDomainMapper
import com.migrely.app.onboarding.presentation.model.AttackFrequencyPresentationModel
import com.migrely.app.onboarding.presentation.model.OnboardingGoalPresentationModel
import com.migrely.app.onboarding.presentation.model.OnboardingPresentationNotification
import com.migrely.app.onboarding.presentation.model.OnboardingViewState
import com.migrely.app.onboarding.presentation.navigation.OnboardingPresentationNavigationEvent
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify

@RunWith(MockitoJUnitRunner::class)
class OnboardingViewModelTest :
    BaseViewModelTest<
        OnboardingViewState,
        OnboardingPresentationNotification,
        OnboardingViewModel
        >() {
    @Mock private lateinit var saveGoalUseCase: SaveGoalUseCase

    @Mock private lateinit var saveAttackFrequencyUseCase: SaveAttackFrequencyUseCase

    @Mock private lateinit var savePeriodTrackingUseCase: SavePeriodTrackingUseCase

    @Mock private lateinit var finishOnboardingUseCase: FinishOnboardingUseCase

    @Before
    fun setUp() {
        classUnderTest = OnboardingViewModel(
            saveGoalUseCase,
            saveAttackFrequencyUseCase,
            savePeriodTrackingUseCase,
            finishOnboardingUseCase,
            OnboardingGoalDomainMapper(),
            AttackFrequencyDomainMapper(),
            useCaseExecutor
        )
    }

    @Test
    fun `When entering then ready`() = runTest {
        val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }

        classUnderTest.onEnter()

        assertEquals(OnboardingViewState.Ready, state.await())
    }

    @Test
    fun `When answering then each answer is saved`() {
        classUnderTest.onGoalAction(OnboardingGoalPresentationModel.FewerPainkillers)
        classUnderTest.onFrequencyAction(AttackFrequencyPresentationModel.FourToFourteen)
        classUnderTest.onPeriodTrackingAction(false)

        verify(useCaseExecutor).execute(
            useCase = eq(saveGoalUseCase),
            value = eq(OnboardingGoalDomainModel.FewerPainkillers),
            onResult = any(),
            onException = any()
        )
        verify(useCaseExecutor).execute(
            useCase = eq(saveAttackFrequencyUseCase),
            value = eq(AttackFrequencyDomainModel.FourToFourteen),
            onResult = any(),
            onException = any()
        )
        verify(useCaseExecutor).execute(
            useCase = eq(savePeriodTrackingUseCase),
            value = eq(false),
            onResult = any(),
            onException = any()
        )
    }

    @Test
    fun `Given an answer is not saved then the user is told`() = runTest {
        saveGoalUseCase.givenSuccessfulExecution(
            OnboardingGoalDomainModel.FindTriggers,
            OnboardingWriteResultDomainModel.Failed
        )
        val notification = async(start = UNDISPATCHED) { classUnderTest.notification.first() }

        classUnderTest.onGoalAction(OnboardingGoalPresentationModel.FindTriggers)

        assertEquals(OnboardingPresentationNotification.NotSaved, notification.await())
    }

    @Test
    fun `When finishing then home, even if it could not be saved`() = runTest {
        finishOnboardingUseCase.givenSuccessfulExecution(
            Unit,
            OnboardingWriteResultDomainModel.Failed
        )
        val first = async(start = UNDISPATCHED) { classUnderTest.navigationEvent.first() }
        classUnderTest.onFinishAction()
        assertEquals(OnboardingPresentationNavigationEvent.OnFinished, first.await())
    }

    @Test
    fun `When restoring then Your data opens`() = runTest {
        val event = async(start = UNDISPATCHED) { classUnderTest.navigationEvent.first() }

        classUnderTest.onRestoreAction()

        assertEquals(OnboardingPresentationNavigationEvent.OnRestoreBackup, event.await())
    }
}
