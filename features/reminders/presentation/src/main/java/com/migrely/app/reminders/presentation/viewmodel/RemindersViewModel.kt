package com.migrely.app.reminders.presentation.viewmodel

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.architecture.presentation.viewmodel.BaseViewModel
import com.migrely.app.reminders.domain.model.ReminderWriteResultDomainModel
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
import com.migrely.app.reminders.presentation.model.RemindersPresentationNotification
import com.migrely.app.reminders.presentation.model.RemindersViewState
import com.migrely.app.reminders.presentation.navigation.RemindersPresentationNavigationEvent
import java.time.LocalDate
import java.time.LocalTime

/**
 * The Reminders screen (BRD D65–D68). Turning a reminder on needs notifications: when they are off, permission is asked
 * for if the 30-day rule allows it (EC-REM-01, EC-REM-02); otherwise the screen shows why it stays off. The UI passes
 * back the time shown and which switch asked, so the view model keeps no state. The doctor visit is saved at once, and
 * permission is asked for after it by the same rule; "Remove" offers Undo with the removed visit (BRD D168). Only the
 * latest read is shown, so a late first read never hides a newer save. A write with no message of its own sends
 * [RemindersPresentationNotification.Saved], and the screen decides what it means (BRD D178 (1)).
 */
class RemindersViewModel(
    private val getRemindersUseCase: GetRemindersUseCase,
    private val setDailyReminderUseCase: SetDailyReminderUseCase,
    private val setEffectReminderUseCase: SetEffectReminderUseCase,
    private val recordPermissionAskedUseCase: RecordPermissionAskedUseCase,
    private val setDoctorVisitUseCase: SetDoctorVisitUseCase,
    private val removeDoctorVisitUseCase: RemoveDoctorVisitUseCase,
    private val restoreDoctorVisitUseCase: RestoreDoctorVisitUseCase,
    private val remindersPresentationMapper: RemindersPresentationMapper,
    private val doctorVisitPresentationMapper: DoctorVisitPresentationMapper,
    useCaseExecutor: UseCaseExecutor
) : BaseViewModel<RemindersViewState, RemindersPresentationNotification>(useCaseExecutor) {
    /** The number of the latest read; reads are made and answered on the main thread. */
    private var latestLoad = 0

    fun onEnter() = load()

    fun onResume() = load()

    /** The phone's date may have moved while the screen shows: midnight, a time set by hand, a new zone (issue #187). */
    fun onClockChanged() = load()

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

    /** A day picked for the next doctor visit (BRD EC-REM-07); the same day again changes nothing (EC-REM-09). */
    fun onVisitDateAction(date: LocalDate) {
        setDoctorVisitUseCase(
            value = date,
            onResult = { result ->
                when (result) {
                    // The date is written either way, so an error it made untrue goes (BRD D178 (1), ux F4).
                    is SetDoctorVisitResultDomainModel.Saved -> {
                        notify(RemindersPresentationNotification.Saved)
                        if (result.askForPermission) {
                            notify(
                                RemindersPresentationNotification.AskForPermission(
                                    ReminderKindPresentationModel.Visit
                                )
                            )
                        }
                    }
                    SetDoctorVisitResultDomainModel.Unchanged -> Unit
                    SetDoctorVisitResultDomainModel.StorageFull ->
                        notify(RemindersPresentationNotification.StorageFull)
                    SetDoctorVisitResultDomainModel.Failed ->
                        notify(RemindersPresentationNotification.SomethingWentWrong)
                }
                load()
            }
        )
    }

    fun onRemoveVisitAction() {
        removeDoctorVisitUseCase(
            onResult = { result ->
                when (result) {
                    is RemoveDoctorVisitResultDomainModel.Removed -> notify(
                        RemindersPresentationNotification.VisitRemoved(
                            doctorVisitPresentationMapper.toPresentation(result.visit)
                        )
                    )
                    RemoveDoctorVisitResultDomainModel.NothingToRemove -> Unit
                    RemoveDoctorVisitResultDomainModel.StorageFull ->
                        notify(RemindersPresentationNotification.StorageFull)
                    RemoveDoctorVisitResultDomainModel.Failed ->
                        notify(RemindersPresentationNotification.SomethingWentWrong)
                }
                load()
            }
        )
    }

    fun onUndoRemoveVisitAction(visit: DoctorVisitPresentationModel) {
        restoreDoctorVisitUseCase(
            value = doctorVisitPresentationMapper.toDomain(visit),
            onResult = { result ->
                when (result) {
                    ReminderWriteResultDomainModel.Done -> notify(
                        RemindersPresentationNotification.Undone
                    )
                    ReminderWriteResultDomainModel.StorageFull ->
                        notify(RemindersPresentationNotification.StorageFull)
                    ReminderWriteResultDomainModel.Failed ->
                        notify(RemindersPresentationNotification.SomethingWentWrong)
                }
                load()
            }
        )
    }

    /** The system's answer to a request for [kind]: when allowed, that reminder is turned on; a visit stays saved. */
    fun onPermissionResultAction(
        granted: Boolean,
        kind: ReminderKindPresentationModel,
        time: LocalTime
    ) {
        recordPermissionAskedUseCase(
            onResult = {
                when {
                    !granted -> load()
                    else -> when (kind) {
                        ReminderKindPresentationModel.Daily -> setDaily(true, time)
                        ReminderKindPresentationModel.Effect -> setEffect(true)
                        ReminderKindPresentationModel.Visit -> load()
                    }
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
            ReminderWriteResultDomainModel.Done -> notify(RemindersPresentationNotification.Saved)
            ReminderWriteResultDomainModel.StorageFull ->
                notify(RemindersPresentationNotification.StorageFull)
            ReminderWriteResultDomainModel.Failed ->
                notify(RemindersPresentationNotification.SomethingWentWrong)
        }
        load()
    }

    private fun load() {
        val load = ++latestLoad
        getRemindersUseCase(
            onResult = { result ->
                val latest = load == latestLoad
                if (latest) updateViewState(remindersPresentationMapper.toPresentation(result))
            }
        )
    }
}
