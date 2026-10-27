package com.migrely.app.home.ui.mapper

import com.migrely.app.designsystem.snackbar.SnackbarTone
import com.migrely.app.home.ui.model.HomeMessageBehaviourUiModel
import com.migrely.app.home.ui.model.HomeMessageUiModel
import com.migrely.app.home.ui.model.HomeUndoTargetUiModel
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class HomeMessageBehaviourUiMapperTest(
    private val message: HomeMessageUiModel,
    private val expected: HomeMessageBehaviourUiModel
) {
    companion object {
        private val information =
            HomeMessageBehaviourUiModel(SnackbarTone.Information, null, 8_000L)

        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun data(): Collection<Array<*>> = listOf(
            arrayOf<Any>(
                HomeMessageUiModel.AttackStarted("a"),
                HomeMessageBehaviourUiModel(
                    SnackbarTone.Confirmation,
                    HomeUndoTargetUiModel.Start,
                    5_000L
                )
            ),
            arrayOf<Any>(
                HomeMessageUiModel.AttackEnded("a"),
                HomeMessageBehaviourUiModel(
                    SnackbarTone.Confirmation,
                    HomeUndoTargetUiModel.End,
                    5_000L
                )
            ),
            arrayOf<Any>(
                HomeMessageUiModel.AttackDeleted("a", 1L),
                HomeMessageBehaviourUiModel(
                    SnackbarTone.Confirmation,
                    HomeUndoTargetUiModel.Delete,
                    5_000L
                )
            ),
            arrayOf<Any>(HomeMessageUiModel.Undone, information),
            arrayOf<Any>(HomeMessageUiModel.UndoNotPossible, information),
            arrayOf<Any>(HomeMessageUiModel.StartMoveRejected(1_000L), information),
            arrayOf<Any>(HomeMessageUiModel.EndTimeOutOfRange, information),
            arrayOf<Any>(
                HomeMessageUiModel.SomethingWentWrong,
                HomeMessageBehaviourUiModel(SnackbarTone.Error, null, 8_000L)
            ),
            arrayOf<Any>(
                HomeMessageUiModel.StorageFull,
                HomeMessageBehaviourUiModel(SnackbarTone.Error, null, null)
            )
        )
    }

    @Test
    fun `When mapping a message then returns its tone, undo and duration`() {
        // When
        val actual = HomeMessageBehaviourUiMapper().toUi(message)

        // Then
        assertEquals(expected, actual)
    }
}
