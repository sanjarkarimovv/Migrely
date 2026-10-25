package com.migrely.app.report.ui.mapper

import com.migrely.app.designsystem.snackbar.SnackbarTone
import com.migrely.app.report.ui.model.ReportMessageBehaviourUiModel
import com.migrely.app.report.ui.model.ReportMessageUiModel
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class ReportMessageBehaviourUiMapperTest(
    private val message: ReportMessageUiModel,
    private val expected: ReportMessageBehaviourUiModel
) {
    companion object {
        private val reading = ReportMessageBehaviourUiModel(SnackbarTone.Error, 8_000L)

        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun data(): Collection<Array<*>> = listOf(
            arrayOf<Any>(ReportMessageUiModel.NothingLogged, reading),
            arrayOf<Any>(ReportMessageUiModel.SomethingWentWrong, reading),
            arrayOf<Any>(ReportMessageUiModel.NoAppToShare, reading),
            arrayOf<Any>(
                ReportMessageUiModel.StorageFull,
                ReportMessageBehaviourUiModel(SnackbarTone.Error, null)
            )
        )
    }

    @Test
    fun `When mapping a message then returns its tone and duration`() {
        // When
        val actual = ReportMessageBehaviourUiMapper().toUi(message)

        // Then
        assertEquals(expected, actual)
    }
}
