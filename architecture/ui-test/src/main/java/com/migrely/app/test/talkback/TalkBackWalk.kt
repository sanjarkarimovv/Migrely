package com.migrely.app.test.talkback

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.unit.Density
import androidx.core.view.WindowCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat
import java.io.File
import java.nio.ByteBuffer
import kotlin.math.roundToInt

// How far the order check scrolls between looks, in px: a few looks per item, so items cross each edge.
private const val ORDER_STEP_PX = 25f

// Past either end of any page: the scroll stops there.
private const val FAR = 100_000f

// A gesture navigation bar: the page's visible part ends above the window, as on a phone (walk-b1).
private const val NAVIGATION_BAR_DP = 24

private const val PERCENT = 100

// More steps than any page needs between two items: a check that cannot loop for ever.
private const val MAX_STEPS_PER_SWIPE = 20

/**
 * TalkBack's walk through each of [screens] at [fontScale] (#255, design D7, tasks 4.x), in a window drawn edge to
 * edge with a 24 dp navigation bar. For each vertical scroll area on a screen it checks, as TalkBack goes:
 * - the order, at every [ORDER_STEP_PX] down the area: no step goes back up the page, no stop comes twice;
 * - swiping back from the end and on from the top: each step keeps the item TalkBack was on in view, or, where the
 *   gap to the next item is half the visible part or more, at least reaches that next item;
 * - the cut sweep ([cutSweep]): a stop cut by an edge says what it says whole;
 * - no stop says its words twice, as its own description and again as its Text (BRD D181 (2)).
 * Returns each problem as "screen: …"; empty when TalkBack walks every screen. Writes each screen's window pixels at
 * the top of its page to `build/screen-pixels/<screen> <width>dp <font>%.raw`, for the before-and-after comparison.
 * TalkBack is turned on here, so call it before anything is composed.
 */
fun <A : ComponentActivity> AndroidComposeTestRule<*, A>.talkBackWalk(
    fontScale: Float,
    screens: Map<String, @Composable () -> Unit>
): List<String> {
    runOnUiThread {
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)
        activity.turnOnTalkBack()
    }
    var current by mutableStateOf(screens.keys.first())
    setContent {
        val density = LocalDensity.current
        CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
            key(current) { screens.getValue(current)() }
        }
    }
    runOnUiThread { activity.setNavigationBar(NAVIGATION_BAR_DP) }
    val width = activity.resources.configuration.screenWidthDp
    val size = "${width}dp ${(fontScale * PERCENT).roundToInt()}%"
    return screens.keys.flatMap { name ->
        runOnIdle { current = name }
        waitForIdle()
        dumpPixels("$name $size")
        val walk = Walk(this)
        val areas = walk.scrollAreas()
        val problems = walk.saidTwice() + if (areas.isEmpty()) {
            walk.wrongSteps()
        } else {
            areas.flatMap { area ->
                walk.orderDown(area) + walk.lost(area, backward = true) +
                    walk.lost(area, backward = false) +
                    cutSweep(page = area)
            }
        }
        problems.map { "$name: $it" }
    }
}

private fun <A : ComponentActivity> AndroidComposeTestRule<*, A>.dumpPixels(name: String) {
    val decor = activity.window.decorView
    val bitmap = Bitmap.createBitmap(decor.width, decor.height, Bitmap.Config.ARGB_8888)
    runOnUiThread { decor.draw(Canvas(bitmap)) }
    val pixels = ByteBuffer.allocate(bitmap.byteCount)
    bitmap.copyPixelsToBuffer(pixels)
    File("build/screen-pixels").apply { mkdirs() }.resolve("$name.raw").writeBytes(pixels.array())
}

private class Walk<A : ComponentActivity>(private val rule: AndroidComposeTestRule<*, A>) {
    private val tree get() = TalkBackTree(rule.activity)

