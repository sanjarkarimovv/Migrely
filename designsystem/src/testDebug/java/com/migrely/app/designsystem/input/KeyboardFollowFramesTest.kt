package com.migrely.app.designsystem.input

import androidx.activity.ComponentActivity
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.layout.MigrelyInsets
import com.migrely.app.designsystem.theme.MigrelyTheme
import kotlin.math.roundToInt
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

// QA's API 36 360 dp window (`wm size 945x2100`, landscape): the status bar and Gboard's keyboard, in px.
private const val STATUS_PX = 63
private const val KEYBOARD_PX = 627

// Attack details' top bar at 200% (74 dp), and content around the note so the page scrolls both ways.
private val BAR = 74.dp
private val ABOVE = 600.dp
private val BELOW = 400.dp

// OutlinedTextField's padding around its text, with no label: where the text layout starts inside the field.
private val FIELD_PADDING = 16.dp

// Where the note's top shows before the keyboard opens, above the page's bottom (QA's x1: 683 of 882 px).
private const val NOTE_SHOWN_PX = 200

// The keyboard's steps, as fractions of its height (ux C1 to C3).
private val STEPS = listOf(0.3f, 0.7f, 1f)

private const val TOLERANCE = 1f

// Labels of the two single-line fields, as on Edit medication.
private const val NAME = "Name"
private const val DOSE = "Dose"

// Ten keyboard frames, as a keyboard animates at 60 Hz on a phone, 16 ms apart.
private val TEN_STEPS = listOf(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1f)
private const val FRAME_MILLIS = 16L

// A keyboard switching to its symbols, taller by 73 px.
private const val SYMBOLS_PX = 700

/**
 * The note's line moves with the keyboard, in the same turn as the keyboard's frame, while the top bar steps aside
 * (BRD D194, design D2 to D4; tasks 3.2 to 3.5). Each keyboard step is run as on emulator-5570 with TalkBack on: the
 * frame's layout, then a second layout at the same height before the follow's collector runs (Compose's semantics
 * check). Nothing animates in between: the clock does not advance, so a line brought into view by an animation is
 * still hidden when the step is read, as it was when TalkBack looked for its focus (#259).
 */
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w800dp-h360dp-land-420dpi", fontScale = 2f)
class KeyboardFollowFramesTest {
    private val effects = StandardTestDispatcher()

    // Effects queue on their own dispatcher, as on the device, where the follow's collector runs from a later dispatch:
    // they run only when [settle] lets them, never inside the keyboard's inset dispatch.
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>(effects)

    private var value by mutableStateOf(TextFieldValue(""))
    private var name by mutableStateOf("")
    private var dose by mutableStateOf("")
    private var singleLine = false
    private val scroll = ScrollState(0)

    // Read where the page is placed: a change places the page and everything in it again, at the same spot.
    private var placements by mutableIntStateOf(0)

    // With two fields, the first (Name) is the one focused first.
    private var twoFields = false
    private val note get() = rule.onNode(
        hasSetTextAction() and (if (twoFields) hasText(NAME) else hasSetTextAction())
    )
    private val focused get() = note
    private val page get() = rule.onNode(hasScrollAction())

