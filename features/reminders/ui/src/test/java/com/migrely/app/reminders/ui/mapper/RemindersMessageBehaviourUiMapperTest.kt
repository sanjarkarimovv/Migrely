package com.migrely.app.reminders.ui.mapper

import com.migrely.app.designsystem.snackbar.SnackbarTone
import com.migrely.app.reminders.ui.model.DoctorVisitUiModel
import com.migrely.app.reminders.ui.model.RemindersMessageBehaviourUiModel
import com.migrely.app.reminders.ui.model.RemindersMessageUiModel
import org.junit.Assert.assertEquals
import org.junit.Test

class RemindersMessageBehaviourUiMapperTest {
    private val classUnderTest = RemindersMessageBehaviourUiMapper()

    @Test
    fun `When mapping a failure then it stays 8 s, and storage full until closed`() {
        assertEquals(
            RemindersMessageBehaviourUiModel(SnackbarTone.Error, 8_000L),
            classUnderTest.toUi(RemindersMessageUiModel.SomethingWentWrong)
        )
        assertEquals(
            RemindersMessageBehaviourUiModel(SnackbarTone.Error, null),
            classUnderTest.toUi(RemindersMessageUiModel.StorageFull)
        )
    }

    @Test
    fun `When mapping Saved then a confirmation for 8 s`() {
        assertEquals(
            RemindersMessageBehaviourUiModel(SnackbarTone.Confirmation, 8_000L),
            classUnderTest.toUi(RemindersMessageUiModel.Saved)
        )
    }

    @Test
    fun `When mapping Visit removed or Undone then information, for 5 s and 8 s`() {
        assertEquals(
            RemindersMessageBehaviourUiModel(SnackbarTone.Information, 5_000L),
            classUnderTest.toUi(
                RemindersMessageUiModel.VisitRemoved(DoctorVisitUiModel("v", 20_777L))
            )
        )
        assertEquals(
            RemindersMessageBehaviourUiModel(SnackbarTone.Information, 8_000L),
            classUnderTest.toUi(RemindersMessageUiModel.Undone)
        )
    }
}
