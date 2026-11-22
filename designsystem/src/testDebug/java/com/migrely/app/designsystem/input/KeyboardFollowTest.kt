package com.migrely.app.designsystem.input

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// The keyboard's first frame on emulator-5570 under TalkBack (#259), and its next one, in px.
private const val FIRST_FRAME_PX = 422
private const val NEXT_FRAME_PX = 577

/**
 * A focused field's line follows the growing keyboard until the person moves it away (BRD D180 (2); #259). Only a
 * layout the follow has already acted on records what the person sees. With an accessibility service on, Compose lays
 * the screen out again from a posted runnable (its semantics check, `measureAndLayout`) between the keyboard's frame
 * and the follow: on 5570 that layout came at the frame's height with the line under the keyboard, ended the
 * following, and the line was never brought back (diag log g2: `follow dropped` from `semanticsChangeChecker`).
 *
 * The layouts are reported by hand at the moments 5570 logged them, with real coordinates: a field inside the visible
 * part (the line shows) and one past its edge (the keyboard covers the line).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class KeyboardFollowTest {
    @get:Rule
    val rule = createComposeRule()

    private val follow = KeyboardFollow()
    private var keyboard by mutableIntStateOf(0)
    private var brought = 0
    private lateinit var shown: LayoutCoordinates
    private lateinit var covered: LayoutCoordinates

    // The line being typed, in the field's coordinates.
    private val line = Rect(0f, 0f, 10f, 10f)

    /** The focused field, its line brought into view on focus and shown at the keyboard's first height, 0. */
    private fun focus() {
        rule.setContent {
            // The visible part: what lies past its edge is under the keyboard.
            Box(Modifier.size(100.dp).clipToBounds()) {
                Box(Modifier.size(50.dp).onGloballyPositioned { shown = it })
                Box(
                    Modifier
                        .offset(y = 200.dp)
                        .size(50.dp)
                        .onGloballyPositioned { covered = it }
                )
            }
            LaunchedEffect(Unit) { follow.followKeyboard({ keyboard }) { brought++ } }
        }
        rule.waitForIdle()
        follow.onBroughtIntoView()
        follow.onPositioned(0, shown, line)
        rule.waitForIdle()
    }

    /**
     * The keyboard's frame at [px], then the screen's [layouts] at that height, all before the follow runs: in one
     * turn of the main thread, as on 5570, where the follow's collector runs from a later dispatch.
     */
    private fun keyboardFrame(px: Int, layouts: List<LayoutCoordinates>) {
        rule.runOnUiThread {
            keyboard = px
            layouts.forEach { follow.onPositioned(px, it, line) }
        }
        rule.waitForIdle()
    }

    @Test
    fun `Compose's own layout before the follow does not end it`() {
        focus()

        // The frame's layout, then Compose's accessibility layout at the same height, the line under the keyboard.
        keyboardFrame(FIRST_FRAME_PX, listOf(covered, covered))

        assertEquals("the line brought into view as the keyboard grew", 1, brought)
    }

    @Test
    fun `The following goes on through the next frames`() {
        focus()
        keyboardFrame(FIRST_FRAME_PX, listOf(covered, covered))

        keyboardFrame(NEXT_FRAME_PX, listOf(covered, covered))

        assertEquals("the line brought into view at both frames", 2, brought)
    }

    // Negative control: with only the frame's own layout, as with no accessibility service, the keyboard's growth is
    // followed on develop too. The test can pass, and the failure above needs the extra layout.
    @Test
    fun `With only the frame's layout the line is brought into view`() {
        focus()

        keyboardFrame(FIRST_FRAME_PX, listOf(covered))

        assertEquals("the line brought into view as the keyboard grew", 1, brought)
    }

    // Kept: a person who scrolls the line away while the keyboard holds still ends the following, so a keyboard that
    // grows later leaves the page where they put it (BRD D180 (5), ux U6).
    @Test
    fun `A scroll away while the keyboard holds still still ends the following`() {
        focus()
        keyboardFrame(FIRST_FRAME_PX, listOf(covered))
        follow.onPositioned(FIRST_FRAME_PX, shown, line)

        // The person scrolls the line away: a layout at the same height, the line hidden, after the follow ran.
        rule.runOnUiThread { follow.onPositioned(FIRST_FRAME_PX, covered, line) }
        rule.waitForIdle()
        keyboardFrame(NEXT_FRAME_PX, listOf(covered))

        assertEquals("brought only at the first frame", 1, brought)
    }
}
