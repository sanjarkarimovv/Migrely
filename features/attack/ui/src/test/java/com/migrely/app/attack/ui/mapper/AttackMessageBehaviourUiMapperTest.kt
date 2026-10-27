package com.migrely.app.attack.ui.mapper

import com.migrely.app.attack.ui.model.AttackMessageBehaviourUiModel
import com.migrely.app.attack.ui.model.AttackMessageUiModel
import com.migrely.app.attack.ui.model.IntensityPointUiModel
import com.migrely.app.attack.ui.model.TimesUndoUiModel
import com.migrely.app.designsystem.snackbar.SnackbarTone
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class AttackMessageBehaviourUiMapperTest(
    private val message: AttackMessageUiModel,
    private val expected: AttackMessageBehaviourUiModel
) {
    companion object {
        private val undo = AttackMessageBehaviourUiModel(SnackbarTone.Information, true, 5_000L)
        private val information = AttackMessageBehaviourUiModel(
            SnackbarTone.Information,
            false,
            8_000L
        )

        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun data(): Collection<Array<*>> = listOf(
            arrayOf<Any>(
                AttackMessageUiModel.PointsRemovedByTimeChange(
                    TimesUndoUiModel("a", 1L, 2L, emptyList())
                ),
                undo
            ),
            arrayOf<Any>(
                AttackMessageUiModel.PointRemoved("a", IntensityPointUiModel(1L, 6)),
                undo
            ),
            arrayOf<Any>(AttackMessageUiModel.PointOutsideAttack, information),
            arrayOf<Any>(AttackMessageUiModel.TagLimitReached, information),
            arrayOf<Any>(AttackMessageUiModel.Undone, information),
            arrayOf<Any>(AttackMessageUiModel.UndoNotPossible, information),
            arrayOf<Any>(
                AttackMessageUiModel.AttacksMerged,
                AttackMessageBehaviourUiModel(SnackbarTone.Confirmation, false, 8_000L)
            ),
            arrayOf<Any>(
                AttackMessageUiModel.SomethingWentWrong,
                AttackMessageBehaviourUiModel(SnackbarTone.Error, false, 8_000L)
            ),
            arrayOf<Any>(
                AttackMessageUiModel.StorageFull,
                AttackMessageBehaviourUiModel(SnackbarTone.Error, false, null)
            )
        )
    }

    @Test
    fun `When mapping a message then returns its tone, undo and duration`() {
        assertEquals(expected, AttackMessageBehaviourUiMapper().toUi(message))
    }
}
