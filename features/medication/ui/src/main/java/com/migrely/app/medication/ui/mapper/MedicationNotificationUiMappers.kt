package com.migrely.app.medication.ui.mapper

import com.migrely.app.architecture.ui.notification.mapper.NotificationUiMapper
import com.migrely.app.architecture.ui.notification.model.UiNotification
import com.migrely.app.medication.presentation.model.EditMedicationPresentationNotification
import com.migrely.app.medication.presentation.model.LogMedicationPresentationNotification
import com.migrely.app.medication.presentation.model.MedicationsPresentationNotification
import com.migrely.app.medication.ui.model.MedicationEventUiModel
import com.migrely.app.medication.ui.model.MedicationEventUiModel.Message
import com.migrely.app.medication.ui.model.MedicationMessageUiModel
import com.migrely.app.medication.ui.model.SaveProblemUiModel
import com.migrely.app.medication.ui.notification.MedicationEventPresenter

class MedicationsNotificationUiMapper(private val presenter: MedicationEventPresenter) :
    NotificationUiMapper<MedicationsPresentationNotification> {
    override fun toUi(notification: MedicationsPresentationNotification): UiNotification {
        val message = when (notification) {
            is MedicationsPresentationNotification.Archived ->
                MedicationMessageUiModel.Archived(notification.medicationId)
            MedicationsPresentationNotification.Undone -> MedicationMessageUiModel.Undone
            MedicationsPresentationNotification.StorageFull -> MedicationMessageUiModel.StorageFull
            MedicationsPresentationNotification.SomethingWentWrong ->
                MedicationMessageUiModel.SomethingWentWrong
        }
        return UiNotification { presenter.show(Message(message)) }
    }
}

class EditMedicationNotificationUiMapper(
    private val presenter: MedicationEventPresenter,
    private val saveProblemUiMapper: SaveProblemUiMapper
) : NotificationUiMapper<EditMedicationPresentationNotification> {
    override fun toUi(notification: EditMedicationPresentationNotification): UiNotification {
        val event = when (notification) {
            is EditMedicationPresentationNotification.NotSaved ->
                saveProblemUiMapper.toUi(notification.problem).toEvent()
            EditMedicationPresentationNotification.StorageFull ->
                Message(MedicationMessageUiModel.StorageFull)
            EditMedicationPresentationNotification.SomethingWentWrong ->
                Message(MedicationMessageUiModel.SomethingWentWrong)
        }
        return UiNotification { presenter.show(event) }
    }
}

class LogMedicationNotificationUiMapper(
    private val presenter: MedicationEventPresenter,
    private val saveProblemUiMapper: SaveProblemUiMapper,
    private val intakeUiMapper: IntakeUiMapper
) : NotificationUiMapper<LogMedicationPresentationNotification> {
    override fun toUi(notification: LogMedicationPresentationNotification): UiNotification {
        val event = when (notification) {
            is LogMedicationPresentationNotification.Logged ->
                MedicationEventUiModel.Logged(intakeUiMapper.toUi(notification.intake))
            LogMedicationPresentationNotification.IntakeUndone ->
                Message(MedicationMessageUiModel.IntakeUndone)
            is LogMedicationPresentationNotification.NotSaved ->
                saveProblemUiMapper.toUi(notification.problem).toEvent()
            LogMedicationPresentationNotification.InFuture -> Message(
                MedicationMessageUiModel.InFuture
            )
            LogMedicationPresentationNotification.StorageFull ->
                Message(MedicationMessageUiModel.StorageFull)
            LogMedicationPresentationNotification.SomethingWentWrong ->
                Message(MedicationMessageUiModel.SomethingWentWrong)
        }
        return UiNotification { presenter.show(event) }
    }
}

/** The limit is a message; the other problems belong to a form field. */
private fun SaveProblemUiModel.toEvent(): MedicationEventUiModel = when (this) {
    SaveProblemUiModel.LimitReached -> Message(MedicationMessageUiModel.LimitReached)
    is SaveProblemUiModel.Duplicate,
    SaveProblemUiModel.InvalidName,
    SaveProblemUiModel.InvalidDose -> MedicationEventUiModel.NotSaved(this)
}
