package com.migrely.app.attack.ui.view

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.unit.Density
import com.migrely.app.attack.ui.model.AttackDetailsViewStateUiModel
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val DELETE = "Delete migraine"

// Past any page's end: the scroll stops there.
private const val END = 10_000f

// The least room a closed 250 dp keyboard leaves at the page's end, in dp: the keyboard less a navigation bar.
private const val SOME_ROOM = 150f

/**
 * Attack details keeps the page still when the keyboard closes near its end (BRD D189, ux 5.1, #238): the note keeps
 * its place, the room below the page holds no semantics node (R11), and a restored screen has no room (R8).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w400dp-h700dp")
class AttackDetailsKeyboardRoomTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val restoration = StateRestorationTester(rule)
    private var draft by mutableStateOf<String?>(null)

    private val note get() = rule.onNode(hasSetTextAction())
    private val content get() = rule.onNode(hasScrollAction())

    private fun show() {
        rule.runOnUiThread { rule.activity.drawEdgeToEdge() }
        restoration.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
                MigrelyTheme {
                    AttackDetailsContents(
                        viewState = AttackDetailsViewStateUiModel.Ready(previewDetails()),
                        timeUiFormatter = previewTimeFormatter,
                        labels = AttackLabels.Default,
                        localState = AttackDetailsLocalState(
                            timeError = null,
                            safetyCardVisible = false,
                            onDismissSafetyCard = {},
                            onTimeErrorShown = {},
                            noteDraft = draft,
                            onNoteDraft = { draft = it }
                        ),
                        actions = previewActions
                    )
                }
            }
        }
        rule.waitForIdle()
    }

    private fun scrollToTheEnd() {
        content.performSemanticsAction(SemanticsActions.ScrollBy) { it(0f, END) }
        rule.waitForIdle()
    }

    /** With the note focused and the keyboard open at the page's end, closes the keyboard: the note's top before. */
    private fun closeTheKeyboardAtTheEnd(): Float {
        show()
        content.performScrollToNode(hasSetTextAction())
        note.performClick()
        rule.waitForIdle()
        rule.runOnUiThread { rule.activity.setKeyboard(KEYBOARD_DP) }
        rule.waitForIdle()
        scrollToTheEnd()
        val before = note.getUnclippedBoundsInRoot().top.value
        rule.runOnUiThread { rule.activity.setKeyboard(0) }
        rule.waitForIdle()
        return before
    }

    /** The empty page below "Delete migraine", down to the visible part's end, in dp. */
    private fun roomBelowTheEnd(): Float {
        val end = rule.onNodeWithText(DELETE).getUnclippedBoundsInRoot().bottom
        return content.getUnclippedBoundsInRoot().bottom.value - end.value
    }

    private fun SemanticsNode.descendants(): List<SemanticsNode> =
        children.flatMap { listOf(it) + it.descendants() }

    @Test
    fun `Closing the keyboard at the page's end leaves the note where it is`() {
        val before = closeTheKeyboardAtTheEnd()

        assertEquals("the note moved", before, note.getUnclippedBoundsInRoot().top.value, 0.5f)
    }

    @Test
    fun `The room holds no semantics node`() {
        closeTheKeyboardAtTheEnd()
        val room = roomBelowTheEnd()
        assertTrue("no room below the page: $room dp", room > SOME_ROOM)
        val density = rule.activity.resources.displayMetrics.density
        val roomTop = rule.onNodeWithText(DELETE).getUnclippedBoundsInRoot().bottom.value * density
        // Fetched last and read at once: a later fetch leaves this tree's nodes without bounds.
        val scroll = rule.onNode(hasScrollAction(), useUnmergedTree = true).fetchSemanticsNode()
        val roomRect = Rect(
            scroll.boundsInRoot.left,
            roomTop + 1,
            scroll.boundsInRoot.right,
            scroll.boundsInRoot.bottom - 1
        )
        val page = scroll.descendants()
        assertTrue("the page's nodes have no bounds", page.count { !it.boundsInRoot.isEmpty } > 1)

        // The page's own nodes: none of them may lie in the room (R11). Its container and the screen hold it.
        val inRoom = page.filter { it.boundsInRoot.overlaps(roomRect) }

        assertEquals(
            "nodes in the room $roomRect: ${inRoom.map { it.config }}",
            emptyList<SemanticsNode>(),
            inRoom
        )
    }

    @Test
    fun `A restored screen has no room`() {
        closeTheKeyboardAtTheEnd()
        assertTrue("no room before the restore", roomBelowTheEnd() > SOME_ROOM)

        restoration.emulateSavedInstanceStateRestore()
        rule.waitForIdle()
        scrollToTheEnd()

        assertEquals("room after the restore", 0f, roomBelowTheEnd(), 0.5f)
    }
}
