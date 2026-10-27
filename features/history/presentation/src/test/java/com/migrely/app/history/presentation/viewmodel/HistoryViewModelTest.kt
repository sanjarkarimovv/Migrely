package com.migrely.app.history.presentation.viewmodel

import com.migrely.app.architecture.presentation.viewmodel.BaseViewModelTest
import com.migrely.app.history.domain.model.DayMarkKindDomainModel
import com.migrely.app.history.domain.model.DeletedAttackDomainModel
import com.migrely.app.history.domain.model.HistoryMonthDomainModel
import com.migrely.app.history.domain.model.HistoryMonthResultDomainModel
import com.migrely.app.history.domain.model.MonthStatsDomainModel
import com.migrely.app.history.domain.model.SetDayMarkRequestDomainModel
import com.migrely.app.history.domain.model.SetDayMarkResultDomainModel
import com.migrely.app.history.domain.model.UndoResultDomainModel
import com.migrely.app.history.domain.usecase.GetHistoryMonthUseCase
import com.migrely.app.history.domain.usecase.HasPremiumUseCase
import com.migrely.app.history.domain.usecase.RestoreDeletedAttackUseCase
import com.migrely.app.history.domain.usecase.SetDayMarkUseCase
import com.migrely.app.history.presentation.mapper.HistoryAttackPresentationMapper
import com.migrely.app.history.presentation.mapper.HistoryMonthPresentationMapper
import com.migrely.app.history.presentation.mapper.IntensityLevelPresentationMapper
import com.migrely.app.history.presentation.mapper.MonthStatsPresentationMapper
import com.migrely.app.history.presentation.mapper.PainLevelPresentationMapper
import com.migrely.app.history.presentation.model.HistoryPresentationNotification
import com.migrely.app.history.presentation.model.HistoryViewState
import com.migrely.app.history.presentation.model.PremiumFeaturePresentationModel
import com.migrely.app.history.presentation.navigation.HistoryPresentationNavigationEvent
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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

private fun month(
    anythingLogged: Boolean = true,
    earliestMonth: YearMonth = YearMonth.of(2026, 8),
    attackMedicationDays: Int = 0
) = HistoryMonthDomainModel(
    month = SEPTEMBER,
    today = TODAY,
    earliestMonth = earliestMonth,
    anythingLogged = anythingLogged,
    days = emptyList(),
    attacks = emptyList(),
    stats = MonthStatsDomainModel(
        0,
        0,
        0,
        null,
        attackMedicationDays,
        chronic = false,
        progress = null
    )
)

