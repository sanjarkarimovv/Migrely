package com.migrely.app.history.ui.mapper

import com.migrely.app.designsystem.snackbar.SnackbarTone
import com.migrely.app.history.ui.model.HistoryMessageBehaviourUiModel
import com.migrely.app.history.ui.model.HistoryMessageUiModel
import org.junit.Assert.assertEquals
import org.junit.Test

/** "Saved." on History behaves as on Attack details (BRD D178 (1), EC-UX-35). */
class HistoryMessageBehaviourUiMapperTest {
    private val classUnderTest = HistoryMessageBehaviourUiMapper()

    @Test
    fun `When mapping Saved then a confirmation for 8 s with no undo`() {
        assertEquals(
            HistoryMessageBehaviourUiModel(SnackbarTone.Confirmation, false, 8_000L),
            classUnderTest.toUi(HistoryMessageUiModel.Saved)
        )
    }

    @Test
    fun `When mapping storage full then an error that stays until closed`() {
        assertEquals(
            HistoryMessageBehaviourUiModel(SnackbarTone.Error, false, null),
            classUnderTest.toUi(HistoryMessageUiModel.StorageFull)
        )
    }
}
