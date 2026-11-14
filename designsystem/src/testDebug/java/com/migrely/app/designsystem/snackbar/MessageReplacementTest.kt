package com.migrely.app.designsystem.snackbar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val BENEATH = "beneath"
private val TALL = 200.dp
private val SHORT = 60.dp

// The host's own 8 dp above the message.
private val HOST_TOP = 8.dp
private const val FRAMES_TO_FINISH_A_TAP = 3

/**
 * A message replaced by a shorter or a taller one (BRD D178 (4), (5), ux F1, F2): it swaps in place; what follows its
 * bottom eases up over 250 ms when it is shorter and moves at once when it is taller; and for 0.8 s the place of the
 * taller of the two swallows touches.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w400dp-h800dp")
class MessageReplacementTest {
    @get:Rule
    val rule = createComposeRule()

    /** A message as tall as [height]. */
    private data class Posted(val height: Dp, val number: Int)

    private var message by mutableStateOf<Posted?>(Posted(TALL, 1))
    private var reportedBottom = 0.dp
    private var tapsBeneath = 0

    private fun show(reduceMotion: Boolean = false) {
        rule.setContent {
            MigrelyTheme {
                Box(Modifier.fillMaxSize()) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .testTag(BENEATH)
                            .clickable { tapsBeneath++ }
                    )
                    MigrelySnackbarHost(
                        message = message,
                        reduceMotion = reduceMotion,
                        onMessageBottomChanged = { reportedBottom = it }
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(it.height)
                        )
                    }
                }
            }
        }
        rule.waitForIdle()
        rule.mainClock.autoAdvance = false
    }

    /** The bottom reported in each of the next [frames] frames after [next] replaces the message. */
    private fun bottomsAfter(next: Posted, frames: Int = 30): List<Dp> {
        message = next
        Snapshot.sendApplyNotifications()
        return (1..frames).map {
            rule.mainClock.advanceTimeByFrame()
            reportedBottom
        }
    }

    private fun bottomOf(height: Dp): Dp = HOST_TOP + height

    @Test
    fun `A shorter replacement eases the bottom up over 250 ms`() {
        show()

        val bottoms = bottomsAfter(Posted(SHORT, 2))

        // Still near the old bottom in the first frame, between the two later on, and at the new one by the end.
        assertTrue("first ${bottoms.first()}", bottoms.first() > bottomOf(TALL) - 20.dp)
        assertTrue(
            "eases: $bottoms",
            bottoms.any {
                it < bottomOf(TALL) - 20.dp &&
                    it > bottomOf(SHORT) + 20.dp
            }
        )
        assertEquals(bottomOf(SHORT).value, bottoms.last().value, 1f)
        assertEquals(bottoms.sortedDescending(), bottoms)
    }

    @Test
    fun `A taller replacement moves the bottom in the same frame`() {
        message = Posted(SHORT, 1)
        show()

        val bottoms = bottomsAfter(Posted(TALL, 2), frames = 1)

        assertEquals(bottomOf(TALL).value, bottoms.single().value, 1f)
    }

    @Test
    fun `With reduce motion a shorter replacement moves the bottom at once`() {
        show(reduceMotion = true)

        val bottoms = bottomsAfter(Posted(SHORT, 2), frames = 1)

        assertEquals(bottomOf(SHORT).value, bottoms.single().value, 1f)
    }

    @Test
    fun `Soon after a shorter replacement, a tap where the taller one was reaches nothing`() {
        show()
        // Inside the old message, below the new one: where an error's Dismiss sits at 200% (ux F2).
        val oldStrip = Offset(200f, with(rule.density) { (HOST_TOP + TALL - 20.dp).toPx() })

        message = Posted(SHORT, 2)
        Snapshot.sendApplyNotifications()
        rule.mainClock.advanceTimeByFrame()
        rule.mainClock.advanceTimeBy(300)
        rule.onNodeWithTag(BENEATH).performTouchInput { click(oldStrip) }
        repeat(FRAMES_TO_FINISH_A_TAP) { rule.mainClock.advanceTimeByFrame() }

        assertEquals(0, tapsBeneath)
    }

    @Test
    fun `Long after a shorter replacement, that tap reaches what lies under it`() {
        show()
        val oldStrip = Offset(200f, with(rule.density) { (HOST_TOP + TALL - 20.dp).toPx() })

        message = Posted(SHORT, 2)
        Snapshot.sendApplyNotifications()
        rule.mainClock.advanceTimeByFrame()
        // The guard ends; under Robolectric its delay runs about twice as long (MessagePlaceAndTapTest).
        rule.mainClock.advanceTimeBy(3_000)
        rule.onNodeWithTag(BENEATH).performTouchInput { click(oldStrip) }
        repeat(FRAMES_TO_FINISH_A_TAP) { rule.mainClock.advanceTimeByFrame() }

        assertEquals(1, tapsBeneath)
    }
}