@RunWith(MockitoJUnitRunner::class)
class HistoryViewModelTest :
    BaseViewModelTest<HistoryViewState, HistoryPresentationNotification, HistoryViewModel>() {
    @Mock private lateinit var getHistoryMonthUseCase: GetHistoryMonthUseCase

    @Mock private lateinit var restoreDeletedAttackUseCase: RestoreDeletedAttackUseCase

    @Mock private lateinit var setDayMarkUseCase: SetDayMarkUseCase

    @Mock private lateinit var hasPremiumUseCase: HasPremiumUseCase

    private val historyMonthPresentationMapper = HistoryMonthPresentationMapper(
        HistoryAttackPresentationMapper(),
        IntensityLevelPresentationMapper(PainLevelPresentationMapper()),
        MonthStatsPresentationMapper(PainLevelPresentationMapper())
    )

    @Before
    fun setUp() {
        classUnderTest = HistoryViewModel(
            getHistoryMonthUseCase,
            restoreDeletedAttackUseCase,
            setDayMarkUseCase,
            hasPremiumUseCase,
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
            HistoryViewState.Ready(
                historyMonthPresentationMapper.toPresentation(month()),
                requestedMonth = SEPTEMBER
            ),
            state.await()
        )
    }

    @Test
    fun `Given nothing logged yet when entering then nothing logged`() = runTest {
        givenMonth(HistoryMonthResultDomainModel.Loaded(month(anythingLogged = false)), null)
        val state = nextViewState()

        classUnderTest.onEnter(null)

        assertEquals(
            HistoryViewState.NothingLogged(
                historyMonthPresentationMapper.toPresentation(month(anythingLogged = false)),
                requestedMonth = null
            ),
            state.await()
        )
    }

    @Test
    fun `Given medication but no migraine logged when entering then ready with the month before`() =
        runTest {
            // Medication alone shows the month and its numbers, and its earlier months (BRD D99).
            val logged = month(attackMedicationDays = 16)
            givenMonth(HistoryMonthResultDomainModel.Loaded(logged))
            val state = nextViewState()

            classUnderTest.onEnter(SEPTEMBER)

            val actual = state.await()
            assertEquals(
                HistoryViewState.Ready(
                    historyMonthPresentationMapper.toPresentation(logged),
                    requestedMonth = SEPTEMBER
                ),
                actual
            )
            assertTrue((actual as HistoryViewState.Ready).month.hasPreviousMonth)
        }

    @Test
    fun `Given nothing logged and this month the earliest when entering then no month before`() =
        runTest {
            // What the domain returns for only preventive intakes too, as it leaves them out.
            val empty = month(anythingLogged = false, earliestMonth = SEPTEMBER)
            givenMonth(HistoryMonthResultDomainModel.Loaded(empty))
            val state = nextViewState()

            classUnderTest.onEnter(SEPTEMBER)

            val actual = state.await()
            assertTrue(actual is HistoryViewState.NothingLogged)
            assertEquals(false, (actual as HistoryViewState.NothingLogged).month.hasPreviousMonth)
        }

    @Test
    fun `Given a recent deletion when changing month then it is not announced again`() = runTest {
        givenMonth(
            HistoryMonthResultDomainModel.Loaded(
                month(),
                DeletedAttackDomainModel("gone", DELETED_AT)
            )
        )
        val notifications = mutableListOf<HistoryPresentationNotification>()
        val collector = launch(start = UNDISPATCHED) {
            classUnderTest.notification.collect { notifications += it }
        }
        val state = nextViewState()

        classUnderTest.onMonthAction(SEPTEMBER)
        state.await()
        collector.cancel()

        assertEquals(emptyList<HistoryPresentationNotification>(), notifications)
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

    @Test
    fun `Given Premium when asking for the doctor report then emits its event`() = runTest {
        hasPremiumUseCase.givenSuccessfulExecution(true)
        val event = nextNavigationEvent()

        classUnderTest.onReportAction()

        assertEquals(HistoryPresentationNavigationEvent.OnOpenReport, event.await())
    }

    @Test
    fun `Given Premium when asking for insights then emits their event`() = runTest {
        hasPremiumUseCase.givenSuccessfulExecution(true)
        val event = nextNavigationEvent()

        classUnderTest.onInsightsAction()

        assertEquals(HistoryPresentationNavigationEvent.OnOpenInsights, event.await())
    }

    @Test
    fun `Given a free user when asking for the doctor report then its paywall opens`() = runTest {
        hasPremiumUseCase.givenSuccessfulExecution(false)
        val event = nextNavigationEvent()

        classUnderTest.onReportAction()

        assertEquals(
            HistoryPresentationNavigationEvent.OnOpenPaywall(
                PremiumFeaturePresentationModel.Report
            ),
            event.await()
        )
    }

    @Test
    fun `Given a free user when asking for insights then their paywall opens`() = runTest {
        hasPremiumUseCase.givenSuccessfulExecution(false)
        val event = nextNavigationEvent()

        classUnderTest.onInsightsAction()

        assertEquals(
            HistoryPresentationNavigationEvent.OnOpenPaywall(
                PremiumFeaturePresentationModel.Insights
            ),
            event.await()
        )
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

    @Test
    fun `When marking a period day then writes it and loads the month again`() = runTest {
        val day = LocalDate.parse("2026-09-14")
        setDayMarkUseCase.givenSuccessfulExecution(
            SetDayMarkRequestDomainModel(day, DayMarkKindDomainModel.Period, true),
            SetDayMarkResultDomainModel.Done
        )
        givenMonth(HistoryMonthResultDomainModel.Loaded(month()))
        val state = nextViewState()

        classUnderTest.onPeriodAction(SEPTEMBER, day, marked = true)

        assertEquals(
            HistoryViewState.Ready(
                historyMonthPresentationMapper.toPresentation(month()),
                requestedMonth = SEPTEMBER
            ),
            state.await()
        )
    }

    @Test
    fun `Given an attack on the day when marking pain-free then says so`() = runTest {
        val day = LocalDate.parse("2026-09-14")
        setDayMarkUseCase.givenSuccessfulExecution(
            SetDayMarkRequestDomainModel(day, DayMarkKindDomainModel.PainFree, true),
            SetDayMarkResultDomainModel.AttackLogged
        )
        givenMonth(HistoryMonthResultDomainModel.Loaded(month()))
        val notification = async(start = UNDISPATCHED) { classUnderTest.notification.first() }

        classUnderTest.onPainFreeAction(SEPTEMBER, day, marked = true)

        assertEquals(HistoryPresentationNotification.AttackLoggedOnDay, notification.await())
    }

    @Test
    fun `Given storage is full when marking then says so`() = runTest {
        val day = LocalDate.parse("2026-09-14")
        setDayMarkUseCase.givenSuccessfulExecution(
            SetDayMarkRequestDomainModel(day, DayMarkKindDomainModel.PainFree, false),
            SetDayMarkResultDomainModel.StorageFull
        )
        val notification = async(start = UNDISPATCHED) { classUnderTest.notification.first() }

        classUnderTest.onPainFreeAction(SEPTEMBER, day, marked = false)

        assertEquals(HistoryPresentationNotification.StorageFull, notification.await())
    }

    @Test
    fun `Given marking fails or the day is in the future when marking then something went wrong`() =
        runTest {
            val day = LocalDate.parse("2026-09-14")
            listOf(SetDayMarkResultDomainModel.Failed, SetDayMarkResultDomainModel.FutureDay)
                .forEach { result ->
                    setDayMarkUseCase.givenSuccessfulExecution(
                        SetDayMarkRequestDomainModel(day, DayMarkKindDomainModel.Period, true),
                        result
                    )
                    val notification =
                        async(start = UNDISPATCHED) { classUnderTest.notification.first() }

                    classUnderTest.onPeriodAction(SEPTEMBER, day, marked = true)

                    assertEquals(
                        HistoryPresentationNotification.SomethingWentWrong,
                        notification.await()
                    )
                }
        }
}
