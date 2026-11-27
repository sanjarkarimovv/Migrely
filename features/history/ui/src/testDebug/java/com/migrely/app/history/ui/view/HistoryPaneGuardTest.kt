package com.migrely.app.history.ui.view

import android.app.Application
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.toSize
import com.migrely.app.designsystem.layout.ShownTextLayoutsKey
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.history.ui.model.HistoryViewStateUiModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val MONTH_TITLE = "October 2026"
private const val LIST_TITLE = "No migraines logged in October"

/**
 * In a short window History puts the calendar and the list side by side only when each pane is at least 200 dp wide,
 * that is a window of 456 dp or more (#275, BRD D198 (4), EC-HIS-06). Split screen in portrait, 360 dp wide and under
 * 480 dp tall, used to get two panes of about 152 dp, where two-digit days wrapped even at 100%.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HistoryPaneGuardTest {
    @get:Rule
    val rule = createComposeRule()

    private fun show() {
        rule.setContent {
            MigrelyTheme {
                HistoryContents(
                    viewState = HistoryViewStateUiModel.Ready(previewMonth(), null),
                    selectedEpochDay = null,
                    mappers = previewMappers,
                    actions = previewActions,
                    messages = {},
                    overuseIndicator = {}
                )
            }
        }
        rule.waitForIdle()
    }

    /** Unclipped: in one column the list may start below the window. */
    private fun bounds(text: String): Rect = rule.onNodeWithText(text).fetchSemanticsNode().let {
        Rect(it.positionInRoot, it.size.toSize())
    }

    /** The list sits beside the calendar, not under it. */
    private fun twoPanes(): Boolean = bounds(LIST_TITLE).left >= bounds(MONTH_TITLE).right

    private fun dayNumbers(): List<TextLayoutResult> =
        rule.onAllNodes(SemanticsMatcher.keyIsDefined(ShownTextLayoutsKey), useUnmergedTree = true)
            .fetchSemanticsNodes()
            .flatMap { it.config[ShownTextLayoutsKey].all }
            .filter { layout -> layout.layoutInput.text.text.all(Char::isDigit) }

    private fun assertEveryDayNumberWhole() {
        val numbers = dayNumbers()
        assertEquals(31, numbers.size)
        val broken = numbers.filter { it.lineCount != 1 || it.hasVisualOverflow }
            .map { "${it.layoutInput.text.text} (${it.lineCount} lines)" }
        assertEquals("day numbers that wrap or are cut", emptyList<String>(), broken)
    }

    @Test
    @Config(sdk = [35], application = Application::class, qualifiers = "en-w360dp-h380dp-port")
    fun `Split screen in portrait, 360 x 380 dp at 100 percent - one column, whole numbers`() {
        show()

        assertFalse("two panes", twoPanes())
        assertTrue(
            "the list under the calendar",
            bounds(LIST_TITLE).top > bounds(MONTH_TITLE).bottom
        )
        assertEveryDayNumberWhole()
    }

    @Test
    @Config(sdk = [35], application = Application::class, qualifiers = "en-w455dp-h360dp-land")
    fun `A window 455 dp wide, panes of 199,5 dp - one column`() {
        show()

        assertFalse("two panes", twoPanes())
    }

    @Test
    @Config(sdk = [35], application = Application::class, qualifiers = "en-w456dp-h360dp-land")
    fun `A window 456 dp wide, panes of 200 dp - two panes`() {
        show()

        assertTrue("two panes", twoPanes())
        assertEveryDayNumberWhole()
    }

    @Test
    @Config(sdk = [35], application = Application::class, qualifiers = "en-w640dp-h360dp-land")
    fun `A phone in landscape, 640 x 360 dp - two panes as before`() {
        show()

        assertTrue("two panes", twoPanes())
    }

    @Test
    @Config(sdk = [35], application = Application::class, qualifiers = "en-w800dp-h360dp-land")
    fun `A phone in landscape, 800 x 360 dp - two panes as before`() {
        show()

        assertTrue("two panes", twoPanes())
    }

    @Test
    @Config(sdk = [35], application = Application::class, qualifiers = "en-w360dp-h640dp-port")
    fun `Portrait 360 x 640 dp - one column as before (negative control)`() {
        show()

        assertFalse("two panes", twoPanes())
        assertEveryDayNumberWhole()
    }
}
