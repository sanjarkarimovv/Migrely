package com.migrely.app.attack.ui.mapper

import com.migrely.app.architecture.ui.notification.mapper.NotificationUiMapper
import com.migrely.app.architecture.ui.notification.model.UiNotification
import com.migrely.app.attack.presentation.model.AddPastAttackPresentationNotification
import com.migrely.app.attack.ui.model.AddPastAttackEventUiModel
import com.migrely.app.attack.ui.model.AttackMessageUiModel
import com.migrely.app.attack.ui.model.PastAttackProblemUiModel
import com.migrely.app.attack.ui.model.TimeErrorReasonUiModel
import com.migrely.app.attack.ui.model.TimeErrorUiModel
import com.migrely.app.attack.ui.model.TimeFieldUiModel
import com.migrely.app.attack.ui.notification.AddPastAttackEventPresenter

class AddPastAttackNotificationUiMapper(
    private val addPastAttackEventPresenter: AddPastAttackEventPresenter,
    private val timeFieldUiMapper: TimeFieldUiMapper,
    private val attackRangeUiMapper: AttackRangeUiMapper
) : NotificationUiMapper<AddPastAttackPresentationNotification> {
    override fun toUi(notification: AddPastAttackPresentationNotification): UiNotification {
        val event = when (notification) {
            is AddPastAttackPresentationNotification.InFuture -> problem(
                PastAttackProblemUiModel.Time(
                    TimeErrorUiModel(
                        timeFieldUiMapper.toUi(notification.field),
                        TimeErrorReasonUiModel.InFuture
                    )
                )
            )

            AddPastAttackPresentationNotification.EndBeforeStart -> problem(
                PastAttackProblemUiModel.Time(
                    TimeErrorUiModel(TimeFieldUiModel.End, TimeErrorReasonUiModel.EndBeforeStart)
                )
            )

            is AddPastAttackPresentationNotification.Overlaps -> problem(
                PastAttackProblemUiModel.Overlaps(
                    notification.attacks.map(attackRangeUiMapper::toUi)
                )
            )

            AddPastAttackPresentationNotification.StorageFull ->
                AddPastAttackEventUiModel.Message(AttackMessageUiModel.StorageFull)

            AddPastAttackPresentationNotification.SomethingWentWrong ->
                AddPastAttackEventUiModel.Message(AttackMessageUiModel.SomethingWentWrong)
        }
        return UiNotification { addPastAttackEventPresenter.show(event) }
    }

    private fun problem(problem: PastAttackProblemUiModel) =
        AddPastAttackEventUiModel.Problem(problem)
}