    /** Each vertical scroll area that can scroll now, by its semantics id. */
    fun scrollAreas(): List<SemanticsMatcher> = rule.mergedNodes().values
        .filter { node ->
            node.config.getOrNull(
                SemanticsProperties.VerticalScrollAxisRange
            )?.maxValue?.invoke()?.let {
                it >
                    0f
            } ==
                true
        }
        .map { area -> SemanticsMatcher("scroll area ${area.id}") { it.id == area.id } }

    private fun <T> onUi(block: () -> T): T {
        var result: T? = null
        rule.runOnUiThread { result = block() }
        @Suppress("UNCHECKED_CAST")
        return result as T
    }

    /**
     * The stops that carry their words twice, as a description and again as text (BRD D181 (2)): a control given its
     * words on its own node must leave its Text unread.
     */
    fun saidTwice(): List<String> {
        val unmerged = rule.unmergedNodes()
        return rule.mergedNodes().values.filter { it.mergedWords() != null }.mapNotNull { stop ->
            val node = unmerged[stop.id] ?: return@mapNotNull null
            val parts = onUi {
                if (tree.node(stop.id) !=
                    null
                ) {
                    tree.spokenParts(node)
                } else {
                    emptyList()
                }
            }
            val repeated = parts.withIndex().any { (index, part) ->
                parts.withIndex().any { (other, words) -> other != index && part in words }
            }
            if (repeated) "said twice: ${parts.joinToString(" | ")}" else null
        }
    }

    /** The screen's reading order: the semantics tree depth first, which follows each page from the top down. */
    private fun readingOrder(): Map<Int, Int> {
        val order = mutableListOf<Int>()
        fun visit(node: SemanticsNode) {
            order += node.id
            // As Compose orders siblings for TalkBack: a set traversal index first moves an item within its parent.
            node.children
                .sortedBy { it.config.getOrNull(SemanticsProperties.TraversalIndex) ?: 0f }
                .forEach(::visit)
        }
        visit(rule.onRoot().fetchSemanticsNode())
        return order.withIndex().associate { (index, id) -> id to index }
    }

    /** Where TalkBack's order goes back up the page, or meets an item twice, now. */
    fun wrongSteps(): List<String> {
        val nodes = rule.mergedNodes()
        val reading = readingOrder()
        val stops = onUi { tree.order(reading.keys) }.filter { id ->
            nodes[id]?.mergedWords() !=
                null
        }
        fun name(id: Int) = nodes.getValue(id).mergedWords()

        // Back up the page both in the reading order and on screen: a later item laid out above an earlier one (a
        // card's Close at its top corner) is TalkBack's own geometric order, not a wrong step.
        fun top(id: Int) = nodes.getValue(id).positionInRoot.y
        val upward = stops.zipWithNext()
            .filter { (from, to) ->
                reading.getValue(to) < reading.getValue(from) &&
                    top(to) < top(from)
            }
            .map { (from, to) -> "${name(from)} -> ${name(to)}" }
        val twice = stops.groupBy {
            it
        }.filterValues { it.size > 1 }.keys.map { "twice: ${name(it)}" }
        // A check that can fail: the order must reach the screen.
        val tooShort = if (stops.isEmpty()) listOf("no items in the order") else emptyList()
        return upward + twice + tooShort
    }

    private fun position(area: SemanticsMatcher) = rule.onNode(
        area
    ).fetchSemanticsNode().config[SemanticsProperties.VerticalScrollAxisRange].value()

    private fun scrollBy(area: SemanticsMatcher, px: Float) {
        rule.onNode(area).performSemanticsAction(SemanticsActions.ScrollBy) { it(0f, px) }
        rule.waitForIdle()
    }

    /** The wrong steps at each look from the area's top to its end, as "order at N px: …". */
    fun orderDown(area: SemanticsMatcher): List<String> {
        scrollBy(area, -FAR)
        val found = mutableListOf<String>()
        while (true) {
            val before = position(area)
            wrongSteps().takeIf { it.isNotEmpty() }?.let {
                found +=
                    "order at ${before.toInt()} px: $it"
            }
            scrollBy(area, ORDER_STEP_PX)
            if (position(area) == before) return found
        }
    }

