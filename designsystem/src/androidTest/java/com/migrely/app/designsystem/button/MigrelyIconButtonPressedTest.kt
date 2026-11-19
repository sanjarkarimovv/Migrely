package com.migrely.app.designsystem.button

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.migrely.app.designsystem.icon.MigrelyIcons
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val ROW = "row"
private const val MATERIAL = "material"
private const val MIGRELY = "migrely"

/**
 * Pressed, the icon button draws as Material's (#255, lead decision 2026-11-19, option 1). Both are held down at the
 * same point of their 48 dp slot in the same frame and captured in one image, so their ripples are in step. Robolectric
 * draws no platform ripple, so this runs on a device.
 */
@RunWith(AndroidJUnit4::class)
class MigrelyIconButtonPressedTest {
    @get:Rule
    val rule = createComposeRule()

    private fun show() = rule.setContent {
        MigrelyTheme {
            Row(Modifier.testTag(ROW)) {
                IconButton(onClick = {}, modifier = Modifier.testTag(MATERIAL)) {
                    Icon(MigrelyIcons.Close, contentDescription = "Close")
                }
                MigrelyIconButton(onClick = {}, modifier = Modifier.testTag(MIGRELY)) {
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
            (0 until row.height).count { y -> pixels[x, y] != pixels[x + half, y] }
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

    private fun pressBothAt(at: (Float) -> Offset) {
        listOf(MATERIAL, MIGRELY).forEachIndexed { pointer, tag ->
            rule.onNodeWithTag(tag).performTouchInput { down(pointer, at(width.toFloat())) }
        }
    }

    private fun pressedImage(at: (Float) -> Offset): ImageBitmap {
        show()
        val rest = rule.onNodeWithTag(ROW).captureToImage()
        pressBothAt(at)
        rule.mainClock.advanceTimeBy(PRESS_SETTLES_MILLIS)
        val pressed = rule.onNodeWithTag(ROW).captureToImage()
        // A check that can fail: the press must show at all.
        assertNotEquals(0, differing(rest, pressed))
        return pressed
    }

    @Test
    fun pressedInTheMiddleDrawsAsMaterial() {
        assertEquals(0, halvesDiffer(pressedImage { size -> Offset(size / 2, size / 2) }))
    }

    @Test
    fun pressedOffCentreDrawsAsMaterial() {
        assertEquals(0, halvesDiffer(pressedImage { size -> Offset(size * 0.3f, size * 0.4f) }))
    }

    private companion object {
        const val PRESS_SETTLES_MILLIS = 1000L
    }
}
