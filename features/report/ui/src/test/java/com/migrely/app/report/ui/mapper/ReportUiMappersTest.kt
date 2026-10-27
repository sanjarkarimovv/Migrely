package com.migrely.app.report.ui.mapper

import com.migrely.app.report.presentation.model.ReportOptionsPresentationModel
import com.migrely.app.report.presentation.model.ReportPeriodPresentationModel
import com.migrely.app.report.presentation.model.ReportPresentationNotification
import com.migrely.app.report.presentation.model.ReportViewState
import com.migrely.app.report.ui.model.ReportEventUiModel
import com.migrely.app.report.ui.model.ReportMessageUiModel
import com.migrely.app.report.ui.model.ReportOptionsUiModel
import com.migrely.app.report.ui.model.ReportPeriodUiModel
import com.migrely.app.report.ui.model.ReportViewStateUiModel
import java.time.LocalDate
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ReportUiMappersTest {
    @Test
    fun `When mapping the state then the dates as epoch days`() {
        assertEquals(
            ReportViewStateUiModel.Ready(
                ReportOptionsUiModel(20_660, 20_751, anythingLogged = true)
            ),
            ReportViewStateUiMapper().toUi(
                ReportViewState.Ready(
                    ReportOptionsPresentationModel(
                        LocalDate.ofEpochDay(20_660),
                        LocalDate.ofEpochDay(20_751),
                        true
                    )
                )
            )
        )
        assertEquals(
            ReportViewStateUiModel.Working,
            ReportViewStateUiMapper().toUi(ReportViewState.Working)
        )
    }

    @Test
    fun `When mapping the period then months, or the range's dates`() {
        assertEquals(
            ReportPeriodPresentationModel.LastMonths(6),
            ReportPeriodPresentationMapper().toPresentation(ReportPeriodUiModel.LastMonths(6))
        )
        assertEquals(
            ReportPeriodPresentationModel.Range(LocalDate.ofEpochDay(1), LocalDate.ofEpochDay(2)),
            ReportPeriodPresentationMapper().toPresentation(ReportPeriodUiModel.Range(1, 2))
        )
    }

    @Test
    fun `When a notification is presented then share or a message`() = runTest {
        val presenter = ReportEventPresenter()
        val classUnderTest = ReportNotificationUiMapper(presenter)
        mapOf(
            ReportPresentationNotification.Share("content://r") to
                ReportEventUiModel.Share("content://r"),
            ReportPresentationNotification.StorageFull to
                ReportEventUiModel.Message(ReportMessageUiModel.StorageFull),
            ReportPresentationNotification.NothingLogged to
                ReportEventUiModel.Message(ReportMessageUiModel.NothingLogged),
            ReportPresentationNotification.SomethingWentWrong to
                ReportEventUiModel.Message(ReportMessageUiModel.SomethingWentWrong)
        ).forEach { (notification, expected) ->
            val shown = async(start = UNDISPATCHED) { presenter.events.first() }
            classUnderTest.toUi(notification).present()
            assertEquals(expected, shown.await())
        }
    }
}
