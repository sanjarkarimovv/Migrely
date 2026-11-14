package com.migrely.app.reminders.presentation.viewmodel

import com.migrely.app.architecture.presentation.viewmodel.BaseViewModelTest
import com.migrely.app.reminders.domain.model.DoctorVisitDomainModel
import com.migrely.app.reminders.domain.model.ReminderWriteResultDomainModel
import com.migrely.app.reminders.domain.model.RemindersDomainModel
import com.migrely.app.reminders.domain.model.RemindersResultDomainModel
import com.migrely.app.reminders.domain.model.RemoveDoctorVisitResultDomainModel
import com.migrely.app.reminders.domain.model.SetDailyReminderRequestDomainModel
import com.migrely.app.reminders.domain.model.SetDoctorVisitResultDomainModel
import com.migrely.app.reminders.domain.usecase.GetRemindersUseCase
import com.migrely.app.reminders.domain.usecase.RecordPermissionAskedUseCase
import com.migrely.app.reminders.domain.usecase.RemoveDoctorVisitUseCase
import com.migrely.app.reminders.domain.usecase.RestoreDoctorVisitUseCase
import com.migrely.app.reminders.domain.usecase.SetDailyReminderUseCase
import com.migrely.app.reminders.domain.usecase.SetDoctorVisitUseCase
import com.migrely.app.reminders.domain.usecase.SetEffectReminderUseCase
import com.migrely.app.reminders.presentation.mapper.DoctorVisitPresentationMapper
import com.migrely.app.reminders.presentation.mapper.RemindersPresentationMapper
import com.migrely.app.reminders.presentation.model.DoctorVisitPresentationModel
import com.migrely.app.reminders.presentation.model.ReminderKindPresentationModel
import com.migrely.app.reminders.presentation.model.RemindersPresentationModel
import com.migrely.app.reminders.presentation.model.RemindersPresentationNotification
import com.migrely.app.reminders.presentation.model.RemindersViewState
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
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
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