    /**
     * The note on a page under a bar, its top [NOTE_SHOWN_PX] above the page's bottom, focused. With [typingSpace]
     * false the page has no typing space, as a screen without D192's bar. With [single] a
     * labelled single-line field stands for the note, as on Log and Edit medication and Delete all data.
     */
    private fun focusNote(
        typingSpace: Boolean = true,
        single: Boolean = false,
        cutAfterFocusPx: Int = 0,
        shownPx: Int = NOTE_SHOWN_PX,
        bringRunningMillis: Long? = null
    ) {
        singleLine = single
        rule.runOnUiThread { rule.activity.drawEdgeToEdge() }
        rule.setContent {
            MigrelyTheme {
                val space = rememberTypingSpace()
                Column(Modifier.fillMaxSize()) {
                    Box(
                        Modifier
                            .then(if (typingSpace) space.barModifier else Modifier)
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .height(BAR)
                    )
                    Page(if (typingSpace) space else null)
                }
            }
        }
        settle()
        // After the content is attached: attaching dispatches the window's own insets again.
        rule.runOnUiThread { rule.activity.setInsets(keyboardPx = 0, statusPx = STATUS_PX) }
        settle()
        val toNote = note.getUnclippedBoundsInRoot().top.value * density() -
            (visiblePart().bottom - shownPx)
        page.performSemanticsAction(SemanticsActions.ScrollBy) { it(0f, toNote) }
        settle()
        if (bringRunningMillis != null) {
            // The keyboard comes while the focus's own bring is still moving the page: [bringRunningMillis] of it.
            rule.mainClock.autoAdvance = false
            focused.requestFocus()
            settle()
            rule.mainClock.advanceTimeBy(bringRunningMillis)
            settle()
            return
        }
        focused.requestFocus()
        // The focus's own bring runs to its end before the keyboard comes.
        rule.mainClock.advanceTimeBy(SETTLE_MILLIS)
        settle()
        if (cutAfterFocusPx > 0) {
            page.performSemanticsAction(SemanticsActions.ScrollBy) {
                it(0f, -cutAfterFocusPx.toFloat())
            }
            rule.mainClock.advanceTimeBy(SETTLE_MILLIS)
            settle()
        }
    }

