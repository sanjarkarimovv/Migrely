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
        private val error = ReportMessageBehaviourUiModel(SnackbarTone.Error, 8_000L)

        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun data(): Collection<Array<*>> = listOf(
            arrayOf<Any>(
                ReportMessageUiModel.NothingLogged,
                ReportMessageBehaviourUiModel(SnackbarTone.Information, 8_000L)
            ),
            arrayOf<Any>(ReportMessageUiModel.SomethingWentWrong, error),
            arrayOf<Any>(ReportMessageUiModel.NoAppToShare, error),
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
