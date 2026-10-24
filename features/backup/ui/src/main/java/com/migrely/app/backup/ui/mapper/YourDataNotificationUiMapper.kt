package com.migrely.app.backup.ui.mapper

import com.migrely.app.architecture.ui.notification.mapper.NotificationUiMapper
import com.migrely.app.architecture.ui.notification.model.UiNotification
import com.migrely.app.backup.presentation.model.BackupSummaryPresentationModel
import com.migrely.app.backup.presentation.model.YourDataPresentationNotification
import com.migrely.app.backup.ui.model.BackupSummaryUiModel
import com.migrely.app.backup.ui.model.PendingImportUiModel
import com.migrely.app.backup.ui.model.YourDataEventUiModel
import com.migrely.app.backup.ui.model.YourDataMessageUiModel

class YourDataNotificationUiMapper(private val presenter: YourDataEventPresenter) :
    NotificationUiMapper<YourDataPresentationNotification> {
    override fun toUi(notification: YourDataPresentationNotification): UiNotification {
        val event = when (notification) {
            is YourDataPresentationNotification.ConfirmImport -> YourDataEventUiModel.Confirm(
                PendingImportUiModel(
                    uri = notification.uri,
                    summary = toUi(notification.summary),
                    phoneHasRecords = notification.phoneHasRecords
                )
            )

            YourDataPresentationNotification.BackupSaved -> message(
                YourDataMessageUiModel.BackupSaved
            )
            YourDataPresentationNotification.CsvSaved -> message(YourDataMessageUiModel.CsvSaved)
            is YourDataPresentationNotification.Replaced ->
                message(YourDataMessageUiModel.Replaced(notification.summary.attacks))

            is YourDataPresentationNotification.Added -> message(
                YourDataMessageUiModel.Added(
                    notification.addedAttacks,
                    notification.skippedAttacks,
                    notification.addedOthers
                )
            )

            YourDataPresentationNotification.NotABackup -> message(
                YourDataMessageUiModel.NotABackup
            )
            YourDataPresentationNotification.Newer -> message(YourDataMessageUiModel.Newer)
            YourDataPresentationNotification.StorageFull -> message(
                YourDataMessageUiModel.StorageFull
            )
            YourDataPresentationNotification.SomethingWentWrong ->
                message(YourDataMessageUiModel.SomethingWentWrong)
        }
        return UiNotification { presenter.show(event) }
    }

    private fun message(message: YourDataMessageUiModel) = YourDataEventUiModel.Message(message)

    private fun toUi(summary: BackupSummaryPresentationModel) =
        BackupSummaryUiModel(summary.attacks, summary.medications, summary.dayMarks)
}
