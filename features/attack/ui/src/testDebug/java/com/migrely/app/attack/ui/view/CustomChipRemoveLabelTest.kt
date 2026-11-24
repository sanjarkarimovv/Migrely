package com.migrely.app.attack.ui.view

import android.app.Application
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performScrollTo
import com.migrely.app.attack.ui.model.AttackDetailsViewStateUiModel
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// The user's own symptom in the preview details.
private const val CUSTOM_CHIP = "Jaw pain"

/**
 * A custom chip's long press has an action label of its own: TalkBack says "Double-tap and hold to
 * %s", in German "Zum %s doppeltippen und halten" (T23, ux T72 verdict C). The actions menu shows
 * its label alone, so it keeps the verb phrase. Written out here and not read from `R.string`, so
 * a drift fails.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class CustomChipRemoveLabelTest {
    @get:Rule
    val rule = createComposeRule()

    private fun customChip(): SemanticsNode {
        rule.setContent {
            MigrelyTheme {
                AttackDetailsContents(
                    viewState = AttackDetailsViewStateUiModel.Ready(previewDetails()),
                    timeUiFormatter = previewTimeFormatter,
                    labels = AttackLabels.Default,
                    localState = AttackDetailsLocalState(
                        timeError = null,
                        safetyCardVisible = false,
                        onDismissSafetyCard = {},
                        onTimeErrorShown = {}
                    ),
                    actions = previewActions
                )
            }
        }
        return rule.onNode(hasContentDescription(CUSTOM_CHIP))
            .performScrollTo()
            .fetchSemanticsNode()
    }

    private fun SemanticsNode.longClickLabel() = config[SemanticsActions.OnLongClick].label

    private fun SemanticsNode.menuLabels() =
        config[SemanticsActions.CustomActions].map { it.label }

    @Test
    @Config(qualifiers = "de")
    fun `Given German then the long press names the action, and the menu keeps the verb phrase`() {
        val chip = customChip()

        assertEquals("Entfernen aus meiner Liste", chip.longClickLabel())
        assertEquals(listOf("Aus meiner Liste entfernen"), chip.menuLabels())
    }

    @Test
    fun `Given English then the long press says what it said before`() {
        val chip = customChip()

        assertEquals("remove from my list", chip.longClickLabel())
        assertEquals(listOf("Remove from my list"), chip.menuLabels())
    }
}
