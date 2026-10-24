package com.migrely.app.history.presentation.viewmodel

import com.migrely.app.architecture.presentation.viewmodel.BaseViewModelTest
import com.migrely.app.history.domain.model.DeletedAttackDomainModel
import com.migrely.app.history.domain.model.HistoryMonthDomainModel
import com.migrely.app.history.domain.model.HistoryMonthResultDomainModel
import com.migrely.app.history.domain.model.UndoResultDomainModel
import com.migrely.app.history.domain.usecase.GetHistoryMonthUseCase
import com.migrely.app.history.domain.usecase.RestoreDeletedAttackUseCase
import com.migrely.app.history.presentation.mapper.HistoryAttackPresentationMapper
import com.migrely.app.history.presentation.mapper.HistoryMonthPresentationMapper
import com.migrely.app.history.presentation.mapper.IntensityLevelPresentationMapper
import com.migrely.app.history.presentation.model.HistoryPresentationNotification
import com.migrely.app.history.presentation.model.HistoryViewState
import com.migrely.app.history.presentation.navigation.HistoryPresentationNavigationEvent
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

private val SEPTEMBER: YearMonth = YearMonth.of(2026, 9)
private val TODAY: LocalDate = LocalDate.parse("2026-10-23")
private val DELETED_AT: Instant = Instant.parse("2026-10-23T07:00:00Z")

private fun month(anyAttackLogged: Boolean = true) = HistoryMonthDomainModel(
    month = SEPTEMBER,
    today = TODAY,
    earliestMonth = YearMonth.of(2026, 8),
    anyAttackLogged = anyAttackLogged,
    days = emptyList(),
    attacks = emptyList()
)

@RunWith(MockitoJUnitRunner::class)
class HistoryViewModelTest :
    BaseViewModelTest<HistoryViewState, HistoryPresentationNotification, HistoryViewModel>() {
    @Mock private lateinit var getHistoryMonthUseCase: GetHistoryMonthUseCase

    @Mock private lateinit var restoreDeletedAttackUseCase: RestoreDeletedAttackUseCase

    private val historyMonthPresentationMapper = HistoryMonthPresentationMapper(
        HistoryAttackPresentationMapper(),
        IntensityLevelPresentationMapper()
    )

    @Before
    fun setUp() {
        classUnderTest = HistoryViewModel(
            getHistoryMonthUseCase,
            restoreDeletedAttackUseCase,
            historyMonthPresentationMapper,
            useCaseExecutor
        )
    }

    private fun givenMonth(result: HistoryMonthResultDomainModel, request: YearMonth? = SEPTEMBER) {
        getHistoryMonthUseCase.givenSuccessfulExecution(request, result)
    }

    @Test
    fun `When entering then shows the month asked for`() = runTest {
        givenMonth(HistoryMonthResultDomainModel.Loaded(month()))
        val state = nextViewState()

        classUnderTest.onEnter(SEPTEMBER)

        assertEquals(
            HistoryViewState.Ready(historyMonthPresentationMapper.toPresentation(month())),
            state.await()
        )
    }

    @Test
    fun `Given nothing logged yet when entering then no attacks`() = runTest {
        givenMonth(HistoryMonthResultDomainModel.Loaded(month(anyAttackLogged = false)), null)
        val state = nextViewState()

        classUnderTest.onEnter(null)

        assertTrue(state.await() is HistoryViewState.NoAttacks)
    }

    @Test
    fun `Given a recent deletion when resuming then announces it`() = runTest {
        givenMonth(
            HistoryMonthResultDomainModel.Loaded(
                month(),
                DeletedAttackDomainModel("gone", DELETED_AT)
            )
        )
        val notification = nextNotification()

        classUnderTest.onResume(SEPTEMBER)

        assertEquals(
            HistoryPresentationNotification.AttackDeleted("gone", DELETED_AT),
            notification.await()
        )
    }

    @Test
    fun `Given the month cannot be read when changing month then something went wrong`() = runTest {
        givenMonth(HistoryMonthResultDomainModel.Failed)
        val notification = nextNotification()

        classUnderTest.onMonthAction(SEPTEMBER)

        assertEquals(
            HistoryPresentationNotification.SomethingWentWrong,
            notification.await()
        )
    }

    @Test
    fun `When choosing an attack then opens its details`() = runTest {
        val event = nextNavigationEvent()

        classUnderTest.onAttackAction("a")

        assertEquals(
            HistoryPresentationNavigationEvent.OnViewAttackDetails("a"),
            event.await()
        )
    }

    @Test
    fun `When adding for a day then opens the add screen for it`() = runTest {
        val day = LocalDate.parse("2026-09-12")
        val event = nextNavigationEvent()

        classUnderTest.onAddPastAttackAction(day)

        assertEquals(HistoryPresentationNavigationEvent.OnAddPastAttack(day), event.await())
    }

    @Test
    fun `Given the deletion is restored when undoing then undone and the month again`() = runTest {
        restoreDeletedAttackUseCase.givenSuccessfulExecution(
            "gone",
            UndoResultDomainModel.Undone
        )
        givenMonth(
            HistoryMonthResultDomainModel.Loaded(
                month(),
                DeletedAttackDomainModel("other", DELETED_AT)
            )
        )
        val notification = nextNotification()
        val state = nextViewState()

        classUnderTest.onUndoDeleteAction("gone", SEPTEMBER)

        assertEquals(HistoryPresentationNotification.Undone, notification.await())
        assertTrue(state.await() is HistoryViewState.Ready)
    }

    @Test
    fun `Given restoring would overlap when undoing then not possible`() = runTest {
        restoreDeletedAttackUseCase.givenSuccessfulExecution(
            "gone",
            UndoResultDomainModel.NotPossible
        )
        givenMonth(HistoryMonthResultDomainModel.Loaded(month()))
        val notification = nextNotification()

        classUnderTest.onUndoDeleteAction("gone", SEPTEMBER)

        assertEquals(HistoryPresentationNotification.UndoNotPossible, notification.await())
    }

    @Test
    fun `Given restoring fails when undoing then something went wrong`() = runTest {
        restoreDeletedAttackUseCase.givenSuccessfulExecution("gone", UndoResultDomainModel.Failed)
        val notification = nextNotification()

        classUnderTest.onUndoDeleteAction("gone", SEPTEMBER)

        assertEquals(HistoryPresentationNotification.SomethingWentWrong, notification.await())
    }

    @Test
    fun `When leaving then emits the leave event`() = runTest {
        val event = nextNavigationEvent()

        classUnderTest.onLeaveAction()

        assertEquals(HistoryPresentationNavigationEvent.OnLeave, event.await())
    }

    private fun TestScope.nextViewState() = async(start = UNDISPATCHED) {
        classUnderTest.viewState.first()
    }

    private fun TestScope.nextNotification() = async(start = UNDISPATCHED) {
        classUnderTest.notification.first()
    }

    private fun TestScope.nextNavigationEvent() = async(start = UNDISPATCHED) {
        classUnderTest.navigationEvent.first()
    }
}
