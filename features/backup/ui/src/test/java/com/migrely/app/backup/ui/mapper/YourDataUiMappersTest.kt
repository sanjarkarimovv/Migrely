package com.migrely.app.backup.ui.mapper

import com.migrely.app.backup.presentation.model.BackupSummaryPresentationModel
import com.migrely.app.backup.presentation.model.YourDataPresentationNotification
import com.migrely.app.backup.presentation.model.YourDataViewState
import com.migrely.app.backup.ui.model.BackupSummaryUiModel
import com.migrely.app.backup.ui.model.PendingImportUiModel
import com.migrely.app.backup.ui.model.YourDataEventUiModel
import com.migrely.app.backup.ui.model.YourDataMessageUiModel
import com.migrely.app.backup.ui.model.YourDataViewStateUiModel
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

private val SUMMARY = BackupSummaryPresentationModel(attacks = 10, medications = 2, dayMarks = 5)

class YourDataUiMappersTest {
    @Test
    fun `When mapping the state then idle or working`() {
        assertEquals(
            YourDataViewStateUiModel.Idle,
            YourDataViewStateUiMapper().toUi(YourDataViewState.Idle)
        )
        assertEquals(
            YourDataViewStateUiModel.Working,
            YourDataViewStateUiMapper().toUi(YourDataViewState.Working)
        )
    }

    @Test
    fun `When a notification is presented then the presenter shows its event`() = runTest {
        val presenter = YourDataEventPresenter()
        val classUnderTest = YourDataNotificationUiMapper(presenter)
        mapOf(
            YourDataPresentationNotification.ConfirmImport("content://1", SUMMARY, true) to
                YourDataEventUiModel.Confirm(
                    PendingImportUiModel("content://1", BackupSummaryUiModel(10, 2, 5), true)
                ),
            YourDataPresentationNotification.BackupSaved to
                message(YourDataMessageUiModel.BackupSaved),
            YourDataPresentationNotification.CsvSaved to message(YourDataMessageUiModel.CsvSaved),
            YourDataPresentationNotification.Replaced(SUMMARY) to
                message(YourDataMessageUiModel.Replaced(10)),
            YourDataPresentationNotification.Added(6, 4, 1) to
                message(YourDataMessageUiModel.Added(6, 4, 1)),
            YourDataPresentationNotification.NotABackup to
                message(YourDataMessageUiModel.NotABackup),
            YourDataPresentationNotification.Newer to message(YourDataMessageUiModel.Newer),
            YourDataPresentationNotification.StorageFull to
                message(YourDataMessageUiModel.StorageFull),
            YourDataPresentationNotification.SomethingWentWrong to
                message(YourDataMessageUiModel.SomethingWentWrong)
        ).forEach { (notification, expected) ->
            val shown = async(start = UNDISPATCHED) { presenter.events.first() }

            classUnderTest.toUi(notification).present()

            assertEquals(expected, shown.await())
        }
    }

    private fun message(message: YourDataMessageUiModel) = YourDataEventUiModel.Message(message)
}