    /** The area's items TalkBack stops on, from the top down. */
    private fun stops(area: SemanticsMatcher): List<SemanticsNode> {
        val areaId = rule.onNode(area).fetchSemanticsNode().id
        return rule.mergedNodes().values
            .filter { node -> node.mergedWords() != null }
            .filter { node -> generateSequence(node.parent) { it.parent }.any { it.id == areaId } }
            .sortedWith(compareBy({ it.positionInRoot.y }, { it.positionInRoot.x }))
    }

    private fun visiblePart(area: SemanticsMatcher) =
        rule.onNode(area).fetchSemanticsNode().boundsInRoot

    private fun SemanticsNode.wholeIn(area: SemanticsMatcher): Boolean {
        val view = visiblePart(area)
        return positionInRoot.y >= view.top && positionInRoot.y + size.height <= view.bottom
    }

    private fun onScreen(id: Int) = onUi { tree.node(id)?.isVisibleToUser == true }

    private fun perform(id: Int, action: Int) {
        onUi { tree.perform(id, action) }
        rule.waitForIdle()
    }

    /**
     * TalkBack's swipes through the area from one end, moving as TalkBack does: to the next item; an item partly on
     * screen is shown whole first (ACTION_SHOW_ON_SCREEN); when the next item is not on screen the area scrolls one step
     * (ACTION_SCROLL_BACKWARD or FORWARD). Returns what the steps lost. Where the gap to the next item is half the visible
     * part or more, the item TalkBack was on may leave the screen (design D3): only the next item must be reached.
     */
    fun lost(area: SemanticsMatcher, backward: Boolean): List<String> {
        scrollBy(area, if (backward) FAR else -FAR)
        val stops = stops(area)
        val inView = stops.filter { it.wholeIn(area) }
        if (inView.isEmpty()) {
            return listOf(
                "nothing whole in view at the ${if (backward) "end" else "top"}"
            )
        }
        var index = stops.indexOf(if (backward) inView.last() else inView.first())
        val direction = if (backward) -1 else 1
        val step = if (backward) {
            AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD
        } else {
            AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD
        }
        val areaId = rule.onNode(area).fetchSemanticsNode().id
        val lost = mutableListOf<String>()
        while (true) {
            val pivot = stops[index]
            val target = stops.getOrNull(index + direction) ?: break
            if (!onScreen(target.id)) {
                val gap = if (backward) {
                    pivot.positionInRoot.y - (target.positionInRoot.y + target.size.height)
                } else {
                    target.positionInRoot.y - (pivot.positionInRoot.y + pivot.size.height)
                }
                val largeGap = gap >= visiblePart(area).height / 2
                perform(areaId, step)
                if (!largeGap &&
                    !onScreen(pivot.id)
                ) {
                    lost += "${pivot.mergedWords()} left the screen"
                }
                // Where the next item is not in view yet (a large gap, or an item taller than the visible part),
                // TalkBack scrolls again in the same swipe until it shows (design D3).
                var steps = 1
                while (!onScreen(target.id) && steps < MAX_STEPS_PER_SWIPE) {
                    perform(areaId, step)
                    steps++
                }
                if (!onScreen(target.id)) {
                    lost += "${target.mergedWords()} not on screen after $steps steps"
                    break
                }
            }
            val now = rule.mergedNodes()[target.id]
            if (now != null && !now.wholeIn(area)) {
                perform(target.id, AccessibilityActionCompat.ACTION_SHOW_ON_SCREEN.id)
            }
            index += direction
        }
        val end = if (backward) 0 else stops.lastIndex
        if (index != end) lost += "stopped at ${stops[index].mergedWords()}"
        return lost.map { "swiping ${if (backward) "back" else "on"}: $it" }
    }
}
