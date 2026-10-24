package com.migrely.app.home.presentation.viewmodel

import com.migrely.app.architecture.presentation.viewmodel.BaseViewModelTest
import com.migrely.app.home.domain.model.AnswerLongAttackResultDomainModel
import com.migrely.app.home.domain.model.AttackDomainModel
import com.migrely.app.home.domain.model.DeletedAttackDomainModel
import com.migrely.app.home.domain.model.EndAttackRequestDomainModel
import com.migrely.app.home.domain.model.EndAttackResultDomainModel
import com.migrely.app.home.domain.model.IntensityLevelDomainModel
import com.migrely.app.home.domain.model.LongAttackAnswerDomainModel
import com.migrely.app.home.domain.model.LongAttackAnswerRequestDomainModel
import com.migrely.app.home.domain.model.LongAttackCheckDomainModel
import com.migrely.app.home.domain.model.MoveStartResultDomainModel
import com.migrely.app.home.domain.model.RecordIntensityResultDomainModel
import com.migrely.app.home.domain.model.StartAttackResultDomainModel
import com.migrely.app.home.domain.model.TodayDomainModel
import com.migrely.app.home.domain.model.TodayResultDomainModel
import com.migrely.app.home.domain.model.UndoResultDomainModel
import com.migrely.app.home.domain.model.WriteResultDomainModel
import com.migrely.app.home.domain.usecase.AnswerLongAttackCheckUseCase
import com.migrely.app.home.domain.usecase.EndAttackUseCase
import com.migrely.app.home.domain.usecase.GetTodayUseCase
import com.migrely.app.home.domain.usecase.MoveAttackStartEarlierUseCase
import com.migrely.app.home.domain.usecase.RecordIntensityUseCase
import com.migrely.app.home.domain.usecase.RestoreDeletedAttackUseCase
import com.migrely.app.home.domain.usecase.SetPeriodTodayUseCase
import com.migrely.app.home.domain.usecase.StartAttackUseCase
import com.migrely.app.home.domain.usecase.UndoAttackEndUseCase
import com.migrely.app.home.domain.usecase.UndoAttackStartUseCase
import com.migrely.app.home.presentation.mapper.IntensityLevelDomainMapper
import com.migrely.app.home.presentation.mapper.IntensityLevelPresentationMapper
import com.migrely.app.home.presentation.mapper.TodayPresentationMapper
import com.migrely.app.home.presentation.model.HomePresentationNotification
import com.migrely.app.home.presentation.model.HomeViewState
import com.migrely.app.home.presentation.model.IntensityLevelPresentationModel
import com.migrely.app.home.presentation.model.SkyPresentationModel
import com.migrely.app.home.presentation.model.TodayPresentationModel
import com.migrely.app.home.presentation.navigation.HomePresentationNavigationEvent
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

private val START: Instant = Instant.parse("2026-10-23T09:00:00Z")
private val ATTACK = AttackDomainModel(
    id = "a",
    start = START,
    startZone = ZoneId.of("Asia/Tashkent"),
    end = null,
    intensityPoints = emptyList(),
    longAttackCheck = LongAttackCheckDomainModel(confirmedAt = null, disabled = false)
)
private val CALM = HomeViewState.Today(TodayPresentationModel(SkyPresentationModel.Calm, 0, null))

