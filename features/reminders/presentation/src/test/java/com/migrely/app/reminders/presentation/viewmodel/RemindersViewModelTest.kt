package com.migrely.app.reminders.presentation.viewmodel

import com.migrely.app.architecture.presentation.viewmodel.BaseViewModelTest
import com.migrely.app.reminders.domain.model.ReminderWriteResultDomainModel
import com.migrely.app.reminders.domain.model.RemindersDomainModel
import com.migrely.app.reminders.domain.model.RemindersResultDomainModel
import com.migrely.app.reminders.domain.model.SetDailyReminderRequestDomainModel
import com.migrely.app.reminders.domain.usecase.GetRemindersUseCase
import com.migrely.app.reminders.domain.usecase.RecordPermissionAskedUseCase
import com.migrely.app.reminders.domain.usecase.SetDailyReminderUseCase
import com.migrely.app.reminders.domain.usecase.SetEffectReminderUseCase
import com.migrely.app.reminders.presentation.mapper.RemindersPresentationMapper
import com.migrely.app.reminders.presentation.model.ReminderKindPresentationModel
import com.migrely.app.reminders.presentation.model.RemindersPresentationModel
import com.migrely.app.reminders.presentation.model.RemindersPresentationNotification
import com.migrely.app.reminders.presentation.model.RemindersViewState
import java.time.LocalTime
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
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

private val EIGHT_PM = LocalTime.of(20, 0)

@RunWith(MockitoJUnitRunner::class)
class RemindersViewModelTest :
    BaseViewModelTest<RemindersViewState, RemindersPresentationNotification, RemindersViewModel>() {
    @Mock private lateinit var getRemindersUseCase: GetRemindersUseCase

    @Mock private lateinit var setDailyReminderUseCase: SetDailyReminderUseCase

    @Mock private lateinit var setEffectReminderUseCase: SetEffectReminderUseCase

    @Mock private lateinit var recordPermissionAskedUseCase: RecordPermissionAskedUseCase

    @Before
    fun setUp() {
        classUnderTest = RemindersViewModel(
            getRemindersUseCase,
            setDailyReminderUseCase,
            setEffectReminderUseCase,
            recordPermissionAskedUseCase,
            RemindersPresentationMapper(),
            useCaseExecutor
        )
    }

    private fun givenReminders(allowed: Boolean, mayAsk: Boolean = true) {
        getRemindersUseCase.givenSuccessfulExecution(
            RemindersResultDomainModel.Loaded(
                RemindersDomainModel(false, EIGHT_PM, true, allowed, mayAsk)
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
            RemindersViewState.Ready(RemindersPresentationModel(false, EIGHT_PM, true, true, true)),
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
}
