package com.migrely.app.reminders.ui.mapper

import com.migrely.app.designsystem.snackbar.SnackbarTone
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
}
