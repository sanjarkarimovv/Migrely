package com.migrely.app.backup.ui.mapper

import com.migrely.app.backup.ui.model.YourDataMessageBehaviourUiModel
import com.migrely.app.backup.ui.model.YourDataMessageUiModel
import com.migrely.app.designsystem.snackbar.SnackbarTone
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/** QA #56: every message stays 8 s before the accessibility timeout, except "storage full", which waits for the user. */
@RunWith(Parameterized::class)
class YourDataMessageBehaviourUiMapperTest(
    private val message: YourDataMessageUiModel,
    private val expected: YourDataMessageBehaviourUiModel
) {
    companion object {
        private val confirmation =
            YourDataMessageBehaviourUiModel(SnackbarTone.Confirmation, 8_000L)
        private val information = YourDataMessageBehaviourUiModel(SnackbarTone.Information, 8_000L)
        private val error = YourDataMessageBehaviourUiModel(SnackbarTone.Error, 8_000L)

        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun data(): Collection<Array<*>> = listOf(
            arrayOf<Any>(YourDataMessageUiModel.BackupSaved, confirmation),
            arrayOf<Any>(YourDataMessageUiModel.CsvSaved, confirmation),
            arrayOf<Any>(YourDataMessageUiModel.Replaced(10), confirmation),
            arrayOf<Any>(YourDataMessageUiModel.Restored(16), confirmation),
            arrayOf<Any>(YourDataMessageUiModel.Added(1, 1, 0), confirmation),
            arrayOf<Any>(YourDataMessageUiModel.Added(0, 4, 2), confirmation),
            arrayOf<Any>(YourDataMessageUiModel.Added(0, 4, 0), information),
            arrayOf<Any>(YourDataMessageUiModel.NotABackup, error),
            arrayOf<Any>(YourDataMessageUiModel.Newer, error),
            arrayOf<Any>(YourDataMessageUiModel.SomethingWentWrong, error),
            arrayOf<Any>(YourDataMessageUiModel.NoFilePicker, error),
            arrayOf<Any>(
                YourDataMessageUiModel.StorageFull,
                YourDataMessageBehaviourUiModel(SnackbarTone.Error, null)
            )
        )
    }

    @Test
    fun `When mapping a message then returns its tone and duration`() {
        // When
        val actual = YourDataMessageBehaviourUiMapper().toUi(message)

        // Then
        assertEquals(expected, actual)
    }
}
