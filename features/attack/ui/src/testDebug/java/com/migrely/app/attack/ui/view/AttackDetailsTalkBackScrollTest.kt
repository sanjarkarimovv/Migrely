package com.migrely.app.attack.ui.view

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.unit.Density
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat
import com.migrely.app.attack.ui.model.AttackDetailsViewStateUiModel
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.test.talkback.TalkBackTree
import com.migrely.app.test.talkback.mergedNodes
import com.migrely.app.test.talkback.mergedWords
import com.migrely.app.test.talkback.turnOnTalkBack
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

// Past either end of any page: the scroll stops there.
private const val FAR = 100_000f

/**
 * TalkBack keeps its place when it swipes past the edge of what is on screen (#255). It then scrolls the page one step
 * and moves on from the item it was on. Compose drops an item from the accessibility tree once it leaves the screen,
 * so after the step that item must still be on screen; with a step of a whole page it was not, and TalkBack fell back
 * to the page's first or last item, skipped a part of the page and could circle in it.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AttackDetailsTalkBackScrollTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val page get() = rule.onNode(hasScrollAction())

    private fun show(fontScale: Float) {
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
    }

    /** The page's items TalkBack stops on, from the top down. */
    private fun stops(): List<SemanticsNode> {
        val pageId = page.fetchSemanticsNode().id
        return rule.mergedNodes().values
            .filter { node -> node.mergedWords() != null }
            .filter { node -> generateSequence(node.parent) { it.parent }.any { it.id == pageId } }
            .sortedWith(compareBy({ it.positionInRoot.y }, { it.positionInRoot.x }))
    }

    /** Whole inside the page's visible part. */
    private fun SemanticsNode.wholeInView(): Boolean {
        val view = page.fetchSemanticsNode().boundsInRoot
        return positionInRoot.y >= view.top && positionInRoot.y + size.height <= view.bottom
    }

    private fun onScreen(node: SemanticsNode): Boolean {
        var info: AccessibilityNodeInfoCompat? = null
        rule.runOnUiThread { info = TalkBackTree(rule.activity).node(node.id) }
        return info?.isVisibleToUser == true
    }

    private fun perform(id: Int, action: Int) {
        rule.runOnUiThread { TalkBackTree(rule.activity).perform(id, action) }
        rule.waitForIdle()
    }

    /**
     * TalkBack's swipes through the whole page from one end, moving as TalkBack does: to the next item; an item partly
     * on screen is shown whole first (ACTION_SHOW_ON_SCREEN), and when the next item is not on screen at all the page
     * scrolls one step (ACTION_SCROLL_BACKWARD or FORWARD). Returns what the steps lost.
     */
    private fun lostOnTheWay(backward: Boolean): List<String> {
        page.performSemanticsAction(SemanticsActions.ScrollBy) {
            it(0f, if (backward) FAR else -FAR)
        }
        rule.waitForIdle()
        val stops = stops()
        val inView = stops.filter { it.wholeInView() }
        var index = stops.indexOf(if (backward) inView.last() else inView.first())
        val direction = if (backward) -1 else 1
        val step = if (backward) {
            AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD
        } else {
            AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD
        }
        val lost = mutableListOf<String>()
        while (true) {
            val pivot = stops[index]
            val target = stops.getOrNull(index + direction) ?: break
            if (!onScreen(target)) {
                perform(page.fetchSemanticsNode().id, step)
                if (!onScreen(pivot)) lost += "${pivot.mergedWords()} left the screen"
                if (!onScreen(target)) {
                    lost += "${target.mergedWords()} not on screen after the step"
                    break
                }
            }
            if (!stops().first { it.id == target.id }.wholeInView()) {
                perform(target.id, AccessibilityActionCompat.ACTION_SHOW_ON_SCREEN.id)
            }
            index += direction
        }
        val end = if (backward) 0 else stops.lastIndex
        if (index != end) lost += "stopped at ${stops[index].mergedWords()}"
        return lost
    }

    @Test
    @Config(sdk = [35], qualifiers = "w411dp-h891dp")
    fun `Portrait at 100 percent, swiping back keeps TalkBack's place up to the top`() {
        show(fontScale = 1f)
        assertEquals(emptyList<String>(), lostOnTheWay(backward = true))
    }

    @Test
    @Config(sdk = [35], qualifiers = "w411dp-h891dp")
    fun `Portrait at 100 percent, swiping on keeps TalkBack's place down to the end`() {
        show(fontScale = 1f)
        assertEquals(emptyList<String>(), lostOnTheWay(backward = false))
    }

    @Test
    @Config(sdk = [35], qualifiers = "w800dp-h360dp")
    fun `Landscape 360 dp at 200 percent, swiping back keeps TalkBack's place up to the top`() {
        show(fontScale = 2f)
        assertEquals(emptyList<String>(), lostOnTheWay(backward = true))
    }

    @Test
    @Config(sdk = [35], qualifiers = "w800dp-h360dp")
    fun `Landscape 360 dp at 200 percent, swiping on keeps TalkBack's place down to the end`() {
        show(fontScale = 2f)
        assertEquals(emptyList<String>(), lostOnTheWay(backward = false))
    }
}
