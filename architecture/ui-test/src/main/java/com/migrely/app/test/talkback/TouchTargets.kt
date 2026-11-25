package com.migrely.app.test.talkback

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.unit.Density
import java.util.Locale
import kotlin.math.roundToInt

/** A vertically scrolling page or dialog content, whether or not its content overflows now. */
private val aPage = SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)

/** A clickable control and the size of its own touch area, in dp, with no widening (#255, design D5). */
data class TouchTarget(val words: String, val widthDp: Float, val heightDp: Float) {
    override fun toString() =
        "$words ${"%.1f".format(Locale.ROOT, widthDp)}x${"%.1f".format(Locale.ROOT, heightDp)} dp"
}

/**
 * Every clickable control inside the page matched by [page], with the size of the node that takes its click. Inside
 * `PageScroll` this is the whole touch area: Compose no longer widens it to 48 dp. Material keeps a 48 dp layout
 * around a smaller control, but the click lands only on the control's own node.
 */
fun ComposeTestRule.touchTargets(page: SemanticsMatcher = aPage): List<TouchTarget> =
    onAllNodes(hasClickAction() and hasAnyAncestor(page)).fetchSemanticsNodes().map { node ->
        with(density) {
            TouchTarget(
                words = node.mergedWords() ?: "(no words, id ${node.id})",
                widthDp = node.size.width.toDp().value,
                heightDp = node.size.height.toDp().value
            )
        }
    }

/** Material's minimum interactive size, the bar of every control inside a page unless a screen sets a larger one. */
const val TOUCH_BAR_DP = 48f

private const val PERCENT = 100

/**
 * The touch-target audit (#255, design D5; lead decision 2026-11-19, option 1). Shows each of [screens] in turn at
 * [fontScale] and returns, as "screen: words WxH dp", each control inside a page smaller than its [bar]. Prints every
 * screen's controls and its smallest one for the evidence table, as "AUDIT screen | width font | count | smallest |
 * all".
 */
fun <A : ComponentActivity> AndroidComposeTestRule<*, A>.auditTouchTargets(
    fontScale: Float,
    screens: Map<String, @Composable () -> Unit>,
    bar: (TouchTarget) -> Float = { TOUCH_BAR_DP }
): List<String> {
    var current by mutableStateOf(screens.keys.first())
    setContent {
        val density = LocalDensity.current
        CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
            key(current) { screens.getValue(current)() }
        }
    }
    return screens.keys.flatMap { name ->
        runOnIdle { current = name }
        waitForIdle()
        val all = touchTargets()
        val smallest = all.minByOrNull { minOf(it.widthDp, it.heightDp) }
        val width = activity.resources.configuration.screenWidthDp
        val size = "${width}dp ${(fontScale * PERCENT).roundToInt()}%"
        println(
            "AUDIT $name | $size | ${all.size} | ${smallest ?: "none"} | ${all.joinToString("; ")}"
        )
        all.filter { it.widthDp < bar(it) || it.heightDp < bar(it) }.map { "$name: $it" }
    }
}
