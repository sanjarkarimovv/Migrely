package com.migrely.app.reminders.presentation.viewmodel

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.architecture.presentation.viewmodel.BaseViewModel
import com.migrely.app.reminders.domain.model.ReminderWriteResultDomainModel
import com.migrely.app.reminders.domain.model.RemindersResultDomainModel
import com.migrely.app.reminders.domain.model.SetDailyReminderRequestDomainModel
import com.migrely.app.reminders.domain.usecase.GetRemindersUseCase
import com.migrely.app.reminders.domain.usecase.RecordPermissionAskedUseCase
import com.migrely.app.reminders.domain.usecase.SetDailyReminderUseCase
import com.migrely.app.reminders.domain.usecase.SetEffectReminderUseCase
import com.migrely.app.reminders.presentation.mapper.RemindersPresentationMapper
import com.migrely.app.reminders.presentation.model.ReminderKindPresentationModel
import com.migrely.app.reminders.presentation.model.RemindersPresentationNotification
import com.migrely.app.reminders.presentation.model.RemindersViewState
import com.migrely.app.reminders.presentation.navigation.RemindersPresentationNavigationEvent
import java.time.LocalTime

/**
 * The Reminders screen (BRD D65–D68). Turning a reminder on needs notifications: when they are off, permission is asked
 * for if the 30-day rule allows it (EC-REM-01, EC-REM-02); otherwise the screen shows why it stays off. The UI passes
 * back the time shown and which switch asked, so the view model keeps no state.
 */
class RemindersViewModel(
    private val getRemindersUseCase: GetRemindersUseCase,
    private val setDailyReminderUseCase: SetDailyReminderUseCase,
    private val setEffectReminderUseCase: SetEffectReminderUseCase,
    private val recordPermissionAskedUseCase: RecordPermissionAskedUseCase,
    private val remindersPresentationMapper: RemindersPresentationMapper,
    useCaseExecutor: UseCaseExecutor
) : BaseViewModel<RemindersViewState, RemindersPresentationNotification>(useCaseExecutor) {
    fun onEnter() = load()

    fun onResume() = load()

    fun onBackAction() = emitNavigationEvent(RemindersPresentationNavigationEvent.OnBack)

    fun onDailyReminderAction(on: Boolean, time: LocalTime) {
        if (on) {
            whenAllowed(ReminderKindPresentationModel.Daily) {
                setDaily(true, time)
            }
        } else {
            setDaily(false, time)
        }
    }

    fun onDailyTimeAction(on: Boolean, time: LocalTime) = setDaily(on, time)

    fun onEffectReminderAction(on: Boolean) {
        if (on) {
            whenAllowed(ReminderKindPresentationModel.Effect) {
                setEffect(true)
            }
        } else {
            setEffect(false)
        }
    }

    /** The system's answer to a request for [kind]: when allowed, that reminder is turned on. */
    fun onPermissionResultAction(
        granted: Boolean,
        kind: ReminderKindPresentationModel,
        time: LocalTime
    ) {
        recordPermissionAskedUseCase(
            onResult = {
                when {
                    !granted -> load()
                    kind == ReminderKindPresentationModel.Daily -> setDaily(true, time)
                    else -> setEffect(true)
                }
            }
        )
    }

    private fun whenAllowed(kind: ReminderKindPresentationModel, turnOn: () -> Unit) {
        getRemindersUseCase(
            onResult = { result ->
                val reminders = (result as? RemindersResultDomainModel.Loaded)?.reminders
                when {
                    reminders == null -> notify(
                        RemindersPresentationNotification.SomethingWentWrong
                    )
                    reminders.notificationsAllowed -> turnOn()
                    reminders.mayAskPermission ->
                        notify(RemindersPresentationNotification.AskForPermission(kind))
                    else -> load()
                }
            }
        )
    }

    private fun setDaily(on: Boolean, time: LocalTime) {
        setDailyReminderUseCase(
            value = SetDailyReminderRequestDomainModel(on, time),
            onResult = ::reportWrite
        )
    }

    private fun setEffect(on: Boolean) {
        setEffectReminderUseCase(value = on, onResult = ::reportWrite)
    }

    private fun reportWrite(result: ReminderWriteResultDomainModel) {
        when (result) {
            ReminderWriteResultDomainModel.Done -> Unit
            ReminderWriteResultDomainModel.StorageFull ->
                notify(RemindersPresentationNotification.StorageFull)
            ReminderWriteResultDomainModel.Failed ->
                notify(RemindersPresentationNotification.SomethingWentWrong)
        }
        load()
    }

    private fun load() {
        getRemindersUseCase(
            onResult = { updateViewState(remindersPresentationMapper.toPresentation(it)) }
        )
    }
}
