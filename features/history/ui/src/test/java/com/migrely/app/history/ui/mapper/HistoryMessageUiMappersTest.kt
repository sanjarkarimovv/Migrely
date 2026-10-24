package com.migrely.app.history.ui.mapper

import com.migrely.app.designsystem.sky.DaySkyAppearance
import com.migrely.app.designsystem.snackbar.SnackbarTone
import com.migrely.app.history.presentation.model.HistoryPresentationNotification
import com.migrely.app.history.ui.model.DaySkyUiModel
import com.migrely.app.history.ui.model.HistoryMessageBehaviourUiModel
import com.migrely.app.history.ui.model.HistoryMessageUiModel
import com.migrely.app.history.ui.notification.HistoryMessagePresenter
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class HistoryMessageUiMappersTest {
    @Test
    fun `When mapping each notification then the presenter shows its message`() {
        val pairs = listOf(
            HistoryPresentationNotification.AttackDeleted("gone", Instant.ofEpochMilli(1_000L)) to
                HistoryMessageUiModel.AttackDeleted("gone", 1_000L),
            HistoryPresentationNotification.Undone to HistoryMessageUiModel.Undone,
            HistoryPresentationNotification.UndoNotPossible to
                HistoryMessageUiModel.UndoNotPossible,
            HistoryPresentationNotification.SomethingWentWrong to
                HistoryMessageUiModel.SomethingWentWrong
        )
        pairs.forEach { (notification, expected) ->
            val presenter: HistoryMessagePresenter = mock()

            HistoryNotificationUiMapper(presenter).toUi(notification).present()

            verify(presenter).show(expected)
        }
    }

    @Test
    fun `When mapping behaviours then a deletion offers undo for 5 seconds`() {
        val classUnderTest = HistoryMessageBehaviourUiMapper()

        assertEquals(
            HistoryMessageBehaviourUiModel(SnackbarTone.Confirmation, true, 5_000L),
            classUnderTest.toUi(HistoryMessageUiModel.AttackDeleted("gone", 1L))
        )
        assertEquals(
            HistoryMessageBehaviourUiModel(SnackbarTone.Information, false, 8_000L),
            classUnderTest.toUi(HistoryMessageUiModel.Undone)
        )
        assertEquals(
            HistoryMessageBehaviourUiModel(SnackbarTone.Information, false, 8_000L),
            classUnderTest.toUi(HistoryMessageUiModel.UndoNotPossible)
        )
        assertEquals(
            HistoryMessageBehaviourUiModel(SnackbarTone.Error, false, 8_000L),
            classUnderTest.toUi(HistoryMessageUiModel.SomethingWentWrong)
        )
    }

    @Test
    fun `When mapping each day sky then its design-system appearance`() {
        val classUnderTest = DaySkyAppearanceUiMapper()

        assertEquals(
            DaySkyAppearance.entries.toList(),
            DaySkyUiModel.entries.map(classUnderTest::toAppearance)
        )
    }
}
