package com.migrely.app.designsystem.button

import android.os.SystemClock
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.migrely.app.designsystem.icon.MigrelyIcons
import com.migrely.app.designsystem.theme.MigrelyDarkRoom
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val ROW = "row"
private const val MATERIAL = "material"
private const val MIGRELY = "migrely"

// The platform ripple runs on the render thread in real time, not on the test's clock: well past its press animation.
private const val SETTLED_MILLIS = 1_500L

// Early in that animation, for the control.
private const val EARLY_MILLIS = 50L

/**
 * Pressed, the icon button draws as Material's (#255, lead decision 2026-11-19, option 1), in light, dark and dark
 * room. Both are pressed in one injected frame at the same point of their 48 dp slot and captured in one image once the
 * ripple has settled, so their ripples are in step. Robolectric draws no platform ripple, so this runs on a device.
 */
@RunWith(AndroidJUnit4::class)
class MigrelyIconButtonPressedTest {
    @get:Rule
    val rule = createComposeRule()

    private fun show(theme: @Composable (@Composable () -> Unit) -> Unit) = rule.setContent {
        theme {
            Row(Modifier.testTag(ROW)) {
                IconButton(onClick = {}, modifier = Modifier.testTag(MATERIAL)) {
                    Icon(MigrelyIcons.Close, contentDescription = "Close")
                }
                MigrelyIconButton(
                    onClick = {},
                    contentDescription = "Close",
                    modifier = Modifier.testTag(MIGRELY)
                ) {
                    Icon(MigrelyIcons.Close, contentDescription = "Close")
                }
            }
        }
    }

    /** The pixels of the row's left half (Material) that differ from its right half (Migrely). */
    private fun halvesDiffer(row: ImageBitmap): Int {
        val pixels = row.toPixelMap()
        val half = row.width / 2
        return (0 until half).sumOf { x ->
            (0 until row.height).count { y ->
                pixels[x, y] !=
                    pixels[x + half, y]
            }
        }
    }

    private fun differing(one: ImageBitmap, other: ImageBitmap): Int {
        val first = one.toPixelMap()
        val second = other.toPixelMap()
        return (0 until one.width).sumOf { x ->
            (0 until one.height).count { y ->
                first[x, y] !=
                    second[x, y]
            }
        }
    }

    /** Both buttons pressed in one injected frame, at the same point of their slot, given as a share of its size. */
    private fun pressBoth(at: (Float) -> Offset) {
        val points = listOf(MATERIAL, MIGRELY).map { tag ->
            val bounds = rule.onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot
            bounds.topLeft + at(bounds.width)
        }
        rule.onRoot().performTouchInput {
            points.forEachIndexed { pointer, point -> down(pointer, point) }
        }
    }

    private fun capture() = rule.onNodeWithTag(ROW).captureToImage()

    private fun settledPressedRow(at: (Float) -> Offset): ImageBitmap {
        val rest = capture()
        pressBoth(at)
        SystemClock.sleep(SETTLED_MILLIS)
        rule.waitForIdle()
        val pressed = capture()
        // A check that can fail: the press must show at all.
        assertNotEquals(0, differing(rest, pressed))
        return pressed
    }

    private val middle: (Float) -> Offset = { size -> Offset(size / 2, size / 2) }

    @Test
    fun pressedInTheMiddleDrawsAsMaterialInLight() {
        show { content -> MigrelyTheme(dark = false, content = content) }
        assertEquals(0, halvesDiffer(settledPressedRow(middle)))
    }

    @Test
    fun pressedInTheMiddleDrawsAsMaterialInDark() {
        show { content -> MigrelyTheme(dark = true, content = content) }
        assertEquals(0, halvesDiffer(settledPressedRow(middle)))
    }

    @Test
    fun pressedInTheMiddleDrawsAsMaterialInTheDarkRoom() {
        show { content -> MigrelyTheme { MigrelyDarkRoom(content = content) } }
        assertEquals(0, halvesDiffer(settledPressedRow(middle)))
    }

    @Test
    fun pressedOffCentreDrawsAsMaterial() {
        show { content -> MigrelyTheme(content = content) }
        assertEquals(
            0,
            halvesDiffer(
                settledPressedRow { size ->
                    Offset(size * 0.3f, size * 0.4f)
                }
            )
        )
    }

    /** Control: early in the ripple the row is not yet what it settles to, so the wait above is what the check needs. */
    @Test
    fun earlyInThePressTheRowIsNotYetSettled() {
        show { content -> MigrelyTheme(content = content) }
        pressBoth(middle)
        SystemClock.sleep(EARLY_MILLIS)
        val early = capture()
        SystemClock.sleep(SETTLED_MILLIS)
        rule.waitForIdle()
        assertNotEquals(0, differing(early, capture()))
    }
}
