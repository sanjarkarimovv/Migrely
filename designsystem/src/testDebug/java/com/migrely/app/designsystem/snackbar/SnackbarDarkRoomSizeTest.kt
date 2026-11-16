package com.migrely.app.designsystem.snackbar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import com.migrely.app.designsystem.theme.MigrelyDarkRoom
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val MESSAGE = "Migraine started"
private const val STORAGE_FULL = "Couldn't save. Your phone's storage is full."
private const val UNDO = "Undo the start"
private const val DISMISS = "Dismiss"
private val DARK_ROOM_TARGET = 64.dp
private val TARGET = 48.dp
private val SIZE_DELTA = 0.5.dp

/** A message's control is 64 dp in an active dark room and 48 dp elsewhere (BRD D182 (4), EC-UX-59, ux B3). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w400dp-h800dp")
class SnackbarDarkRoomSizeTest {
    @get:Rule
    val rule = createComposeRule()

    private var darkRoom by mutableStateOf(false)
    private lateinit var density: Density

    private fun show(undo: Boolean, room: Boolean) {
        darkRoom = room
        rule.setContent {
            density = LocalDensity.current
            MigrelyTheme {
                MigrelyDarkRoom(active = darkRoom) {
                    MigrelySnackbar(
                        text = if (undo) MESSAGE else STORAGE_FULL,
                        tone = if (undo) SnackbarTone.Confirmation else SnackbarTone.Error,
                        action = if (undo) SnackbarAction("Undo", UNDO) {} else null,
                        baseDurationMillis = if (undo) 5_000L else null,
                        dismissLabel = DISMISS,
                        onDismiss = {}
                    )
                }
            }
        }
        rule.waitForIdle()
    }

    private fun controlSize(description: String) =
        rule.onNodeWithContentDescription(description).getUnclippedBoundsInRoot()

    private fun touchSize(description: String): DpSize = with(density) {
        val node = rule.onNodeWithContentDescription(description).fetchSemanticsNode()
        val touch = node.touchBoundsInRoot
        DpSize(touch.width.toDp(), touch.height.toDp())
    }

    private fun assertAtLeast(minimum: Dp, actual: Dp) =
        assertTrue("$actual is below $minimum", actual >= minimum - SIZE_DELTA)

    @Test
    fun `In an active dark room Undo is at least 64 by 64 dp`() {
        show(undo = true, room = true)

        val undo = controlSize(UNDO)
        assertAtLeast(DARK_ROOM_TARGET, undo.width)
        assertAtLeast(DARK_ROOM_TARGET, undo.height)
    }

    @Test
    fun `In an active dark room Dismiss is 64 by 64 dp`() {
        show(undo = false, room = true)

        val dismiss = controlSize(DISMISS)
        assertEquals(DARK_ROOM_TARGET.value, dismiss.width.value, SIZE_DELTA.value)
        assertEquals(DARK_ROOM_TARGET.value, dismiss.height.value, SIZE_DELTA.value)
    }

    // Outside the room nothing grows: Undo keeps its 48 dp minimum height, and Dismiss keeps Material's 40 dp button
    // with its 48 dp touch target, which lies outside its layout bounds.

    @Test
    fun `Outside the dark room Undo keeps its 48 dp height and does not grow`() {
        show(undo = true, room = false)

        val undo = controlSize(UNDO)
        assertAtLeast(TARGET, undo.height)
        assertTrue("${undo.height} grew", undo.height < DARK_ROOM_TARGET)
    }

    @Test
    fun `Outside the dark room Dismiss keeps its 48 dp touch target and does not grow`() {
        show(undo = false, room = false)

        val touch = touchSize(DISMISS)
        assertAtLeast(TARGET, touch.width)
        assertAtLeast(TARGET, touch.height)
        val layoutHeight = controlSize(DISMISS).height
        assertTrue("$layoutHeight grew", layoutHeight < DARK_ROOM_TARGET)
    }

    @Test
    fun `The room switching under a focused Undo resizes it in the next frame, focus kept`() {
        show(undo = true, room = false)
        rule.mainClock.autoAdvance = false
        rule.onNodeWithContentDescription(UNDO).requestFocus()
        rule.mainClock.advanceTimeByFrame()

        darkRoom = true
        rule.mainClock.advanceTimeByFrame()

        val undo = controlSize(UNDO)
        assertAtLeast(DARK_ROOM_TARGET, undo.width)
        assertAtLeast(DARK_ROOM_TARGET, undo.height)
        rule.onNodeWithContentDescription(UNDO).assertIsFocused()
    }
}
