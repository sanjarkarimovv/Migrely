package com.migrely.app.reminders.presentation.viewmodel

import com.migrely.app.architecture.presentation.viewmodel.BaseViewModelTest
import com.migrely.app.reminders.domain.model.ReminderWriteResultDomainModel
import com.migrely.app.reminders.domain.model.RemindersDomainModel
import com.migrely.app.reminders.domain.model.RemindersResultDomainModel
import com.migrely.app.reminders.domain.model.SetDailyReminderRequestDomainModel
import com.migrely.app.reminders.domain.usecase.GetRemindersUseCase
import com.migrely.app.reminders.domain.usecase.RecordPermissionAskedUseCase
import com.migrely.app.reminders.domain.usecase.SetDailyReminderUseCase
import com.migrely.app.reminders.presentation.model.DailyReminderOfferPresentationNotification
import com.migrely.app.reminders.presentation.model.DailyReminderOfferViewState
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
private val ON_AT_EIGHT = SetDailyReminderRequestDomainModel(true, EIGHT_PM)

@RunWith(MockitoJUnitRunner::class)
class DailyReminderOfferViewModelTest :
    BaseViewModelTest<
        DailyReminderOfferViewState,
        DailyReminderOfferPresentationNotification,
        DailyReminderOfferViewModel
        >() {
    @Mock private lateinit var getRemindersUseCase: GetRemindersUseCase

    @Mock private lateinit var setDailyReminderUseCase: SetDailyReminderUseCase

    @Mock private lateinit var recordPermissionAskedUseCase: RecordPermissionAskedUseCase

    @Before
    fun setUp() {
        classUnderTest = DailyReminderOfferViewModel(
            getRemindersUseCase,
            setDailyReminderUseCase,
            recordPermissionAskedUseCase,
            useCaseExecutor
        )
    }

    private fun givenReminders(
        allowed: Boolean,
        mayAsk: Boolean = true,
        time: LocalTime = EIGHT_PM
    ) {
        getRemindersUseCase.givenSuccessfulExecution(
            RemindersResultDomainModel.Loaded(
                RemindersDomainModel(false, time, true, allowed, mayAsk)
            )
        )
    }

    private fun verifyNotTurnedOn() = verify(useCaseExecutor, never()).execute(
        useCase = eq(setDailyReminderUseCase),
        value = any(),
        onResult = any(),
        onException = any()
    )

    @Test
    fun `When entering then the offer at the saved time, or 8 PM`() = runTest {
        givenReminders(allowed = false, time = LocalTime.of(21, 30))
        val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }
        classUnderTest.onEnter()
        assertEquals(DailyReminderOfferViewState.Ready(LocalTime.of(21, 30)), state.await())

        getRemindersUseCase.givenSuccessfulExecution(RemindersResultDomainModel.Failed)
        val fallback = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }
        classUnderTest.onEnter()
        assertEquals(DailyReminderOfferViewState.Ready(EIGHT_PM), fallback.await())
    }

    @Test
    fun `Given notifications are allowed when turning it on then it is on, and done`() = runTest {
        givenReminders(allowed = true)
        setDailyReminderUseCase.givenSuccessfulExecution(
            ON_AT_EIGHT,
            ReminderWriteResultDomainModel.Done
        )
        val notification = async(start = UNDISPATCHED) { classUnderTest.notification.first() }

        classUnderTest.onTurnOnAction(EIGHT_PM)

        assertEquals(DailyReminderOfferPresentationNotification.Done, notification.await())
    }

    @Test
    fun `Given notifications are off when turning it on then permission is asked for`() = runTest {
        givenReminders(allowed = false)
        val notification = async(start = UNDISPATCHED) { classUnderTest.notification.first() }

        classUnderTest.onTurnOnAction(EIGHT_PM)

        assertEquals(
            DailyReminderOfferPresentationNotification.AskForPermission,
            notification.await()
        )
        verifyNotTurnedOn()
    }

    @Test
    fun `Given permission was refused within 30 days when turning it on then done, with it off`() =
        runTest {
            givenReminders(allowed = false, mayAsk = false)
            val notification = async(start = UNDISPATCHED) { classUnderTest.notification.first() }

            classUnderTest.onTurnOnAction(EIGHT_PM)

            assertEquals(DailyReminderOfferPresentationNotification.Done, notification.await())
            verifyNotTurnedOn()
        }

    @Test
    fun `When permission is answered then on if allowed, and done either way`() = runTest {
        recordPermissionAskedUseCase.givenSuccessfulExecution(ReminderWriteResultDomainModel.Done)
        setDailyReminderUseCase.givenSuccessfulExecution(
            ON_AT_EIGHT,
            ReminderWriteResultDomainModel.Done
        )

        val refused = async(start = UNDISPATCHED) { classUnderTest.notification.first() }
        classUnderTest.onPermissionResultAction(granted = false, time = EIGHT_PM)
        assertEquals(DailyReminderOfferPresentationNotification.Done, refused.await())
        verifyNotTurnedOn()

        val allowed = async(start = UNDISPATCHED) { classUnderTest.notification.first() }
        classUnderTest.onPermissionResultAction(granted = true, time = EIGHT_PM)
        assertEquals(DailyReminderOfferPresentationNotification.Done, allowed.await())
        verify(useCaseExecutor).execute(
            useCase = eq(setDailyReminderUseCase),
            value = eq(ON_AT_EIGHT),
            onResult = any(),
            onException = any()
        )
    }

    @Test
    fun `When not now then done, and nothing changes`() = runTest {
        val notification = async(start = UNDISPATCHED) { classUnderTest.notification.first() }

        classUnderTest.onNotNowAction()

        assertEquals(DailyReminderOfferPresentationNotification.Done, notification.await())
        verifyNotTurnedOn()
    }
}
