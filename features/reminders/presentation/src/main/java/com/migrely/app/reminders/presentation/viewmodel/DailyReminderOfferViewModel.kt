package com.migrely.app.reminders.presentation.viewmodel

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.architecture.presentation.viewmodel.BaseViewModel
import com.migrely.app.reminders.domain.model.RemindersResultDomainModel
import com.migrely.app.reminders.domain.model.SetDailyReminderRequestDomainModel
import com.migrely.app.reminders.domain.usecase.GetRemindersUseCase
import com.migrely.app.reminders.domain.usecase.RecordPermissionAskedUseCase
import com.migrely.app.reminders.domain.usecase.SetDailyReminderUseCase
import com.migrely.app.reminders.presentation.model.DailyReminderOfferPresentationNotification
import com.migrely.app.reminders.presentation.model.DailyReminderOfferViewState
import java.time.LocalTime

private val DEFAULT_TIME: LocalTime = LocalTime.of(20, 0)

/**
 * The daily reminder offered during onboarding (BRD REM-F1, D80), by the Reminders screen's rules: permission is asked
 * for only when the user turns it on, and not again within 30 days of a refusal (EC-REM-01, EC-REM-02). Every answer
 * ends in [DailyReminderOfferPresentationNotification.Done], so onboarding always moves on. The UI passes back the
 * time it shows.
 */
class DailyReminderOfferViewModel(
    private val getRemindersUseCase: GetRemindersUseCase,
    private val setDailyReminderUseCase: SetDailyReminderUseCase,
    private val recordPermissionAskedUseCase: RecordPermissionAskedUseCase,
    useCaseExecutor: UseCaseExecutor
) : BaseViewModel<DailyReminderOfferViewState, DailyReminderOfferPresentationNotification>(
    useCaseExecutor
) {
    fun onEnter() = getRemindersUseCase(
        onResult = { result ->
            val time =
                (result as? RemindersResultDomainModel.Loaded)?.reminders?.dailyTime ?: DEFAULT_TIME
            updateViewState(DailyReminderOfferViewState.Ready(time))
        },
        onException = { updateViewState(DailyReminderOfferViewState.Ready(DEFAULT_TIME)) }
    )

    fun onTurnOnAction(time: LocalTime) = getRemindersUseCase(
        onResult = { result ->
            val reminders = (result as? RemindersResultDomainModel.Loaded)?.reminders
            when {
                reminders == null -> done()
                reminders.notificationsAllowed -> turnOn(time)
                reminders.mayAskPermission ->
                    notify(DailyReminderOfferPresentationNotification.AskForPermission)
                // Refused within 30 days: the reminder stays off, and the Reminders screen says why.
                else -> done()
            }
        },
        onException = { done() }
    )

    /** The system's answer to the permission request. */
    fun onPermissionResultAction(granted: Boolean, time: LocalTime) = recordPermissionAskedUseCase(
        onResult = { if (granted) turnOn(time) else done() },
        onException = { if (granted) turnOn(time) else done() }
    )

    fun onNotNowAction() = done()

    private fun turnOn(time: LocalTime) = setDailyReminderUseCase(
        value = SetDailyReminderRequestDomainModel(true, time),
        onResult = { done() },
        onException = { done() }
    )

    private fun done() = notify(DailyReminderOfferPresentationNotification.Done)
}
