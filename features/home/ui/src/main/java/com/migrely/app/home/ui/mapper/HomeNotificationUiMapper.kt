package com.migrely.app.home.ui.mapper

import com.migrely.app.architecture.ui.notification.mapper.NotificationUiMapper
import com.migrely.app.architecture.ui.notification.model.UiNotification
import com.migrely.app.home.presentation.model.HomePresentationNotification
import com.migrely.app.home.ui.model.HomeMessageUiModel
import com.migrely.app.home.ui.notification.HomeMessagePresenter

class HomeNotificationUiMapper(private val homeMessagePresenter: HomeMessagePresenter) :
    NotificationUiMapper<HomePresentationNotification> {
    override fun toUi(notification: HomePresentationNotification): UiNotification {
        val message = when (notification) {
            is HomePresentationNotification.AttackStarted -> HomeMessageUiModel.AttackStarted(
                notification.attackId
            )
            is HomePresentationNotification.AttackEnded -> HomeMessageUiModel.AttackEnded(
                notification.attackId
            )
            HomePresentationNotification.StorageFull -> HomeMessageUiModel.StorageFull
            is HomePresentationNotification.StartMoveRejected ->
                HomeMessageUiModel.StartMoveRejected(notification.previousEnd.toEpochMilli())

            HomePresentationNotification.EndTimeOutOfRange -> HomeMessageUiModel.EndTimeOutOfRange
            HomePresentationNotification.SomethingWentWrong -> HomeMessageUiModel.SomethingWentWrong
        }
        return UiNotification { homeMessagePresenter.show(message) }
    }
}
