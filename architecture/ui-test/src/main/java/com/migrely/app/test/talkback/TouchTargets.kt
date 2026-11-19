package com.migrely.app.test.talkback

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.ComposeTestRule
import kotlin.math.roundToInt

/** A clickable control and the size of its own touch area, in dp, with no widening (#255, design D5). */
data class TouchTarget(val words: String, val widthDp: Int, val heightDp: Int) {
    override fun toString() = "$words ${widthDp}x$heightDp dp"
}

/**
 * Every clickable control inside the page matched by [page], with the size of the node that takes its click. Inside
 * `PageScroll` this is the whole touch area: Compose no longer widens it to 48 dp. Material keeps a 48 dp layout
 * around a smaller control, but the click lands only on the control's own node.
 */
fun ComposeTestRule.touchTargets(page: SemanticsMatcher = hasScrollAction()): List<TouchTarget> =
    onAllNodes(hasClickAction() and hasAnyAncestor(page)).fetchSemanticsNodes().map { node ->
        with(density) {
            TouchTarget(
                words = node.mergedWords() ?: "(no words, id ${node.id})",
                widthDp = node.size.width.toDp().value.roundToInt(),
                heightDp = node.size.height.toDp().value.roundToInt()
            )
        }
    }