private val EIGHT_PM = LocalTime.of(20, 0)
private val NOVEMBER_20: LocalDate = LocalDate.of(2026, 11, 20)
private val NOVEMBER_27: LocalDate = LocalDate.of(2026, 11, 27)
private val TODAY: LocalDate = LocalDate.of(2026, 11, 10)

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class RemindersViewModelTest :
    BaseViewModelTest<RemindersViewState, RemindersPresentationNotification, RemindersViewModel>() {
    @Mock private lateinit var getRemindersUseCase: GetRemindersUseCase

    @Mock private lateinit var setDailyReminderUseCase: SetDailyReminderUseCase

    @Mock private lateinit var setEffectReminderUseCase: SetEffectReminderUseCase

    @Mock private lateinit var recordPermissionAskedUseCase: RecordPermissionAskedUseCase

    @Mock private lateinit var setDoctorVisitUseCase: SetDoctorVisitUseCase

    @Mock private lateinit var removeDoctorVisitUseCase: RemoveDoctorVisitUseCase

    @Mock private lateinit var restoreDoctorVisitUseCase: RestoreDoctorVisitUseCase

    @Before
    fun setUp() {
        classUnderTest = RemindersViewModel(
            getRemindersUseCase,
            setDailyReminderUseCase,
            setEffectReminderUseCase,
            recordPermissionAskedUseCase,
            setDoctorVisitUseCase,
            removeDoctorVisitUseCase,
            restoreDoctorVisitUseCase,
            RemindersPresentationMapper(),
            DoctorVisitPresentationMapper(),
            useCaseExecutor
        )
    }

    private fun givenReminders(allowed: Boolean, mayAsk: Boolean = true) {
        getRemindersUseCase.givenSuccessfulExecution(
            RemindersResultDomainModel.Loaded(
                RemindersDomainModel(false, EIGHT_PM, true, allowed, mayAsk, TODAY)
            )
        )
    }

    private fun verifyDailySet(on: Boolean) {
        verify(useCaseExecutor).execute(
            useCase = eq(setDailyReminderUseCase),
            value = eq(SetDailyReminderRequestDomainModel(on, EIGHT_PM)),
            onResult = any(),
            onException = any()
        )
    }

    private fun verifyDailyNotSet() {
        verify(useCaseExecutor, never()).execute(
            useCase = eq(setDailyReminderUseCase),
            value = any(),
            onResult = any(),
            onException = any()
        )
    }

    @Test
    fun `When entering then the reminders`() = runTest {
        givenReminders(allowed = true)
        val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }

        classUnderTest.onEnter()

        assertEquals(
            RemindersViewState.Ready(
                RemindersPresentationModel(false, EIGHT_PM, true, true, true, TODAY)
            ),
            state.await()
        )
    }

    @Test
    fun `Given notifications are allowed when turning it on then it is turned on`() {
        givenReminders(allowed = true)

        classUnderTest.onDailyReminderAction(on = true, time = EIGHT_PM)

        verifyDailySet(on = true)
    }

    @Test
    fun `Given notifications are off when turning it on then permission is asked for`() = runTest {
        givenReminders(allowed = false)
        val notification = async(start = UNDISPATCHED) { classUnderTest.notification.first() }

        classUnderTest.onDailyReminderAction(on = true, time = EIGHT_PM)

        assertEquals(
            RemindersPresentationNotification.AskForPermission(
                ReminderKindPresentationModel.Daily
            ),
            notification.await()
        )
        verifyDailyNotSet()
    }

    @Test
    fun `Given permission was asked within 30 days when turning it on then it stays off`() {
        givenReminders(allowed = false, mayAsk = false)

        classUnderTest.onDailyReminderAction(on = true, time = EIGHT_PM)

        verifyDailyNotSet()
    }

    @Test
    fun `Given permission is granted for the daily reminder then it is noted and turned on`() {
        recordPermissionAskedUseCase.givenSuccessfulExecution(ReminderWriteResultDomainModel.Done)

        classUnderTest.onPermissionResultAction(true, ReminderKindPresentationModel.Daily, EIGHT_PM)

        verifyDailySet(on = true)
    }

    @Test
    fun `Given permission is refused then it is noted and nothing is turned on`() {
        recordPermissionAskedUseCase.givenSuccessfulExecution(ReminderWriteResultDomainModel.Done)
        givenReminders(allowed = false, mayAsk = false)

        classUnderTest.onPermissionResultAction(
            false,
            ReminderKindPresentationModel.Daily,
            EIGHT_PM
        )

        verifyDailyNotSet()
    }

    @Test
    fun `Given storage is full when turning it off then storage full`() = runTest {
        setDailyReminderUseCase.givenSuccessfulExecution(
            SetDailyReminderRequestDomainModel(false, EIGHT_PM),
            ReminderWriteResultDomainModel.StorageFull
        )
        givenReminders(allowed = true)
        val notification = async(start = UNDISPATCHED) { classUnderTest.notification.first() }

        classUnderTest.onDailyReminderAction(on = false, time = EIGHT_PM)

        assertEquals(RemindersPresentationNotification.StorageFull, notification.await())
    }

    @Test
    fun `When moving the time then it is saved as it is, on or off`() {
        classUnderTest.onDailyTimeAction(on = true, time = EIGHT_PM)

        verifyDailySet(on = true)
    }

    @Test
    fun `Given notifications off when a visit is saved then permission is asked for it`() =
        runTest {
            givenReminders(allowed = false)
            setDoctorVisitUseCase.givenSuccessfulExecution(
                NOVEMBER_20,
                SetDoctorVisitResultDomainModel.Saved(askForPermission = true)
            )
            // After the write saved (ux F4), which has its own test below.
            val heard = notificationsOf { classUnderTest.onVisitDateAction(NOVEMBER_20) }

            assertEquals(
                RemindersPresentationNotification.AskForPermission(
                    ReminderKindPresentationModel.Visit
                ),
                heard.last()
            )
        }

    @Test
    fun `Given the same date when picked then nothing is said and the screen reads again`() =
        runTest {
            givenReminders(allowed = true)
            setDoctorVisitUseCase.givenSuccessfulExecution(
                NOVEMBER_20,
                SetDoctorVisitResultDomainModel.Unchanged
            )
            val notifications = mutableListOf<RemindersPresentationNotification>()
            val collecting = launch(start = UNDISPATCHED) {
                classUnderTest.notification.collect { notifications += it }
            }

            classUnderTest.onVisitDateAction(NOVEMBER_20)
            runCurrent()

            assertEquals(emptyList<RemindersPresentationNotification>(), notifications)
            verify(
                useCaseExecutor
            ).execute(useCase = eq(getRemindersUseCase), onResult = any(), onException = any())
            collecting.cancel()
        }

    @Test
    fun `Given storage is full when a visit is saved then it says so`() = runTest {
        givenReminders(allowed = true)
        setDoctorVisitUseCase.givenSuccessfulExecution(
            NOVEMBER_20,
            SetDoctorVisitResultDomainModel.StorageFull
        )
        val notification = async(start = UNDISPATCHED) { classUnderTest.notification.first() }

        classUnderTest.onVisitDateAction(NOVEMBER_20)

        assertEquals(RemindersPresentationNotification.StorageFull, notification.await())
    }

    @Test
    fun `When the visit is removed then Visit removed with it, and Undo puts it back`() = runTest {
        givenReminders(allowed = true)
        removeDoctorVisitUseCase.givenSuccessfulExecution(
            RemoveDoctorVisitResultDomainModel.Removed(DoctorVisitDomainModel("v", NOVEMBER_20))
        )
        restoreDoctorVisitUseCase.givenSuccessfulExecution(
            DoctorVisitDomainModel("v", NOVEMBER_20),
            ReminderWriteResultDomainModel.Done
        )
        val removed = async(start = UNDISPATCHED) { classUnderTest.notification.first() }

        classUnderTest.onRemoveVisitAction()

        assertEquals(
            RemindersPresentationNotification.VisitRemoved(
                DoctorVisitPresentationModel("v", NOVEMBER_20)
            ),
            removed.await()
        )
        val undone = async(start = UNDISPATCHED) { classUnderTest.notification.first() }

        classUnderTest.onUndoRemoveVisitAction(DoctorVisitPresentationModel("v", NOVEMBER_20))

        assertEquals(RemindersPresentationNotification.Undone, undone.await())
    }

    @Test
    fun `Given the visit asked when permission is granted then no reminder is turned on`() {
        givenReminders(allowed = true)
        recordPermissionAskedUseCase.givenSuccessfulExecution(ReminderWriteResultDomainModel.Done)

        classUnderTest.onPermissionResultAction(true, ReminderKindPresentationModel.Visit, EIGHT_PM)

        verifyDailyNotSet()
        verify(useCaseExecutor, never()).execute(
            useCase = eq(setEffectReminderUseCase),
            value = any(),
            onResult = any(),
            onException = any()
        )
    }

    @Test
    fun `Given the first read answers after a newer one when shown then the newer one stays`() =
        runTest {
            val reads = argumentCaptor<(RemindersResultDomainModel) -> Unit>()
            val states = mutableListOf<RemindersViewState>()
            val collecting = launch(start = UNDISPATCHED) {
                classUnderTest.viewState.collect {
                    states +=
                        it
                }
            }

            classUnderTest.onEnter()
            classUnderTest.onResume()
            verify(useCaseExecutor, times(2)).execute(
                useCase = eq(getRemindersUseCase),
                onResult = reads.capture(),
                onException = any()
            )
            reads.secondValue(loaded(nextVisit = NOVEMBER_27))
            reads.firstValue(loaded(nextVisit = null))
            runCurrent()

            assertEquals(
                listOf(NOVEMBER_27),
                states.map {
                    (it as RemindersViewState.Ready).reminders.nextVisit
                }
            )
            collecting.cancel()
        }

    /** Issue #187: the screen shows past midnight, so the clock's change reads again and the visit day has no Remove. */
    @Test
    fun `Given the screen shows when the clock changes then it reads the new day`() = runTest {
        val reads = argumentCaptor<(RemindersResultDomainModel) -> Unit>()
        val states = mutableListOf<RemindersViewState>()
        val collecting = launch(start = UNDISPATCHED) {
            classUnderTest.viewState.collect { states += it }
        }

        classUnderTest.onEnter()
        verify(useCaseExecutor).execute(
            useCase = eq(getRemindersUseCase),
            onResult = reads.capture(),
            onException = any()
        )
        reads.lastValue(loaded(nextVisit = NOVEMBER_20, today = NOVEMBER_20.minusDays(1)))
        runCurrent()
        classUnderTest.onClockChanged()
        verify(useCaseExecutor, times(2)).execute(
            useCase = eq(getRemindersUseCase),
            onResult = reads.capture(),
            onException = any()
        )
        reads.lastValue(loaded(nextVisit = NOVEMBER_20, today = NOVEMBER_20))
        runCurrent()

        assertEquals(
            listOf(
                NOVEMBER_20.minusDays(1) to true,
                NOVEMBER_20 to false
            ),
            states.map {
                val reminders = (it as RemindersViewState.Ready).reminders
                reminders.today to reminders.visitRemovable
            }
        )
        collecting.cancel()
    }

    private fun loaded(nextVisit: LocalDate?, today: LocalDate = TODAY) =
        RemindersResultDomainModel.Loaded(
            RemindersDomainModel(
                false,
                EIGHT_PM,
                true,
                true,
                true,
                today = today,
                nextVisit = nextVisit,
                visitRemovable = nextVisit?.isAfter(today) == true
            )
        )

    // "Write saved" (BRD D178 (1), EC-UX-35, ux F4): a reminder or visit write with no message of its own says that it
    // saved, so the screen can replace an error with "Saved.". A result that wrote nothing says nothing.

    /** Every notification [action] sends. */
    private fun TestScope.notificationsOf(action: () -> Unit): List<RemindersPresentationNotification> {
        val heard = mutableListOf<RemindersPresentationNotification>()
        val collecting = launch(start = UNDISPATCHED) {
            classUnderTest.notification.collect { heard += it }
        }
        action()
        runCurrent()
        collecting.cancel()
        return heard
    }

    private val saved = listOf(RemindersPresentationNotification.Saved)

    @Test
    fun `Given notifications are allowed when the daily reminder is turned on then write saved`() =
        runTest {
            givenReminders(allowed = true)
            setDailyReminderUseCase.givenSuccessfulExecution(
                SetDailyReminderRequestDomainModel(true, EIGHT_PM),
                ReminderWriteResultDomainModel.Done
            )

            assertEquals(
                saved,
                notificationsOf { classUnderTest.onDailyReminderAction(true, EIGHT_PM) }
            )
        }

    @Test
    fun `When the daily reminder is turned off then write saved`() = runTest {
        givenReminders(allowed = true)
        setDailyReminderUseCase.givenSuccessfulExecution(
            SetDailyReminderRequestDomainModel(false, EIGHT_PM),
            ReminderWriteResultDomainModel.Done
        )

        assertEquals(saved, notificationsOf { classUnderTest.onDailyReminderAction(false, EIGHT_PM) })
    }

    @Test
    fun `When the daily time is saved then write saved`() = runTest {
        givenReminders(allowed = true)
        setDailyReminderUseCase.givenSuccessfulExecution(
            SetDailyReminderRequestDomainModel(true, EIGHT_PM),
            ReminderWriteResultDomainModel.Done
        )

        assertEquals(saved, notificationsOf { classUnderTest.onDailyTimeAction(true, EIGHT_PM) })
    }

    @Test
    fun `Given notifications are allowed when the effect reminder is turned on then write saved`() =
        runTest {
            givenReminders(allowed = true)
            setEffectReminderUseCase.givenSuccessfulExecution(
                true,
                ReminderWriteResultDomainModel.Done
            )

            assertEquals(saved, notificationsOf { classUnderTest.onEffectReminderAction(true) })
        }

    @Test
    fun `When the effect reminder is turned off then write saved`() = runTest {
        givenReminders(allowed = true)
        setEffectReminderUseCase.givenSuccessfulExecution(false, ReminderWriteResultDomainModel.Done)

        assertEquals(saved, notificationsOf { classUnderTest.onEffectReminderAction(false) })
    }

    @Test
    fun `Given a reminder write fails then no write saved`() = runTest {
        givenReminders(allowed = true)
        setEffectReminderUseCase.givenSuccessfulExecution(
            false,
            ReminderWriteResultDomainModel.StorageFull
        )

        assertEquals(
            listOf(RemindersPresentationNotification.StorageFull),
            notificationsOf { classUnderTest.onEffectReminderAction(false) }
        )
    }

    @Test
    fun `When a visit date is saved then write saved`() = runTest {
        givenReminders(allowed = true)
        setDoctorVisitUseCase.givenSuccessfulExecution(
            NOVEMBER_20,
            SetDoctorVisitResultDomainModel.Saved(askForPermission = false)
        )

        assertEquals(saved, notificationsOf { classUnderTest.onVisitDateAction(NOVEMBER_20) })
    }

    @Test
    fun `Given a visit date saved that asks for permission then write saved, then the request`() =
        runTest {
            givenReminders(allowed = false)
            setDoctorVisitUseCase.givenSuccessfulExecution(
                NOVEMBER_20,
                SetDoctorVisitResultDomainModel.Saved(askForPermission = true)
            )

            assertEquals(
                listOf(
                    RemindersPresentationNotification.Saved,
                    RemindersPresentationNotification.AskForPermission(
                        ReminderKindPresentationModel.Visit
                    )
                ),
                notificationsOf { classUnderTest.onVisitDateAction(NOVEMBER_20) }
            )
        }

    @Test
    fun `Given the same visit date then no write saved`() = runTest {
        givenReminders(allowed = true)
        setDoctorVisitUseCase.givenSuccessfulExecution(
            NOVEMBER_20,
            SetDoctorVisitResultDomainModel.Unchanged
        )

        assertEquals(
            emptyList<RemindersPresentationNotification>(),
            notificationsOf { classUnderTest.onVisitDateAction(NOVEMBER_20) }
        )
    }

    @Test
    fun `Given no visit to remove then no write saved`() = runTest {
        givenReminders(allowed = true)
        removeDoctorVisitUseCase.givenSuccessfulExecution(
            RemoveDoctorVisitResultDomainModel.NothingToRemove
        )

        assertEquals(
            emptyList<RemindersPresentationNotification>(),
            notificationsOf { classUnderTest.onRemoveVisitAction() }
        )
    }

    @Test
    fun `Given a visit removed or put back then only its own message`() = runTest {
        givenReminders(allowed = true)
        removeDoctorVisitUseCase.givenSuccessfulExecution(
            RemoveDoctorVisitResultDomainModel.Removed(DoctorVisitDomainModel("v", NOVEMBER_20))
        )
        restoreDoctorVisitUseCase.givenSuccessfulExecution(
            DoctorVisitDomainModel("v", NOVEMBER_20),
            ReminderWriteResultDomainModel.Done
        )

        assertEquals(
            listOf(
                RemindersPresentationNotification.VisitRemoved(
                    DoctorVisitPresentationModel("v", NOVEMBER_20)
                ),
                RemindersPresentationNotification.Undone
            ),
            notificationsOf {
                classUnderTest.onRemoveVisitAction()
                classUnderTest.onUndoRemoveVisitAction(
                    DoctorVisitPresentationModel("v", NOVEMBER_20)
                )
            }
        )
    }
}
