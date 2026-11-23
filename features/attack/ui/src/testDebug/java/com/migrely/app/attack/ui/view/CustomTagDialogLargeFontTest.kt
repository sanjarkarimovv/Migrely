package com.migrely.app.attack.ui.view

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val LARGEST_FONT = 2f

// What the custom tag dialog leaves for its title and field on the API 36 emulator at 200% in landscape, with German's
// stacked buttons (T23 8.1 row R03): the 48 dp text area plus the 90 dp the two-line title took, at 480 dpi.
private val SQUEEZED_TEXT_AREA = 138.dp

// The dialog's content width there: 816 px at 480 dpi.
private val DIALOG_CONTENT_WIDTH = 272.dp

// With the wide dialog in that window: Material's 560 dp less its 24 dp padding on each side, and the 116 dp text area
// plus the 48 dp the second button row gave back.
private val WIDE_CONTENT_WIDTH = 512.dp
private val WIDE_TEXT_AREA = 164.dp

/**
 * The custom symptom or trigger dialog keeps its Name field reachable when its buttons stack and leave the text area
 * little room (T23 8.1 row R03): the area scrolls, and the field with its counter can be brought into view whole.
 * The text area is tested on its own, clipped to that height: a dialog window under Robolectric never settles here.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class, qualifiers = "w640dp-h326dp-land-xxhdpi")
class CustomTagDialogLargeFontTest {
    @get:Rule
    val rule = createComposeRule()

    // The system's font scale, so text grows as on the device: non-linearly at 200% from API 34.
    private fun showSqueezedTextArea(
        title: String,
        width: Dp = DIALOG_CONTENT_WIDTH,
        height: Dp = SQUEEZED_TEXT_AREA
    ) {
        RuntimeEnvironment.setFontScale(LARGEST_FONT)
        rule.setContent {
            MigrelyTheme {
                Box(Modifier.size(width, height).clipToBounds()) {
                    CustomTagNameArea(title, TextFieldValue()) {}
                }
            }
        }
    }

    /**
     * The field and its counter can be brought on screen and then show whole, and the field keeps at least Material's
     * height: a layout that only squeezes it into the room left would also "show" it whole.
     */
    private fun assertFieldReachable(counter: String) {
        val field = rule.onNode(hasSetTextAction()).fetchSemanticsNode()
        val minimum = with(rule.density) { OutlinedTextFieldDefaults.MinHeight.toPx() }
        assertTrue(
            "the field is squeezed to ${field.size.height} px, under $minimum px",
            field.size.height >= minimum - 1f
        )
        listOf(hasSetTextAction(), hasText(counter)).forEach { matcher ->
            val node = rule.onNode(matcher)
            if (hasScrollingAncestor(node.fetchSemanticsNode())) node.performScrollTo()
            val shown = node.fetchSemanticsNode()
            assertTrue(
                "${matcher.description} shows ${shown.boundsInRoot.height} of ${shown.size.height} px",
                shown.size.height > 0 && shown.boundsInRoot.height >= shown.size.height - 1f
            )
        }
    }

    /** On opening, with no scroll, the field and its floating label show whole; the title can be scrolled to. */
    private fun assertFieldInViewOnOpening(title: String) {
        rule.waitForIdle()
        val field = rule.onNode(hasSetTextAction()).fetchSemanticsNode()
        assertTrue(
            "on opening the field shows ${field.boundsInRoot.height} of ${field.size.height} px",
            field.size.height > 0 && field.boundsInRoot.height >= field.size.height - 1f
        )
        assertTrue(
            "the title is a heading for TalkBack",
            rule.onAllNodes(hasText(title) and SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
                .fetchSemanticsNodes().size == 1
        )
    }

    private fun hasScrollingAncestor(node: SemanticsNode): Boolean =
        generateSequence(node.parent) { it.parent }
            .any { SemanticsActions.ScrollBy in it.config }

    @Test
    @Config(qualifiers = "+de-rDE")
    fun `Given German at 200 percent then a squeezed text area still reaches the name field`() {
        showSqueezedTextArea("Eigenes Symptom")

        assertFieldReachable("0/40")
    }

    @Test
    @Config(qualifiers = "+de-rDE")
    fun `Given German at 200 percent then a squeezed text area opens on the field`() {
        showSqueezedTextArea("Eigenes Symptom")

        assertFieldInViewOnOpening("Eigenes Symptom")
    }

    @Test
    @Config(qualifiers = "+de-rDE")
    fun `Given German at 200 percent in the wide dialog then the field and counter show at once`() {
        showSqueezedTextArea("Eigenes Symptom", WIDE_CONTENT_WIDTH, WIDE_TEXT_AREA)

        rule.waitForIdle()
        listOf(hasSetTextAction(), hasText("0/40")).forEach { matcher ->
            val shown = rule.onNode(matcher).fetchSemanticsNode()
            assertTrue(
                "${matcher.description} shows ${shown.boundsInRoot.height} of ${shown.size.height} px",
                shown.size.height > 0 && shown.boundsInRoot.height >= shown.size.height - 1f
            )
        }
    }

    @Test
    @Config(qualifiers = "+en-rUS")
    fun `Given English at 200 percent then a squeezed text area opens on the field`() {
        showSqueezedTextArea("Your own symptom")

        assertFieldInViewOnOpening("Your own symptom")
    }

    @Test
    @Config(qualifiers = "+en-rUS")
    fun `Given English at 200 percent then a squeezed text area still reaches the name field`() {
        showSqueezedTextArea("Your own symptom")

        assertFieldReachable("0/40")
    }
}
