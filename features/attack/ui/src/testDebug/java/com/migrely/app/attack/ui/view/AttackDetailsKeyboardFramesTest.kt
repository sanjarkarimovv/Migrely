package com.migrely.app.attack.ui.view

import android.graphics.Insets
import android.view.WindowInsets
import android.view.WindowInsetsAnimation
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import com.migrely.app.attack.ui.model.AttackDetailsViewStateUiModel
import com.migrely.app.designsystem.theme.MigrelyTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

// Gboard's keyboard in QA's 360 dp window (`wm size 945x2100`, landscape), in px: its top at 318.
private const val KEYBOARD_PX = 945 - KEYBOARD_TOP_360_PX

// Ten keyboard frames, 16 ms apart, as on a phone.
private val TEN_STEPS = listOf(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1f)
private const val FRAME_MILLIS = 16L
private const val SETTLE_MILLIS = 1_000L
private const val SETTLE_ROUNDS = 5

// Where the note's top shows before the keyboard, above the page's bottom (QA's #259 rows: 683 of 882 px).
private const val NOTE_SHOWN_PX = 200

// OutlinedTextField's padding around its text, with no label: where the text layout starts inside the field.
private val FIELD_PADDING = 16.dp
private const val TOLERANCE = 1f

/**
 * On Attack details the note's line being typed shows in every keyboard frame's own layout while the keyboard opens
 * (BRD D194, #259 review F3): the screen wires its page through `TypingSpace.scrollingPage`, so the state the page
 * scrolls with is the one kept in step with the keyboard. Each frame is laid out, then laid out again at the same height
 * (Compose's accessibility layout), and read before any effect runs: effects queue on their own dispatcher.
 */
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = WINDOW_360, fontScale = 2f)
class AttackDetailsKeyboardFramesTest {
    private val effects = StandardTestDispatcher()

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>(effects)

    private var draft by mutableStateOf<String?>(null)

    private val note get() = rule.onNode(hasSetTextAction())
    private val page get() = rule.onNode(hasScrollAction())

    private fun settle() {
        repeat(SETTLE_ROUNDS) {
            rule.waitForIdle()
            effects.scheduler.runCurrent()
        }
        rule.waitForIdle()
    }

    private fun density() = rule.activity.resources.displayMetrics.density

    /** QA's #259 set-up: the note's top showing near the page's bottom, the note focused, its own bring done. */
    private fun focusNote() {
        rule.runOnUiThread { rule.activity.drawEdgeToEdge() }
        rule.setContent {
            MigrelyTheme {
                AttackDetailsContents(
                    viewState = AttackDetailsViewStateUiModel.Ready(
                        previewDetails().copy(note = "")
                    ),
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
        settle()
        rule.runOnUiThread { rule.activity.setShortWindowInsets(null) }
        settle()
        val toNote = note.getUnclippedBoundsInRoot().top.value * density() -
            (visiblePart().bottom - NOTE_SHOWN_PX)
        page.performSemanticsAction(SemanticsActions.ScrollBy) { it(0f, toNote) }
        settle()
        note.requestFocus()
        rule.mainClock.advanceTimeBy(SETTLE_MILLIS)
        settle()
    }

    private fun visiblePart(): Rect = page.fetchSemanticsNode().boundsInRoot

    private fun cursorLine(): Rect {
        val results = mutableListOf<TextLayoutResult>()
        note.fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action!!(results)
        val field = note.getUnclippedBoundsInRoot().top.value * density()
        val offset = note.fetchSemanticsNode().config[SemanticsProperties.TextSelectionRange].end
        val cursor = results.first().getCursorRect(offset)
        val top = field + FIELD_PADDING.value * density() + cursor.top
        return Rect(0f, top, 0f, top + cursor.height)
    }

    private fun insets(keyboard: Int): WindowInsets = WindowInsets.Builder()
        .setInsets(WindowInsets.Type.statusBars(), Insets.of(0, STATUS_PX, 0, 0))
        .setVisible(WindowInsets.Type.statusBars(), true)
        .setInsets(WindowInsets.Type.ime(), Insets.of(0, 0, 0, keyboard))
        .setVisible(WindowInsets.Type.ime(), keyboard > 0)
        .build()

    /** The frame's layout, then Compose's accessibility layout at the same height. */
    private fun frameAndAccessibilityLayout() {
        val root = rule.onRoot().fetchSemanticsNode().root!!
        root.measureAndLayoutForTest()
        Snapshot.sendApplyNotifications()
        root.measureAndLayoutForTest()
    }

    /** The keyboard's own animation through [TEN_STEPS]; returns the line and visible part in each frame's layout. */
    private fun openKeyboard(): List<Pair<Rect, Rect>> {
        rule.mainClock.autoAdvance = false
        val decor = rule.activity.window.decorView
        val animation = WindowInsetsAnimation(WindowInsets.Type.ime(), null, ANIMATION_MILLIS)
        rule.runOnUiThread {
            decor.dispatchWindowInsetsAnimationPrepare(animation)
            decor.dispatchApplyWindowInsets(insets(KEYBOARD_PX))
            decor.dispatchWindowInsetsAnimationStart(
                animation,
                WindowInsetsAnimation.Bounds(Insets.NONE, Insets.of(0, 0, 0, KEYBOARD_PX))
            )
        }
        settle()
        val frames = TEN_STEPS.map { fraction ->
            rule.runOnUiThread {
                animation.fraction = fraction
                decor.dispatchWindowInsetsAnimationProgress(
                    insets((KEYBOARD_PX * fraction).toInt()),
                    listOf(animation)
                )
            }
            frameAndAccessibilityLayout()
            val frame = cursorLine() to visiblePart()
            settle()
            rule.mainClock.advanceTimeBy(FRAME_MILLIS)
            settle()
            frame
        }
        rule.runOnUiThread { decor.dispatchWindowInsetsAnimationEnd(animation) }
        settle()
        return frames
    }

    @Test
    fun `The note's line shows in every keyboard frame and stays once the keyboard is up`() {
        focusNote()

        val frames = openKeyboard()
        val atLastFrame = cursorLine().top
        rule.mainClock.advanceTimeBy(SETTLE_MILLIS)
        settle()

        frames.forEachIndexed { index, (line, visible) ->
            val cutBelow = (line.bottom - visible.bottom).coerceAtLeast(0f)
            val cutAbove = (visible.top - line.top).coerceAtLeast(0f)
            assertTrue(
                "keyboard frame ${index + 1}: line ${line.top}..${line.bottom} in " +
                    "${visible.top}..${visible.bottom}",
                cutAbove <= TOLERANCE && cutBelow <= TOLERANCE
            )
        }
        assertEquals(
            "the line's top after the keyboard's last frame",
            atLastFrame,
            cursorLine().top,
            TOLERANCE
        )
    }

    private companion object {
        const val ANIMATION_MILLIS = 250L
    }
}
