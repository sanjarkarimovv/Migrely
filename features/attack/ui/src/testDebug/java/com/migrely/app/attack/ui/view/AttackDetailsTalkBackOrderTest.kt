package com.migrely.app.attack.ui.view

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.unit.Density
import com.migrely.app.attack.ui.model.AttackDetailsViewStateUiModel
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

// A keyboard short enough for a landscape phone at 200 percent to keep some page above it.
private const val LANDSCAPE_KEYBOARD_DP = 150

// Fewer items than this in TalkBack's order means it did not reach the page: the top bar alone has three.
private const val MINIMUM_STOPS = 4

// How far each check scrolls the page, in px: a few steps per item, so items cross each edge.
private const val STEP = 25f

/**
 * TalkBack's order on Attack details runs down the page at every scroll, each item once (#255, BRD D178). With the
 * keyboard up, the page's visible part ends above the window's bottom. Compose then still lists an item lying within
 * half a 48 dp touch target below that end (under the keyboard), and sorts it first in the page because its visible
 * bounds are empty: TalkBack's "previous" from the first item went down to it, and the walk circled.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AttackDetailsTalkBackOrderTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private fun show(fontScale: Float, keyboardDp: Int) {
        rule.runOnUiThread {
            rule.activity.drawEdgeToEdge()
            rule.activity.turnOnTalkBack()
        }
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
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
        }
        rule.runOnUiThread { rule.activity.setKeyboard(keyboardDp) }
        rule.waitForIdle()
    }

    /** The page's reading order: the semantics tree depth first, which follows this page from the top down. */
    private fun readingOrder(): Map<Int, Int> {
        val order = mutableListOf<Int>()
        fun visit(node: SemanticsNode) {
            order += node.id
            node.children.forEach(::visit)
        }
        visit(rule.onRoot().fetchSemanticsNode())
        return order.withIndex().associate { (index, id) -> id to index }
    }

    /** Where TalkBack's order goes back up the page, or meets an item twice, at the current scroll. */
    private fun wrongSteps(): List<String> {
        val nodes = rule.mergedNodes()
        val reading = readingOrder()
        var stops: List<Int> = emptyList()
        rule.runOnUiThread {
            stops =
                TalkBackTree(rule.activity).order(reading.keys).filter { id ->
                    nodes[id]?.talkBackName() !=
                        null
                }
        }
        fun name(id: Int) = nodes.getValue(id).talkBackName()
        val upward = stops.zipWithNext()
            .filter { (from, to) -> reading.getValue(to) < reading.getValue(from) }
            .map { (from, to) -> "${name(from)} -> ${name(to)}" }
        val twice = stops.groupBy {
            it
        }.filterValues { it.size > 1 }.keys.map { "twice: ${name(it)}" }
        // A check that can fail: the order must reach the screen, not stop at the top bar.
        val tooShort = if (stops.size <
            MINIMUM_STOPS
        ) {
            listOf("only ${stops.size} items in the order")
        } else {
            emptyList()
        }
        return upward + twice + tooShort
    }

    /** The wrong steps at each scroll position in px, from the page's top to its end. */
    private fun wrongStepsDownThePage(): Map<Int, List<String>> {
        val page = rule.onNode(hasScrollAction())
        fun position() =
            page.fetchSemanticsNode().config[SemanticsProperties.VerticalScrollAxisRange].value()
        val found = mutableMapOf<Int, List<String>>()
        while (true) {
            val before = position()
            wrongSteps().takeIf { it.isNotEmpty() }?.let { found[before.toInt()] = it }
            page.performSemanticsAction(SemanticsActions.ScrollBy) { it(0f, STEP) }
            rule.waitForIdle()
            if (position() == before) return found
        }
    }

    @Test
    @Config(sdk = [35], qualifiers = "w411dp-h891dp")
    fun `Portrait at 100 percent, keyboard up, TalkBack's order runs down the page`() {
        show(fontScale = 1f, keyboardDp = KEYBOARD_DP)
        assertEquals(emptyMap<Int, List<String>>(), wrongStepsDownThePage())
    }

    @Test
    @Config(sdk = [35], qualifiers = "w800dp-h360dp")
    fun `Landscape 360 dp at 200 percent, keyboard up, the order runs down the page`() {
        show(fontScale = 2f, keyboardDp = LANDSCAPE_KEYBOARD_DP)
        assertEquals(emptyMap<Int, List<String>>(), wrongStepsDownThePage())
    }

    @Test
    @Config(sdk = [35], qualifiers = "w411dp-h891dp")
    fun `Portrait at 100 percent, no keyboard, TalkBack's order runs down the page`() {
        show(fontScale = 1f, keyboardDp = 0)
        assertEquals(emptyMap<Int, List<String>>(), wrongStepsDownThePage())
    }
}
