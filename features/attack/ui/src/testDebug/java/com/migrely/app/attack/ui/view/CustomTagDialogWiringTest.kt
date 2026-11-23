package com.migrely.app.attack.ui.view

import android.app.Application
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.compose.ui.platform.ViewRootForTest
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.core.view.children
import com.migrely.app.attack.ui.model.TagKindUiModel
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowDialog

/**
 * The custom tag dialog itself, wired to its width rule (design D10 item 6). Robolectric's window is out of touch
 * mode, so the field takes focus on opening, as on a device out of touch mode: an intent to type. No on-screen keyboard
 * ever rises here. The clock is driven by hand: with a focused field the dialog never goes idle.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class, qualifiers = "w640dp-h360dp-land")
class CustomTagDialogWiringTest {
    @get:Rule
    val rule = createComposeRule()

    @Before
    fun setUp() {
        rule.mainClock.autoAdvance = false
    }

    private fun open(hardwareKeyboard: Boolean = false) {
        rule.setContent {
            MigrelyTheme {
                WithHardwareKeyboard(hardwareKeyboard) {
                    CustomTagDialog(TagKindUiModel.Symptom, onAdd = {}, onDismiss = {})
                }
            }
        }
        rule.mainClock.advanceTimeBy(SETTLED)
    }

    /** Wide: the window does not use the platform's default width, so Compose lays it out at MATCH_PARENT. */
    private fun wide(): Boolean = ShadowDialog.getLatestDialog().window!!.attributes.width ==
        WindowManager.LayoutParams.MATCH_PARENT

    @Test
    fun `Given the field takes focus on opening then the dialog is narrow`() {
        open()

        assertFalse(wide())
    }

    @Test
    fun `Given no on-screen keyboard comes then the dialog is wide again after the wait`() {
        open()

        rule.mainClock.advanceTimeBy(PAST_THE_WAIT)

        assertTrue(wide())
    }

    @Test
    fun `Given a hardware keyboard then the dialog stays wide when the field takes focus`() {
        open(hardwareKeyboard = true)

        assertTrue(wide())
    }

    /**
     * T23 8.1 F3: Material names its own pane "Dialog"; TalkBack announced whichever name appeared last. Read from the
     * dialog window's own semantics tree: the test rule's queries wait for an idle this dialog never reaches.
     */
    @Test
    fun `Given the dialog then its one pane is named by its title`() {
        open()

        val root = ShadowDialog.getLatestDialog().window!!.decorView.composeRoot()!!
        val panes = generateSequence(listOf(root.semanticsOwner.unmergedRootSemanticsNode)) { level ->
            level.flatMap { it.children }.takeIf { it.isNotEmpty() }
        }.flatten().mapNotNull { it.config.getOrNull(SemanticsProperties.PaneTitle) }.toList()
        assertEquals(listOf("Your own symptom"), panes)
    }

    private fun View.composeRoot(): ViewRootForTest? = this as? ViewRootForTest
        ?: (this as? ViewGroup)?.children?.firstNotNullOfOrNull { it.composeRoot() }

    private companion object {
        const val SETTLED = 500L
        const val PAST_THE_WAIT = 3_100L
    }
}