    @Composable
    private fun Page(space: TypingSpace?) {
        Column(
            Modifier
                .fillMaxSize()
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    layout(placeable.width, placeable.height) {
                        placements
                        placeable.place(0, 0)
                    }
                }
                .windowInsetsPadding(MigrelyInsets.barsAndKeyboard.only(WindowInsetsSides.Bottom))
                .then(space?.scrollingPage(scroll) ?: Modifier.verticalScroll(scroll))
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(ABOVE))
            Box {
                if (space != null) {
                    ProvideTypingSpace(space) { Note() }
                } else {
                    Note()
                }
            }
            Spacer(Modifier.height(BELOW))
        }
    }

    @Composable
    private fun Note() = if (twoFields) {
        Column {
            Field(NAME, name) { name = it }
            Field(DOSE, dose) { dose = it }
        }
    } else if (singleLine) {
        SingleLineFieldInView(hasLabel = true) { inView, interactionSource ->
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                label = { Text("Name") },
                interactionSource = interactionSource,
                modifier = inView.fillMaxWidth()
            )
        }
    } else {
        MultiLineNote()
    }

    @Composable
    private fun Field(label: String, text: String, onText: (String) -> Unit) =
        SingleLineFieldInView(hasLabel = true) { inView, interactionSource ->
            OutlinedTextField(
                value = text,
                onValueChange = onText,
                singleLine = true,
                label = { Text(label) },
                interactionSource = interactionSource,
                modifier = inView.fillMaxWidth()
            )
        }

    @Composable
    private fun MultiLineNote() = CursorFollowingTextField(
        value = value,
        onValueChange = { value = it },
        minLines = 3,
        modifier = Modifier.fillMaxWidth()
    )

    private fun density() = rule.activity.resources.displayMetrics.density

    /** Lays out, then runs the queued effects, until nothing is left to run. */
    private fun settle() {
        repeat(SETTLE_ROUNDS) {
            rule.waitForIdle()
            effects.scheduler.runCurrent()
        }
        rule.waitForIdle()
    }

    private fun visiblePart(): Rect = page.fetchSemanticsNode().boundsInRoot

    private fun layoutResult(): TextLayoutResult {
        val results = mutableListOf<TextLayoutResult>()
        note.fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action!!(results)
        return results.first()
    }

    /** The field's whole bounds, unclipped, in px, in the root's coordinates. */
    private fun fieldBounds(): Rect {
        val bounds = note.getUnclippedBoundsInRoot()
        return Rect(0f, bounds.top.value * density(), 0f, bounds.bottom.value * density())
    }

    /** The line holding the cursor, in px, in the root's coordinates. */
    private fun cursorLine(): Rect {
        val field = note.getUnclippedBoundsInRoot().top.value * density()
        val offset = note.fetchSemanticsNode().config[SemanticsProperties.TextSelectionRange].end
        val cursor = layoutResult().getCursorRect(offset)
        val top = field + FIELD_PADDING.value * density() + cursor.top
        return Rect(0f, top, 0f, top + cursor.height)
    }

    /** The frame's layout, then Compose's semantics check laying the screen out again at the same height. */
    private fun frameAndAccessibilityLayout() {
        val root = rule.onRoot().fetchSemanticsNode().root!!
        root.measureAndLayoutForTest()
        placements++
        Snapshot.sendApplyNotifications()
        root.measureAndLayoutForTest()
    }

    /** One record per keyboard step: the keyboard's height, the page's scroll, the line and the visible part. */
    private class Step(
        val keyboard: Int,
        val scroll: Int,
        val line: Rect,
        val field: Rect,
        val visible: Rect
    )

    /**
     * Opens the keyboard through [fractions] of its height, each step in one turn of the main thread as on 5570, then
     * lets the follow run without advancing the clock. Returns a record before the first step and after each.
     */
    private fun openKeyboard(fractions: List<Float> = STEPS): List<Step> {
        rule.mainClock.autoAdvance = false
        val steps = mutableListOf(record(0))
        var keyboard: Int? = null
        val opening = KeyboardAnimation(rule.activity, STATUS_PX, 0, KEYBOARD_PX) {
            frameAndAccessibilityLayout()
            // What the frame and the accessibility service see, before any effect or collector has run.
            keyboard?.let { frames += record(it) }
            keyboard = null
            settle()
        }
        opening.start()
        fractions.forEach { fraction ->
            keyboard = (KEYBOARD_PX * fraction).toInt()
            opening.progress(fraction)
            steps += record((KEYBOARD_PX * fraction).toInt())
        }
        opening.end()
        return steps
    }

    /**
     * As [openKeyboard], but a frame of time (16 ms) passes after each keyboard step, so an animation already running
     * (the focus's own bring, Compose's scroll of a focused field) moves on with the keyboard, as on a device.
     */
    private fun openKeyboardTimed(fractions: List<Float>): List<Step> =
        moveKeyboard(0, KEYBOARD_PX, fractions)

    /** The keyboard going from [from] to [to] px through [fractions] of the way, a frame of time after each step. */
    private fun moveKeyboard(from: Int, to: Int, fractions: List<Float>): List<Step> {
        rule.mainClock.autoAdvance = false
        val steps = mutableListOf(record(from))
        var keyboard: Int? = null
        val moving = KeyboardAnimation(rule.activity, STATUS_PX, from, to) {
            frameAndAccessibilityLayout()
            keyboard?.let { frames += record(it) }
            keyboard = null
            settle()
            rule.mainClock.advanceTimeBy(FRAME_MILLIS)
            settle()
        }
        moving.start()
        fractions.forEach { fraction ->
            keyboard = from + ((to - from) * fraction).toInt()
            moving.progress(fraction)
            steps += record(from + ((to - from) * fraction).toInt())
        }
        moving.end()
        return steps
    }

    /** How far the page moves once the keyboard has stopped: a second of time, nothing else. */
    private fun movementAfterKeyboard(): Int {
        val atLastFrame = scroll.value
        rule.mainClock.advanceTimeBy(SETTLE_MILLIS)
        settle()
        return scroll.value - atLastFrame
    }

    private fun record(keyboard: Int) =
        Step(keyboard, scroll.value, cursorLine(), fieldBounds(), visiblePart())

    // Each keyboard step as its frame's own layout left it, before the follow's collector ran.
    private val frames = mutableListOf<Step>()

    private fun assertLineShows(step: String, line: Rect, visible: Rect) {
        val cutAbove = (visible.top - line.top).coerceAtLeast(0f)
        val cutBelow = (line.bottom - visible.bottom).coerceAtLeast(0f)
        assertTrue(
            "$step: line ${line.top}..${line.bottom} in the visible part " +
                "${visible.top}..${visible.bottom}: cut by $cutAbove px above and $cutBelow px below",
            cutAbove <= TOLERANCE && cutBelow <= TOLERANCE
        )
    }

    // BRD D194 (1): the page moves the line in the layout pass that shrinks the visible part, so neither the frame nor
    // Compose's accessibility layout after it ever has the line under the keyboard.
    @Test
    fun `In each keyboard frame's own layout the line being typed shows`() {
        focusNote()

        openKeyboard()

        assertEquals("frames recorded", STEPS.size, frames.size)
        frames.forEach { frame ->
            assertLineShows(
                "keyboard ${frame.keyboard} px, the frame's layout",
                frame.line,
                frame.visible
            )
        }
    }

    // As above for a note wholly in view before the keyboard, as at the page's end: Compose's own scroll then brings
    // the focused field back itself, animated, and the page must still move the line in the keyboard's frame.
    @Test
    fun `A note wholly in view before the keyboard shows in each keyboard frame`() {
        focusNote(shownPx = WHOLE_NOTE_SHOWN_PX)
        val before = fieldBounds()
        assertTrue("the note starts wholly in view", before.bottom <= visiblePart().bottom)

        openKeyboard()

        frames.forEach { frame ->
            assertLineShows(
                "keyboard ${frame.keyboard} px, the frame's layout",
                frame.line,
                frame.visible
            )
        }
    }

    // ux C1: the page never moves further than the keyboard did, plus 1 px; C2: never back down.
    @Test
    fun `Each step the page scrolls no further than the keyboard moved and never back`() {
        focusNote()

        openKeyboard().zipWithNext().forEach { (before, after) ->
            val scrolled = after.scroll - before.scroll
            val keyboard = after.keyboard - before.keyboard
            assertTrue(
                "keyboard ${after.keyboard} px: the page scrolled $scrolled px for a keyboard move of $keyboard px",
                scrolled in 0..(keyboard + TOLERANCE.roundToInt())
            )
        }
    }

    // ux C2: no double move where the bar steps aside in the same frame. Where the page scrolled, it scrolled only as
    // far as the line needs: the line, with the margins the visible part leaves it, ends at the part's bottom.
    @Test
    fun `Where the page scrolls it stops with the line at the visible part's bottom`() {
        focusNote()
        val margin = (8.dp.value * density())

        val scrolled = openKeyboard().zipWithNext().filter { (before, after) ->
            after.scroll >
                before.scroll
        }

        assertTrue("no step scrolled the page", scrolled.isNotEmpty())
        scrolled.forEach { (_, after) ->
            val clearance = (after.visible.height - after.line.height).coerceIn(0f, margin)
            assertEquals(
                "keyboard ${after.keyboard} px: the line's bottom and its margin against the part's bottom",
                after.visible.bottom,
                after.line.bottom + clearance,
                TOLERANCE
            )
        }
    }

    // ux C3: nothing moves once the keyboard has settled.
    @Test
    fun `Once the keyboard has settled the page does not move`() {
        focusNote()
        val settled = openKeyboard().last().scroll

        rule.mainClock.autoAdvance = true
        rule.mainClock.advanceTimeBy(SETTLE_MILLIS)
        settle()

        assertEquals("the scroll after the keyboard settled", settled, scroll.value)
    }

    // ux C1: a keyboard whose frames were skipped moves the page once, never above the keyboard's whole move.
    @Test
    fun `A keyboard that skips its frames moves the page once and no further`() {
        focusNote()

        val (before, after) = openKeyboard(listOf(1f))

        assertLineShows("keyboard up in one step", after.line, after.visible)
        assertTrue(
            "the page scrolled ${after.scroll - before.scroll} px for a keyboard move of $KEYBOARD_PX px",
            after.scroll - before.scroll in 0..KEYBOARD_PX + 1
        )
    }

    // Design D2, D5: a field outside a typing space keeps the old bring. It animates, so it has not moved the page in
    // the keyboard's own turn; once time runs it has, as on develop: the bar stays, the visible part (61 px) cannot hold
    // the line, so the bring shows the line's top with its margin at the part's top.
    @Test
    fun `Outside a typing space the line is brought into view as before`() {
        focusNote(typingSpace = false)
        val steps = openKeyboard(listOf(1f))

        assertEquals(
            "no move in the keyboard's own turn",
            steps.first().scroll,
            steps.last().scroll
        )
        rule.mainClock.autoAdvance = true
        settle()
        assertEquals(
            "the line's top with its margin at the visible part's top",
            visiblePart().top,
            cursorLine().top - 8.dp.value * density(),
            TOLERANCE
        )
    }

    // A drag that starts on the note still scrolls the page, as before.
    @Test
    fun `A drag that starts on the note scrolls the page as before`() {
        focusNote()
        val before = scroll.value

        note.performTouchInput { swipeUp() }
        settle()

        assertTrue("the page scrolled ${scroll.value - before} px", scroll.value > before)
    }

    // Task 3.3: a single-line field moves in step too, with ux U1's largest area that fits: here the whole field. It
    // starts with its bottom cut by the page's bottom and its input line whole, so the page does not track it itself
    // (once it is wholly in view the page does: the cell below).
    @Test
    fun `A single-line field cut at the bottom shows whole after the keyboard's first frame`() {
        focusNote(single = true, cutAfterFocusPx = SINGLE_CUT_PX)

        val before = openKeyboard(listOf(STEPS.first())).first()

        assertTrue("the field started cut", before.field.bottom > before.visible.bottom)
        val frame = frames.single()
        assertLineShows(
            "keyboard ${frame.keyboard} px, the field in the frame's layout",
            frame.field,
            frame.visible
        )
    }

    @Test
    fun `Each step a single-line field's page scrolls no further than the keyboard moved`() {
        focusNote(single = true, cutAfterFocusPx = SINGLE_CUT_PX)

        openKeyboard().zipWithNext().forEach { (before, after) ->
            val scrolled = after.scroll - before.scroll
            val keyboard = after.keyboard - before.keyboard
            assertTrue(
                "keyboard ${after.keyboard} px: the page scrolled $scrolled px for a keyboard move of $keyboard px",
                scrolled in 0..(keyboard + TOLERANCE.roundToInt())
            )
        }
    }

    // The ContentInViewNode case: a field wholly in view before the keyboard is Compose's own focused child, which the
    // page brings back itself, animated, once the keyboard clips it; while that runs a scrollable takes no raw scroll.
    // The page's own layout keeps the field whole in each keyboard frame regardless (single-line fields are wholly in
    // view after their focus bring).
    @Test
    fun `A field wholly in view before the keyboard stays whole in each keyboard frame`() {
        focusNote(single = true)
        val before = fieldBounds()
        assertTrue("the field starts wholly in view", before.bottom <= visiblePart().bottom)

        openKeyboard()

        assertEquals("frames recorded", STEPS.size, frames.size)
        frames.forEach { frame ->
            assertLineShows(
                "keyboard ${frame.keyboard} px, the field in the frame's layout",
                frame.field,
                frame.visible
            )
        }
    }

    // BRD D180 (5): a person who scrolled the line away while the keyboard held still is not pulled back when it
    // grows: the page keeps the line in view only while the field follows the keyboard.
    @Test
    fun `After a scroll away a growing keyboard leaves the page where it was put`() {
        focusNote()
        page.performSemanticsAction(SemanticsActions.ScrollBy) { it(0f, -SCROLLED_AWAY_PX) }
        settle()
        val away = scroll.value

        openKeyboard()

        frames.forEach { frame ->
            assertEquals(
                "keyboard ${frame.keyboard} px: the scroll in the frame's layout",
                away,
                frame.scroll
            )
        }
    }

    // F1: the keyboard comes while the focus's own bring is still moving the page (#259 review cr-259 F1), the
    // note's top only just showing at the page's bottom edge.
    @Test
    fun `With the focus bring still running, the line shows in each of ten keyboard frames`() {
        focusNote(shownPx = EDGE_SHOWN_PX, bringRunningMillis = FRAME_MILLIS)

        openKeyboardTimed(TEN_STEPS)

        frames.forEach { frame ->
            assertLineShows(
                "keyboard ${frame.keyboard} px, the frame's layout",
                frame.line,
                frame.visible
            )
        }
    }

    // A one-step keyboard leaves an unfollowed note less than its line, so the typing space keeps the note even
    // without the bring's follow (#259 (a')): these one-step cells guard the outcome. The follow during the bring is
    // pinned by the ten-step caret-at-end cell (cr-259 N4: no bring delay from 16 to 240 ms tells the two apart).
    @Test
    fun `With the focus bring still running, a one-step keyboard shows the line in its frame`() {
        focusNote(shownPx = EDGE_SHOWN_PX, bringRunningMillis = FRAME_MILLIS)

        openKeyboardTimed(listOf(1f))

        val frame = frames.single()
        assertLineShows(
            "keyboard ${frame.keyboard} px, the frame's layout",
            frame.line,
            frame.visible
        )
    }

    // The note opens with its caret at the end (Attack details' `opensAtEnd`), so the focus's own bring has the furthest
    // to go.
    @Test
    fun `With the caret at the end and the bring running, the line shows in each frame`() {
        val text = (1..NOTE_LINES).joinToString("\n") { "Line $it of the note" }
        value = TextFieldValue(text, TextRange(text.length))
        focusNote(bringRunningMillis = FRAME_MILLIS)

        openKeyboardTimed(TEN_STEPS)

        frames.forEach { frame ->
            assertLineShows(
                "keyboard ${frame.keyboard} px, the frame's layout",
                frame.line,
                frame.visible
            )
        }
    }

    @Test
    fun `With the caret at the note's end, a one-step keyboard shows the line in its frame`() {
        val text = (1..NOTE_LINES).joinToString("\n") { "Line $it of the note" }
        value = TextFieldValue(text, TextRange(text.length))
        focusNote(bringRunningMillis = FRAME_MILLIS)

        openKeyboardTimed(listOf(1f))

        val frame = frames.single()
        assertLineShows(
            "keyboard ${frame.keyboard} px, the frame's layout",
            frame.line,
            frame.visible
        )
    }

    // BRD D180 (5): a drag by the person while the focus's own bring runs cancels it and ends the following; the
    // keyboard then leaves the page where the drag put it.
    @Test
    fun `A drag that cancels the focus bring still ends the following`() {
        focusNote(bringRunningMillis = FRAME_MILLIS)
        page.performTouchInput { swipeDown(durationMillis = DRAG_MILLIS) }
        rule.mainClock.advanceTimeBy(SETTLE_MILLIS)
        settle()
        val dragged = scroll.value

        openKeyboardTimed(TEN_STEPS)

        frames.forEach { frame ->
            assertEquals(
                "keyboard ${frame.keyboard} px: the scroll in the frame's layout",
                dragged,
                frame.scroll
            )
        }
    }

    // F2: the note wholly in view mid-page before the keyboard (cr-259 cell G). Compose scrolls a focused field that
    // was wholly in view back once the keyboard clips it; the page must land where that leaves nothing to do.
    @Test
    fun `A note wholly in view mid-page does not move after a one-step keyboard`() {
        focusNote(shownPx = WHOLE_NOTE_SHOWN_PX)
        assertTrue("the note starts wholly in view", fieldBounds().bottom <= visiblePart().bottom)

        val after = openKeyboardTimed(listOf(1f)).last()

        assertLineShows("keyboard up", after.line, after.visible)
        assertEquals(
            "px the page moved after the keyboard's last frame",
            0,
            movementAfterKeyboard()
        )
    }

    @Test
    fun `A note wholly in view mid-page does not move after a ten-step keyboard`() {
        focusNote(shownPx = WHOLE_NOTE_SHOWN_PX)

        openKeyboardTimed(TEN_STEPS)

        frames.forEach { frame ->
            assertLineShows(
                "keyboard ${frame.keyboard} px, the frame's layout",
                frame.line,
                frame.visible
            )
        }
        assertEquals(
            "px the page moved after the keyboard's last frame",
            0,
            movementAfterKeyboard()
        )
    }

    @Test
    fun `A note cut by the page's bottom does not move after a ten-step keyboard`() {
        focusNote()

        openKeyboardTimed(TEN_STEPS)

        assertEquals(
            "px the page moved after the keyboard's last frame",
            0,
            movementAfterKeyboard()
        )
    }

    // F5: a keyboard that grows while typing (its symbols) follows; one that shrinks back scrolls nothing (BRD D189).
    @Test
    fun `A keyboard growing while up keeps the line, and shrinking back scrolls nothing`() {
        focusNote()
        openKeyboardTimed(TEN_STEPS)
        frames.clear()

        moveKeyboard(KEYBOARD_PX, SYMBOLS_PX, listOf(0.5f, 1f))
        frames.forEach { frame ->
            assertLineShows("taller keyboard ${frame.keyboard} px", frame.line, frame.visible)
        }
        val atSymbols = scroll.value
        frames.clear()

        moveKeyboard(SYMBOLS_PX, KEYBOARD_PX, listOf(0.5f, 1f))
        frames.forEach { frame ->
            assertEquals(
                "shorter keyboard ${frame.keyboard} px: the scroll",
                atSymbols,
                frame.scroll
            )
        }
    }

    // F5: moving to another field with the keyboard up (Edit medication: name, then dose) brings that field into view
    // and the page keeps it, not the first one, as the keyboard grows.
    @Test
    fun `Moving to the next field with the keyboard up keeps that field in view`() {
        twoFields = true
        focusNote(single = true)
        openKeyboardTimed(TEN_STEPS)
        rule.onNode(hasSetTextAction() and hasText(DOSE)).requestFocus()
        rule.mainClock.advanceTimeBy(SETTLE_MILLIS)
        settle()
        frames.clear()

        moveKeyboard(KEYBOARD_PX, SYMBOLS_PX, listOf(0.5f, 1f))

        // The dose field (244 px) no longer fits whole, so ux U1 shows it from its input line down, its bottom at the
        // visible part's bottom.
        val dose = rule.onNode(hasSetTextAction() and hasText(DOSE)).getUnclippedBoundsInRoot()
        assertEquals(
            "the dose field's bottom",
            visiblePart().bottom,
            dose.bottom.value * density(),
            TOLERANCE
        )
    }

    // Device row u3 (#259, decision (a')): the keyboard closed with the note still focused, the person scrolled so the
    // note's top sits just above the page's bottom edge, and the keyboard opens again on the same field. While part of
    // the note shows nothing moves; the keyboard never leaves the focused note wholly hidden (out of the accessibility
    // tree), and once up the line shows.
    @Test
    fun `A keyboard opened again never hides the still-focused note wholly`() {
        val after = reopenAtEdge().last()

        frames.forEach { frame ->
            val (field, visible) = frame.field to frame.visible
            assertTrue(
                "keyboard ${frame.keyboard} px: the note ${field.top}..${field.bottom} is hidden " +
                    "from the visible part ${visible.top}..${visible.bottom}",
                shownInPage(field.top - visible.top, field.height, visible.height.toInt()) >=
                    MIN_SHOWN_PX
            )
        }
        assertLineShows("keyboard up", after.line, after.visible)
    }

    // cr-259 R1 (N1): after the keyboard reopened and kept the note, the person scrolls it wholly away; the taller
    // symbols keyboard then leaves the page where it was put, as after a normal opening (R1c, BRD D180 (5)).
    @Test
    fun `After a reopen kept the note, a scroll away is left alone by a growing keyboard`() {
        reopenAtEdge()

        assertScrollAwayHolds()
    }

    @Test
    fun `After a normal opening, a scroll away is left alone by a growing keyboard`() {
        focusNote()
        openKeyboardTimed(TEN_STEPS)
        rule.mainClock.advanceTimeBy(SETTLE_MILLIS)
        settle()

        assertScrollAwayHolds()
    }

    // cr-259 N3: the note's 60 px are less than its line, so the keyboard's first frame, which takes them, keeps the
    // note; from then on the page follows in step, each later frame no further than the keyboard moved.
    @Test
    fun `From the keyboard's first frame the note is kept, then the page moves in step`() {
        val steps = reopenAtEdge()

        val moves = steps.zipWithNext().filter { (before, after) -> after.scroll != before.scroll }
        assertEquals(
            "the keyboard at the page's first move",
            steps[1].keyboard,
            moves.first().second.keyboard
        )
        moves.drop(1).forEach { (before, after) ->
            assertTrue(
                "keyboard ${before.keyboard} to ${after.keyboard} px: the page moved " +
                    "${after.scroll - before.scroll} px",
                after.scroll - before.scroll <= after.keyboard - before.keyboard + TOLERANCE
            )
        }
    }

    // cr-259 N1: with the caret at the end of a longer note (Attack details opens it there), the kept note follows
    // the keyboard from then on, so its line stays in view in each later frame, not only the note's top.
    @Test
    fun `With the caret at the note's end, the line kept on reopening shows in each frame`() {
        val text = (1..NOTE_LINES).joinToString("\n") { "Line $it of the note" }
        value = TextFieldValue(text, TextRange(text.length))

        reopenAtEdge()

        frames.forEach { frame ->
            assertLineShows(
                "keyboard ${frame.keyboard} px, the frame's layout",
                frame.line,
                frame.visible
            )
        }
    }

    /** The keyboard closes with the note focused, the note's top 60 px above the page's bottom, and opens again. */
    private fun reopenAtEdge(): List<Step> {
        focusNote()
        openKeyboardTimed(TEN_STEPS)
        moveKeyboard(KEYBOARD_PX, 0, listOf(0.5f, 1f))
        rule.mainClock.advanceTimeBy(SETTLE_MILLIS)
        settle()
        val toEdge = fieldBounds().top - (visiblePart().bottom - EDGE_SHOWN_PX)
        page.performSemanticsAction(SemanticsActions.ScrollBy) { it(0f, toEdge) }
        rule.mainClock.advanceTimeBy(SETTLE_MILLIS)
        settle()
        assertTrue(
            "the note's top shows at the page's bottom edge",
            fieldBounds().top < visiblePart().bottom
        )
        frames.clear()
        val steps = openKeyboardTimed(TEN_STEPS)
        rule.mainClock.advanceTimeBy(SETTLE_MILLIS)
        settle()
        return steps
    }

    /** The person scrolls the note wholly away, then the keyboard grows to its symbols: the page stays put. */
    private fun assertScrollAwayHolds() {
        page.performSemanticsAction(SemanticsActions.ScrollBy) { it(0f, -SCROLLED_AWAY_PX) }
        rule.mainClock.advanceTimeBy(SETTLE_MILLIS)
        settle()
        assertTrue("the note is wholly away", fieldBounds().top >= visiblePart().bottom)
        val away = scroll.value
        frames.clear()

        moveKeyboard(KEYBOARD_PX, SYMBOLS_PX, listOf(0.5f, 1f))

        frames.forEach { frame ->
            assertEquals("keyboard ${frame.keyboard} px: the scroll", away, frame.scroll)
        }
        assertEquals("px the page moved after the keyboard", 0, movementAfterKeyboard())
    }

    private companion object {
        const val SETTLE_MILLIS = 1_000L
        const val SETTLE_ROUNDS = 5

        // How far the page is pulled down after the focus: the single-line field's bottom is cut, its input line not.
        const val SINGLE_CUT_PX = 40

        // Where the note's top shows before the keyboard for it to be wholly in view, above the page's bottom.
        const val WHOLE_NOTE_SHOWN_PX = 480

        // How far the person scrolls the page back, the line out of view, before the keyboard opens.
        const val SCROLLED_AWAY_PX = 900f

        // The note's top only just showing at the page's bottom edge before the keyboard (cr-259 F1).
        const val EDGE_SHOWN_PX = 60

        // A note of four lines, opened with its caret at the end.
        const val NOTE_LINES = 4

        // A slow drag, so the page takes it as the person's own scroll.
        const val DRAG_MILLIS = 400L
    }
}
