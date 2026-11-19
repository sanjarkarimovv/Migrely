package com.migrely.app.test.talkback

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

// How much of a stop shows at an edge in the sweep: 1 px, then up to 12 dp (design D7).
private val CUTS_DP = listOf(4, 8, 12)

/** A stop on the page: its semantics id, its top in the page's content and its height, in px. */
private class Stop(val id: Int, val words: String, val top: Int, val height: Int)

/**
 * The cut sweep (#255, design D7). For each item TalkBack stops on inside the page matched by [page], it scrolls so
 * that 1 px, then 4, 8 and 12 dp of the item show at the visible part's bottom edge, then at its top edge, and reads
 * the item's name as TalkBack builds it ([TalkBackTree.spokenName]). Returns each cut where the item is still a stop
 * but says less than when it is whole, as "words: N px at the bottom edge says '…'"; empty when every stop keeps its
 * name. Only the stops [of] matches (on the merged tree) are swept. TalkBack must be on ([turnOnTalkBack]) before
 * the screen is composed.
 */
fun <A : ComponentActivity> AndroidComposeTestRule<*, A>.cutSweep(
    page: SemanticsMatcher = hasScrollAction(),
    of: SemanticsMatcher = SemanticsMatcher("any stop") { true }
): List<String> {
    val sweep = Sweep(this, page)
    sweep.scrollTo(0)
    val cuts = listOf(1) + CUTS_DP.map { with(density) { it.dp.roundToPx() } }
    return sweep.stops(of).flatMap { stop -> sweep.cutNames(stop, cuts) }
}

private class Sweep<A : ComponentActivity>(
    private val rule: AndroidComposeTestRule<*, A>,
    private val page: SemanticsMatcher
) {
    private val pageNode get() = rule.onNode(page).fetchSemanticsNode()
    private val range get() = pageNode.config[SemanticsProperties.VerticalScrollAxisRange]
    private val offset get() = range.value().roundToInt()
    private val viewport get() = pageNode.boundsInRoot

    fun scrollTo(target: Int) {
        val delta = target - offset
        if (delta != 0) {
            rule.onNode(page).performSemanticsAction(SemanticsActions.ScrollBy) {
                it(0f, delta.toFloat())
            }
            rule.waitForIdle()
        }
    }

    /**
     * The page's stops, from the merged tree: each item that carries words, can be clicked or merges its children,
     * so a stop with no words at all is swept too.
     */
    fun stops(of: SemanticsMatcher): List<Stop> {
        val pageId = pageNode.id
        val top = viewport.top
        return rule.mergedNodes().values
            .filter { node -> generateSequence(node.parent) { it.parent }.any { it.id == pageId } }
            .filter { node -> node.isStop() && of.matches(node) }
            .map { node ->
                val words = node.mergedWords() ?: "a stop with no words (id ${node.id})"
                Stop(
                    node.id,
                    words,
                    (node.positionInRoot.y - top).roundToInt() + offset,
                    node.size.height
                )
            }
    }

    private fun SemanticsNode.isStop() = mergedWords() != null ||
        config.isMergingSemanticsOfDescendants ||
        SemanticsActions.OnClick in config

    fun cutNames(stop: Stop, cuts: List<Int>): List<String> {
        val height = viewport.height.roundToInt()
        val max = range.maxValue().roundToInt()
        scrollTo((stop.top - (height - stop.height) / 2).coerceIn(0, max))
        val whole = spokenName(stop.id) ?: return emptyList()
        if (whole.isBlank()) return listOf("${stop.words}: says nothing even when whole")
        return cuts.filter { it < stop.height }.flatMap { cut ->
            listOf("bottom" to stop.top - height + cut, "top" to stop.top + stop.height - cut)
                .filter { (_, target) -> target in 0..max }
                .mapNotNull { (edge, target) ->
                    scrollTo(target)
                    spokenName(stop.id)?.takeIf { it != whole }
                        ?.let { said -> "${stop.words}: $cut px at the $edge edge says '$said'" }
                }
        }
    }

    /**
     * The stop's name as TalkBack builds it, or null when it is not in the tree. A text field's name is its own: its
     * label (on its node) and its text. Its supporting line under it (a counter) is only extra, so it may go with the part
     * an edge cuts off, as anything out of view does; TalkBack does not speak a field's state description, so the line
     * stays a child (#255, device walk 6.1).
     */
    private fun spokenName(id: Int): String? {
        val node = rule.unmergedNodes()[id] ?: return null
        var name: String? = null
        rule.runOnUiThread {
            val tree = TalkBackTree(rule.activity)
            if (tree.node(id) != null) {
                val parts = tree.spokenParts(node)
                name = if (node.config.contains(SemanticsProperties.EditableText)) {
                    listOfNotNull(
                        node.config.getOrNull(
                            SemanticsProperties.ContentDescription
                        )?.joinToString(" "),
                        node.config[SemanticsProperties.EditableText].text
                    ).filter { it.isNotBlank() }.joinToString(" ")
                } else {
                    parts.joinToString(" ")
                }
            }
        }
        return name
    }
}
