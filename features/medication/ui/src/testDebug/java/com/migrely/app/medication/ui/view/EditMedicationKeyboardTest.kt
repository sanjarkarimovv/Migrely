package com.migrely.app.medication.ui.view

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.medication.ui.model.MedicationFormUiModel
import com.migrely.app.medication.ui.model.MedicationKindUiModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val SAVE = "Save"
private const val NAME = "Name"
private const val DOSE = "Usual dose (optional)"

// The input line's clearance from the keyboard's top (BRD D180 (2), ux U1).
private val MARGIN = 8.dp

// An outlined field with a label: its text starts 16 dp below its box's top, which lies half the floating label's
// line below the field's top (Material 3's minimizedLabelHalfHeight, bodySmall's 16 sp line at the font scale).
private val TEXT_PADDING = 16.dp
private const val LABEL_LINE_SP = 16f

/**
 * A focused medication field stays above the keyboard (BRD D180 (2), EC-UX-40): with a 250 dp keyboard, the input line of
 * the name or the dose shows 8 dp above it at 200% in landscape, the whole field with its supporting line at 100% in
 * portrait, and Save can be scrolled into the space above the keyboard.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class EditMedicationKeyboardTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private var form by mutableStateOf<MedicationFormUiModel?>(
        MedicationFormUiModel(name = "Ibuprofen", kind = MedicationKindUiModel.Acute)
    )
    private var fontScale = 1f

    private val name get() = rule.onNode(hasSetTextAction() and hasText(NAME))
    private val dose get() = rule.onNode(hasSetTextAction() and hasText(DOSE))

    private fun show(scale: Float) {
        fontScale = scale
        rule.runOnUiThread { rule.activity.drawEdgeToEdge() }
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, scale)) {
                MigrelyTheme {
                    EditMedicationContents(
                        adding = false,
                        notFound = false,
                        form = form,
                        problem = null,
                        pending = false,
                        onFormChange = { form = it },
                        onSave = {},
                        onBack = {},
                        messages = {}
                    )
                }
            }
        }
        rule.waitForIdle()
    }

    /** Focuses the field labelled [label] with a tap, as a person does, then opens the keyboard. */
    private fun type(label: String) {
        rule.onNode(hasScrollAction()).performScrollToNode(hasSetTextAction() and hasText(label))
        rule.onNode(hasSetTextAction() and hasText(label)).performClick()
        rule.waitForIdle()
        rule.runOnUiThread { rule.activity.setKeyboard(KEYBOARD_DP) }
        rule.waitForIdle()
    }

    private fun keyboardTop(): Float {
        val view = rule.activity.window.decorView
        return view.height / view.resources.displayMetrics.density - KEYBOARD_DP
    }

    /** The bottom of [field]'s input line, in dp in the root. */
    private fun inputLineBottom(field: SemanticsNodeInteraction): Float {
        val results = mutableListOf<TextLayoutResult>()
        field.fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action!!(results)
        val density = rule.activity.resources.displayMetrics.density
        val line = results.first().getLineBottom(0) / density
        val labelHalf = LABEL_LINE_SP * fontScale / 2
        return field.getUnclippedBoundsInRoot().top.value + labelHalf + TEXT_PADDING.value + line
    }

    private fun assertInputLineShows(field: SemanticsNodeInteraction) {
        val bottom = inputLineBottom(field)
        assertTrue(
            "input line ends at $bottom, keyboard at ${keyboardTop()}",
            bottom + MARGIN.value <= keyboardTop() + 0.5f
        )
    }

    private fun assertSaveCanBeReached() {
        rule.onNode(hasScrollAction()).performScrollToNode(hasText(SAVE))
        rule.waitForIdle()
        val save = rule.onNodeWithText(SAVE).getUnclippedBoundsInRoot()
        assertTrue(
            "Save ends at ${save.bottom}, keyboard at ${keyboardTop()}",
            save.bottom.value <= keyboardTop() + 0.5f
        )
    }

    @Test
    @Config(sdk = [35], qualifiers = "w891dp-h411dp")
    fun `At 200 percent in landscape the name's input line shows above the keyboard`() {
        show(2f)

        type(NAME)

        assertInputLineShows(name)
        assertSaveCanBeReached()
    }

    @Test
    @Config(sdk = [35], qualifiers = "w891dp-h411dp")
    fun `At 200 percent in landscape the dose's input line shows above the keyboard`() {
        show(2f)

        type(DOSE)

        assertInputLineShows(dose)
        assertSaveCanBeReached()
    }

    @Test
    @Config(sdk = [35], qualifiers = "w411dp-h891dp")
    fun `At 100 percent in portrait the dose shows whole with its supporting line`() {
        show(1f)

        type(DOSE)

        val field = dose.getUnclippedBoundsInRoot()
        assertTrue(
            "dose ends at ${field.bottom}, keyboard at ${keyboardTop()}",
            field.bottom.value <= keyboardTop() + 0.5f
        )
        assertSaveCanBeReached()
    }

    @Test
    @Config(sdk = [35], qualifiers = "w411dp-h891dp")
    fun `Closing the keyboard after scrolling to Save does not scroll back to the name`() {
        show(2f)
        type(NAME)
        rule.onNode(hasScrollAction()).performScrollToNode(hasText(SAVE))
        rule.waitForIdle()
        val scrolled = scroll()
        assertTrue("Save was already in view", scrolled > 0f)

        rule.runOnUiThread { rule.activity.setKeyboard(0) }
        rule.waitForIdle()

        // Save ends the page, so the larger area may only settle it down to the page's new end (ux U6).
        assertEquals("the page scrolled back", minOf(scrolled, range().maxValue()), scroll(), 0.5f)
    }

    @Test
    @Config(sdk = [35], qualifiers = "w411dp-h891dp")
    fun `Closing the keyboard near the page's end leaves the dose where it is`() {
        show(1f)
        type(DOSE)
        rule.onNode(hasScrollAction()).performScrollToNode(hasText(SAVE))
        rule.waitForIdle()
        assertTrue("the page fits above the keyboard", scroll() > 0f)
        val before = dose.getUnclippedBoundsInRoot().top

        rule.runOnUiThread { rule.activity.setKeyboard(0) }
        rule.waitForIdle()

        // The page keeps its scroll position (BRD D189, #238): a pulled page is a scroll TalkBack follows.
        assertEquals("the dose moved", before.value, dose.getUnclippedBoundsInRoot().top.value, 0.5f)
    }

    private fun range() = rule.onNode(hasScrollAction()).fetchSemanticsNode()
        .config[SemanticsProperties.VerticalScrollAxisRange]

    private fun scroll(): Float = range().value()
}
