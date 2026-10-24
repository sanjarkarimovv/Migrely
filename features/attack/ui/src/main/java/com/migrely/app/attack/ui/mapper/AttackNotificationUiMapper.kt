package com.migrely.app.attack.ui.mapper

import com.migrely.app.architecture.ui.notification.mapper.NotificationUiMapper
import com.migrely.app.architecture.ui.notification.model.UiNotification
import com.migrely.app.attack.presentation.model.AttackDetailsPresentationNotification
import com.migrely.app.attack.presentation.model.IntensityPointPresentationModel
import com.migrely.app.attack.presentation.model.TimeRejectionPresentationModel
import com.migrely.app.attack.ui.model.AttackDetailsEventUiModel
import com.migrely.app.attack.ui.model.AttackMessageUiModel
import com.migrely.app.attack.ui.model.IntensityPointUiModel
import com.migrely.app.attack.ui.model.TimeErrorReasonUiModel
import com.migrely.app.attack.ui.model.TimeErrorUiModel
import com.migrely.app.attack.ui.model.TimesUndoUiModel
import com.migrely.app.attack.ui.notification.AttackDetailsEventPresenter

class AttackNotificationUiMapper(
    private val attackDetailsEventPresenter: AttackDetailsEventPresenter,
    private val timeFieldUiMapper: TimeFieldUiMapper
) : NotificationUiMapper<AttackDetailsPresentationNotification> {
    override fun toUi(notification: AttackDetailsPresentationNotification): UiNotification {
        val event = when (notification) {
            is AttackDetailsPresentationNotification.TimeRejected ->
                AttackDetailsEventUiModel.TimeRejected(
                    TimeErrorUiModel(
                        timeFieldUiMapper
                            .toUi(notification.field),
                        reasonOf(notification.reason)
                    )
                )

            // The pickers keep times inside the attack, so a rejected entry only follows a race; say so briefly.
            is AttackDetailsPresentationNotification.PointRejected ->
                AttackDetailsEventUiModel.Message(AttackMessageUiModel.PointOutsideAttack)

            is AttackDetailsPresentationNotification.PointsRemovedByTimeChange ->
                AttackDetailsEventUiModel.Message(
                    AttackMessageUiModel.PointsRemovedByTimeChange(
                        TimesUndoUiModel(
                            attackId = notification.undo.attackId,
                            startEpochMillis = notification.undo.start.toEpochMilli(),
                            endEpochMillis = notification.undo.end?.toEpochMilli(),
                            removedPoints = notification.undo.removedPoints.map(::pointOf)
                        )
                    )
                )

            is AttackDetailsPresentationNotification.PointRemoved ->
                AttackDetailsEventUiModel.Message(
                    AttackMessageUiModel.PointRemoved(
                        notification.attackId,
                        pointOf(notification.point)
                    )
                )

            AttackDetailsPresentationNotification.FirstMaximumRecorded ->
                AttackDetailsEventUiModel.FirstMaximumRecorded
            AttackDetailsPresentationNotification.TagLimitReached ->
                AttackDetailsEventUiModel.Message(AttackMessageUiModel.TagLimitReached)

            AttackDetailsPresentationNotification.Undone ->
                AttackDetailsEventUiModel.Message(AttackMessageUiModel.Undone)
            AttackDetailsPresentationNotification.UndoNotPossible ->
                AttackDetailsEventUiModel.Message(AttackMessageUiModel.UndoNotPossible)

            AttackDetailsPresentationNotification.StorageFull ->
                AttackDetailsEventUiModel.Message(AttackMessageUiModel.StorageFull)

            AttackDetailsPresentationNotification.SomethingWentWrong ->
                AttackDetailsEventUiModel.Message(AttackMessageUiModel.SomethingWentWrong)
        }
        return UiNotification { attackDetailsEventPresenter.show(event) }
    }

    private fun reasonOf(reason: TimeRejectionPresentationModel): TimeErrorReasonUiModel = when (
        reason
    ) {
        TimeRejectionPresentationModel.InFuture -> TimeErrorReasonUiModel.InFuture
        TimeRejectionPresentationModel.EndBeforeStart -> TimeErrorReasonUiModel.EndBeforeStart
        is TimeRejectionPresentationModel.Overlaps ->
            TimeErrorReasonUiModel.Overlaps(reason.start.toEpochMilli(), reason.end?.toEpochMilli())
    }

    private fun pointOf(point: IntensityPointPresentationModel) =
        IntensityPointUiModel(point.at.toEpochMilli(), point.value)
}