@RunWith(MockitoJUnitRunner::class)
class HomeViewModelTest :
    BaseViewModelTest<HomeViewState, HomePresentationNotification, HomeViewModel>() {
    @Mock private lateinit var getTodayUseCase: GetTodayUseCase

    @Mock private lateinit var startAttackUseCase: StartAttackUseCase

    @Mock private lateinit var recordIntensityUseCase: RecordIntensityUseCase

    @Mock private lateinit var moveAttackStartEarlierUseCase: MoveAttackStartEarlierUseCase

    @Mock private lateinit var endAttackUseCase: EndAttackUseCase

    @Mock private lateinit var undoAttackStartUseCase: UndoAttackStartUseCase

    @Mock private lateinit var undoAttackEndUseCase: UndoAttackEndUseCase

    @Mock private lateinit var answerLongAttackCheckUseCase: AnswerLongAttackCheckUseCase

    @Mock private lateinit var restoreDeletedAttackUseCase: RestoreDeletedAttackUseCase

    @Mock private lateinit var setPeriodTodayUseCase: SetPeriodTodayUseCase

    @Before
    fun setUp() {
        classUnderTest = HomeViewModel(
            getTodayUseCase,
            startAttackUseCase,
            recordIntensityUseCase,
            moveAttackStartEarlierUseCase,
            endAttackUseCase,
            undoAttackStartUseCase,
            undoAttackEndUseCase,
            answerLongAttackCheckUseCase,
            restoreDeletedAttackUseCase,
            setPeriodTodayUseCase,
            TodayPresentationMapper(IntensityLevelPresentationMapper()),
            IntensityLevelDomainMapper(),
            useCaseExecutor
        )
    }

    @Test
    fun `Given nothing logged when entering then presents a calm day`() = runTest {
        givenToday(TodayDomainModel.Calm)
        val state = nextViewState()

        classUnderTest.onEnter(startAttackRequested = false)

        assertEquals(CALM, state.await())
    }

    @Test
    fun `Given the shortcut start when entering then offers undo and shows attack mode`() =
        runTest {
            startAttackUseCase.givenSuccessfulExecution(
                StartAttackResultDomainModel.Started(ATTACK)
            )
            givenToday(TodayDomainModel.AttackInProgress(ATTACK, longAttackCheckDue = false))
            val notification = nextNotification()
            val state = nextViewState()

            classUnderTest.onEnter(startAttackRequested = true)

            assertEquals(HomePresentationNotification.AttackStarted("a"), notification.await())
            assertEquals(HomeViewState.AttackInProgress::class, state.await()::class)
        }

    @Test
    fun `Given an attack already in progress when starting then shows it without undo`() = runTest {
        startAttackUseCase.givenSuccessfulExecution(
            StartAttackResultDomainModel.AlreadyInProgress(ATTACK)
        )
        givenToday(TodayDomainModel.AttackInProgress(ATTACK, longAttackCheckDue = false))
        val state = nextViewState()

        classUnderTest.onStartAttackAction()

        assertEquals(HomeViewState.AttackInProgress::class, state.await()::class)
    }

    @Test
    fun `Given storage is full when starting then tells the user`() = runTest {
        startAttackUseCase.givenSuccessfulExecution(StartAttackResultDomainModel.StorageFull)
        val notification = nextNotification()

        classUnderTest.onStartAttackAction()

        assertEquals(HomePresentationNotification.StorageFull, notification.await())
    }

    @Test
    fun `When selecting Severe then records level 9 and reloads today`() = runTest {
        recordIntensityUseCase.givenSuccessfulExecution(
            IntensityLevelDomainModel.Severe,
            RecordIntensityResultDomainModel.Recorded
        )
        givenToday(TodayDomainModel.AttackInProgress(ATTACK, longAttackCheckDue = false))
        val state = nextViewState()

        classUnderTest.onIntensityAction(IntensityLevelPresentationModel.Severe)

        assertEquals(HomeViewState.AttackInProgress::class, state.await()::class)
    }

    @Test
    fun `Given the previous attack ended later when moving the start then explains why`() =
        runTest {
            val previousEnd = START.minusSeconds(1_800)
            moveAttackStartEarlierUseCase.givenSuccessfulExecution(
                Duration.ofMinutes(60),
                MoveStartResultDomainModel.OverlapsPrevious(previousEnd)
            )
            val notification = nextNotification()

            classUnderTest.onMoveStartEarlierAction(60)

            assertEquals(
                HomePresentationNotification.StartMoveRejected(previousEnd),
                notification.await()
            )
        }

    @Test
    fun `When ending the attack then offers undo`() = runTest {
        endAttackUseCase.givenSuccessfulExecution(
            EndAttackRequestDomainModel(null),
            EndAttackResultDomainModel.Ended("a")
        )
        givenToday(TodayDomainModel.Calm)
        val notification = nextNotification()

        classUnderTest.onEndAttackAction()

        assertEquals(HomePresentationNotification.AttackEnded("a"), notification.await())
    }

    @Test
    fun `Given a picked end time out of range when ending then tells the user`() = runTest {
        val picked = START.minusSeconds(60)
        endAttackUseCase.givenSuccessfulExecution(
            EndAttackRequestDomainModel(picked),
            EndAttackResultDomainModel.EndOutOfRange
        )
        val notification = nextNotification()

        classUnderTest.onEndedAtAction(picked)

        assertEquals(HomePresentationNotification.EndTimeOutOfRange, notification.await())
    }

    @Test
    fun `When undoing a start then reloads today`() = runTest {
        undoAttackStartUseCase.givenSuccessfulExecution("a", UndoResultDomainModel.Undone)
        givenToday(TodayDomainModel.Calm)
        val state = nextViewState()

        classUnderTest.onUndoStartAction("a")

        assertEquals(CALM, state.await())
    }

    @Test
    fun `When undoing a start then confirms it`() = runTest {
        undoAttackStartUseCase.givenSuccessfulExecution("a", UndoResultDomainModel.Undone)
        givenToday(TodayDomainModel.Calm)
        val notification = nextNotification()

        classUnderTest.onUndoStartAction("a")

        assertEquals(HomePresentationNotification.Undone, notification.await())
    }

    @Test
    fun `Given the end can no longer be undone when undoing then tells the user and reloads`() =
        runTest {
            undoAttackEndUseCase.givenSuccessfulExecution("a", UndoResultDomainModel.NotPossible)
            givenToday(TodayDomainModel.Calm)
            val notification = nextNotification()
            val state = nextViewState()

            classUnderTest.onUndoEndAction("a")

            assertEquals(HomePresentationNotification.UndoNotPossible, notification.await())
            assertEquals(CALM, state.await())
        }

    @Test
    fun `Given undoing an end fails then tells the user`() = runTest {
        undoAttackEndUseCase.givenSuccessfulExecution("a", UndoResultDomainModel.Failed)
        val notification = nextNotification()

        classUnderTest.onUndoEndAction("a")

        assertEquals(HomePresentationNotification.SomethingWentWrong, notification.await())
    }

    @Test
    fun `When answering still going then saves and reloads today`() = runTest {
        answerLongAttackCheckUseCase.givenSuccessfulExecution(
            LongAttackAnswerRequestDomainModel("a", LongAttackAnswerDomainModel.StillGoing),
            AnswerLongAttackResultDomainModel.Saved
        )
        givenToday(TodayDomainModel.AttackInProgress(ATTACK, longAttackCheckDue = false))
        val state = nextViewState()

        classUnderTest.onStillGoingAction("a")

        assertEquals(HomeViewState.AttackInProgress::class, state.await()::class)
    }

    @Test
    fun `When answering don't ask again then saves and reloads today`() = runTest {
        answerLongAttackCheckUseCase.givenSuccessfulExecution(
            LongAttackAnswerRequestDomainModel("a", LongAttackAnswerDomainModel.DontAskAgain),
            AnswerLongAttackResultDomainModel.Saved
        )
        givenToday(TodayDomainModel.AttackInProgress(ATTACK, longAttackCheckDue = false))
        val state = nextViewState()

        classUnderTest.onDontAskAgainAction("a")

        assertEquals(HomeViewState.AttackInProgress::class, state.await()::class)
    }

    @Test
    fun `When resuming then reloads today`() = runTest {
        givenToday(TodayDomainModel.Calm)
        val state = nextViewState()

        classUnderTest.onResume()

        assertEquals(CALM, state.await())
    }

    @Test
    fun `When the date changes then reloads today`() = runTest {
        givenToday(TodayDomainModel.Calm)
        val state = nextViewState()

        classUnderTest.onDateChanged()

        assertEquals(CALM, state.await())
    }

    @Test
    fun `Given today cannot be loaded when resuming then tells the user`() = runTest {
        getTodayUseCase.givenSuccessfulExecution(TodayResultDomainModel.Failed)
        val notification = nextNotification()

        classUnderTest.onResume()

        assertEquals(HomePresentationNotification.SomethingWentWrong, notification.await())
    }

    @Test
    fun `Given a recent deletion when returning to home then offers to undo it`() = runTest {
        val deletedAt = START.plusSeconds(60)
        getTodayUseCase.givenSuccessfulExecution(
            TodayResultDomainModel.Loaded(
                TodayDomainModel.Calm,
                DeletedAttackDomainModel("gone", deletedAt)
            )
        )
        val notification = nextNotification()

        classUnderTest.onResume()

        assertEquals(
            HomePresentationNotification.AttackDeleted("gone", deletedAt),
            notification.await()
        )
    }

    @Test
    fun `When undoing a deletion then restores and confirms`() = runTest {
        restoreDeletedAttackUseCase.givenSuccessfulExecution("gone", UndoResultDomainModel.Undone)
        givenToday(TodayDomainModel.Calm)
        val notification = nextNotification()

        classUnderTest.onUndoDeleteAction("gone")

        assertEquals(HomePresentationNotification.Undone, notification.await())
    }

    @Test
    fun `When opening an attack's details then asks to navigate there`() = runTest {
        val event = async(start = UNDISPATCHED) { classUnderTest.navigationEvent.first() }

        classUnderTest.onAttackDetailsAction("a")

        assertEquals(HomePresentationNavigationEvent.OnViewAttackDetails("a"), event.await())
    }

    @Test
    fun `When adding a past attack then asks to navigate there`() = runTest {
        val event = async(start = UNDISPATCHED) { classUnderTest.navigationEvent.first() }

        classUnderTest.onAddPastAttackAction()

        assertEquals(HomePresentationNavigationEvent.OnAddPastAttack, event.await())
    }

    private fun givenToday(today: TodayDomainModel) {
        getTodayUseCase.givenSuccessfulExecution(TodayResultDomainModel.Loaded(today))
    }

    private fun TestScope.nextViewState() = async(start = UNDISPATCHED) {
        classUnderTest.viewState.first()
    }

    private fun TestScope.nextNotification() = async(start = UNDISPATCHED) {
        classUnderTest.notification.first()
    }

    @Test
    fun `When logging a medication from attack mode then opens it for the attack`() = runTest {
        val event = async(start = UNDISPATCHED) { classUnderTest.navigationEvent.first() }

        classUnderTest.onLogMedicationAction("a")

        assertEquals(HomePresentationNavigationEvent.OnLogMedication("a"), event.await())
    }

    @Test
    fun `When logging a medication from the normal mode then opens it on its own`() = runTest {
        val event = async(start = UNDISPATCHED) { classUnderTest.navigationEvent.first() }

        classUnderTest.onLogMedicationAction(null)

        assertEquals(HomePresentationNavigationEvent.OnLogMedication(null), event.await())
    }

    @Test
    fun `When marking today's period then writes it and loads today again`() = runTest {
        setPeriodTodayUseCase.givenSuccessfulExecution(true, WriteResultDomainModel.Written)
        getTodayUseCase.givenSuccessfulExecution(
            TodayResultDomainModel.Loaded(TodayDomainModel.Calm, periodToday = true)
        )
        val state = nextViewState()

        classUnderTest.onPeriodTodayAction(marked = true)

        assertEquals(
            HomeViewState.Today(
                TodayPresentationModel(
                    sky = SkyPresentationModel.Calm,
                    attackCount = 0,
                    peak = null,
                    periodToday = true
                )
            ),
            state.await()
        )
    }

    @Test
    fun `Given storage is full when marking today's period then says so`() = runTest {
        setPeriodTodayUseCase.givenSuccessfulExecution(false, WriteResultDomainModel.StorageFull)
        val notification = async(start = UNDISPATCHED) { classUnderTest.notification.first() }

        classUnderTest.onPeriodTodayAction(marked = false)

        assertEquals(HomePresentationNotification.StorageFull, notification.await())
    }

    @Test
    fun `Given the write fails when marking today's period then something went wrong`() = runTest {
        setPeriodTodayUseCase.givenSuccessfulExecution(true, WriteResultDomainModel.Failed)
        val notification = async(start = UNDISPATCHED) { classUnderTest.notification.first() }

        classUnderTest.onPeriodTodayAction(marked = true)

        assertEquals(HomePresentationNotification.SomethingWentWrong, notification.await())
    }

    @Test
    fun `When choosing reminders then opens them`() = runTest {
        val event = async(start = UNDISPATCHED) { classUnderTest.navigationEvent.first() }

        classUnderTest.onRemindersAction()

        assertEquals(HomePresentationNavigationEvent.OnOpenReminders, event.await())
    }
}
