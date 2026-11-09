package com.migrely.app.reminders.presentation.viewmodel

import com.migrely.app.architecture.presentation.viewmodel.BaseViewModelTest
import com.migrely.app.reminders.domain.model.VisitCardDomainModel
import com.migrely.app.reminders.domain.usecase.CloseVisitCardUseCase
import com.migrely.app.reminders.domain.usecase.GetVisitCardUseCase
import com.migrely.app.reminders.presentation.model.VisitCardPresentationNotification
import com.migrely.app.reminders.presentation.model.VisitCardViewState
import com.migrely.app.reminders.presentation.navigation.VisitCardPresentationNavigationEvent
import java.time.LocalDate
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify

private val NOVEMBER_20: LocalDate = LocalDate.of(2026, 11, 20)

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class VisitCardViewModelTest :
    BaseViewModelTest<VisitCardViewState, VisitCardPresentationNotification, VisitCardViewModel>() {
    @Mock private lateinit var getVisitCardUseCase: GetVisitCardUseCase

    @Mock private lateinit var closeVisitCardUseCase: CloseVisitCardUseCase

    @Before
    fun setUp() {
        classUnderTest =
            VisitCardViewModel(getVisitCardUseCase, closeVisitCardUseCase, useCaseExecutor)
    }

    @Test
    fun `Given a visit is due when home loads then the card shows its date`() = runTest {
        getVisitCardUseCase.givenSuccessfulExecution(VisitCardDomainModel.Shown(NOVEMBER_20))
        val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }

        classUnderTest.onEnter()

        assertEquals(VisitCardViewState.Shown(NOVEMBER_20), state.await())
    }

    @Test
    fun `When the card is closed then it goes at once and is closed for that visit`() = runTest {
        val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }

        classUnderTest.onCloseAction(NOVEMBER_20)

        assertEquals(VisitCardViewState.Hidden, state.await())
        verify(useCaseExecutor).execute(
            useCase = eq(closeVisitCardUseCase),
            value = eq(NOVEMBER_20),
            onResult = any(),
            onException = any()
        )
    }

    @Test
    fun `When Make a report is tapped then the card goes and the report opens through History`() =
        runTest {
            val event = async(start = UNDISPATCHED) { classUnderTest.navigationEvent.first() }

            classUnderTest.onReportAction(NOVEMBER_20)

            assertEquals(VisitCardPresentationNavigationEvent.OnOpenReport, event.await())
            verify(useCaseExecutor).execute(
                useCase = eq(closeVisitCardUseCase),
                value = eq(NOVEMBER_20),
                onResult = any(),
                onException = any()
            )
        }

    @Test
    fun `Given a read answering after the card was closed then the card stays gone`() = runTest {
        val states = mutableListOf<VisitCardViewState>()
        val collecting =
            launch(start = UNDISPATCHED) {
                classUnderTest.viewState.collect {
                    states +=
                        it
                }
            }
        val read = argumentCaptor<(VisitCardDomainModel) -> Unit>()

        classUnderTest.onEnter()
        verify(
            useCaseExecutor
        ).execute(
            useCase = eq(getVisitCardUseCase),
            onResult = read.capture(),
            onException = any()
        )
        classUnderTest.onCloseAction(NOVEMBER_20)
        read.firstValue(VisitCardDomainModel.Shown(NOVEMBER_20))
        runCurrent()

        assertEquals(listOf<VisitCardViewState>(VisitCardViewState.Hidden), states)
        collecting.cancel()
    }
}
