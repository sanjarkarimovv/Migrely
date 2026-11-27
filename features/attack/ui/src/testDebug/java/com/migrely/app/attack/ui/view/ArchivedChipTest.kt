package com.migrely.app.attack.ui.view

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isPopup
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import com.migrely.app.attack.ui.model.AttackDetailsUiModel
import com.migrely.app.attack.ui.model.AttackDetailsViewStateUiModel
import com.migrely.app.attack.ui.model.CustomTagUiModel
import com.migrely.app.attack.ui.model.SymptomUiModel
import com.migrely.app.attack.ui.model.TagKindUiModel
import com.migrely.app.attack.ui.model.TriggerUiModel
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val JAW_PAIN = "Jaw pain"
private const val PERFUME = "Perfume"
private const val REMOVE = "Remove from my list"
private const val REMOVE_LONG_PRESS = "remove from my list"
private const val FRAMES = 30

/** The preview's own symptom, selected on its attack, archived or not. */
private fun jawPain(archived: Boolean) =
    SymptomUiModel.Custom(CustomTagUiModel("j", TagKindUiModel.Symptom, JAW_PAIN, archived))

/** The preview attack with "Jaw pain" removed from the list, or brought back by Undo; it stays selected (EC-ATK-11). */
private fun detailsWithJawPain(archived: Boolean): AttackDetailsUiModel {
    val preview = previewDetails()
    val tag = jawPain(archived)
    val builtIn: (SymptomUiModel) -> Boolean = { it is SymptomUiModel.BuiltIn }
    return preview.copy(
        symptomChoices = preview.symptomChoices.filter(builtIn) + tag,
        selectedSymptoms = preview.selectedSymptoms.filter(builtIn).toSet() + tag
    )
}

/** A past attack opened later that uses a trigger removed from the list earlier: it shows, selected. */
private fun pastAttackWithArchivedPerfume(): AttackDetailsUiModel {
    val preview = previewDetails()
    val perfume = TriggerUiModel.Custom(
        CustomTagUiModel("p", TagKindUiModel.Trigger, PERFUME, archived = true)
    )
    return preview.copy(
        triggerChoices = preview.triggerChoices + perfume,
        selectedTriggers = preview.selectedTriggers + perfume
    )
}

/**
 * A custom tag removed from the list but still shown on an attack offers no remove until Undo brings it back: no
 * long-press menu, no long-press label and no "Remove from my list" action (BRD EC-ATK-28, D199 (3), design D7). Its
 * control: at 2d3ca53f the chip never reads `archived`, so all three are there.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class ArchivedChipTest {
    @get:Rule
    val rule = createComposeRule()

    private var details by mutableStateOf(detailsWithJawPain(archived = true))

    private fun show(shown: AttackDetailsUiModel) {
        details = shown
        rule.setContent {
            MigrelyTheme {
                AttackDetailsContents(
                    viewState = AttackDetailsViewStateUiModel.Ready(details),
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
        rule.waitForIdle()
    }

    private fun chip(name: String) = rule.onNode(hasContentDescription(name)).performScrollTo()

    private fun SemanticsNodeInteraction.longClickLabel() =
        fetchSemanticsNode().config.getOrNull(SemanticsActions.OnLongClick)?.label

    private fun SemanticsNodeInteraction.actionLabels() =
        fetchSemanticsNode().config.getOrNull(SemanticsActions.CustomActions)
            .orEmpty()
            .map { it.label }

    private fun SemanticsNodeInteraction.isSelected() =
        fetchSemanticsNode().config.getOrNull(SemanticsProperties.Selected) == true

    /** Whether a finger's long press on the chip opens its menu with "Remove from my list". */
    private fun SemanticsNodeInteraction.opensMenuOnLongPress(): Boolean {
        performTouchInput { longClick() }
        repeat(FRAMES) {
            rule.mainClock.advanceTimeByFrame()
            rule.waitForIdle()
        }
        return rule.onAllNodes(hasAnyAncestor(isPopup()).and(hasText(REMOVE)))
            .fetchSemanticsNodes()
            .isNotEmpty()
    }

    /** What a chip offers to remove it: its long-press label, its accessibility actions and a long press's menu. */
    private data class RemoveOffers(
        val longPressLabel: String?,
        val actions: List<String>,
        val menu: Boolean
    )

    /** The long press last: with no menu it is the chip's tap, as on a built-in chip. */
    private fun offers(name: String) = RemoveOffers(
        longPressLabel = chip(name).longClickLabel(),
        actions = chip(name).actionLabels(),
        menu = chip(name).opensMenuOnLongPress()
    )

    private val none = RemoveOffers(longPressLabel = null, actions = emptyList(), menu = false)

    @Test
    fun `A removed tag selected on this attack offers no remove`() {
        show(detailsWithJawPain(archived = true))

        assertTrue("shown selected", chip(JAW_PAIN).isSelected())
        assertEquals(none, offers(JAW_PAIN))
    }

    @Test
    fun `Undo brings the menu, its label and the action back`() {
        show(detailsWithJawPain(archived = true))

        details = detailsWithJawPain(archived = false)
        rule.waitForIdle()

        assertTrue("still selected", chip(JAW_PAIN).isSelected())
        assertEquals(RemoveOffers(REMOVE_LONG_PRESS, listOf(REMOVE), menu = true), offers(JAW_PAIN))
    }

    @Test
    fun `A past attack's removed tag offers no remove`() {
        show(pastAttackWithArchivedPerfume())

        assertTrue("shown selected", chip(PERFUME).isSelected())
        assertEquals(none, offers(PERFUME))
    }
}
