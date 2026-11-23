package com.migrely.app.designsystem.button

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.icon.MigrelyIcons
import com.migrely.app.designsystem.layout.PageScroll
import com.migrely.app.designsystem.theme.MigrelyTheme
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val SLOT = "slot"
private const val MATERIAL = "material"
private const val MIGRELY = "migrely"

/**
 * An icon button inside a page keeps a 48 dp touch area of its own, with no help from the view configuration, which
 * exact bounds set to zero (#255, lead decision 2026-11-19, option 1), and draws as Material's does. Pressed, the
 * ripple is the platform's and Robolectric draws none (Material's own pressed button equals its resting one here), so
 * the pressed comparison is the instrumented `MigrelyIconButtonPressedTest`.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w411dp-h891dp")
class MigrelyIconButtonTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    /** Whether a tap 2 dp inside the 48 dp slot's left edge, on its middle line, reaches [button] inside a page. */
    private fun tapInTheRingActs(button: @Composable (onClick: () -> Unit) -> Unit): Boolean {
        var clicks = 0
        rule.setContent {
            MigrelyTheme {
                PageScroll {
                    Column(Modifier.pageScroll(rememberScrollState())) {
                        Box(Modifier.testTag(SLOT)) { button { clicks++ } }
                    }
                }
            }
        }
        // 22 dp from the centre: 2 dp outside the 40 dp circle Material draws, inside 48 dp.
        val at = with(rule.density) { Offset(2.dp.toPx(), 24.dp.toPx()) }
        rule.onNodeWithTag(SLOT).performTouchInput { click(at) }
        rule.waitForIdle()
        return clicks == 1
    }

    @Test
    fun `A tap in the ring between the drawn circle and 48 dp acts`() {
        assertTrue(
            tapInTheRingActs { onClick ->
                MigrelyIconButton(onClick, contentDescription = "Close") { CloseIcon() }
            }
        )
    }

    @Test
    fun `Control, Material's icon button misses that tap inside a page`() {
        assertFalse(tapInTheRingActs { onClick -> IconButton(onClick) { CloseIcon() } })
    }

    private fun showBoth(enabled: Boolean = true) {
        rule.setContent {
            MigrelyTheme {
                Row {
                    Box(Modifier.testTag(MATERIAL)) {
                        IconButton(onClick = {}, enabled = enabled) { CloseIcon() }
                    }
                    Box(Modifier.testTag(MIGRELY)) {
                        MigrelyIconButton(onClick = {
                        }, contentDescription = "Close", enabled = enabled) {
                            CloseIcon()
                        }
                    }
                }
            }
        }
    }

    // `captureToImage` never returns under Robolectric here (as in TypedLineMarginsTest); the window draws the same.
    private fun image(tag: String): Bitmap {
        val decor = rule.activity.window.decorView
        val window = Bitmap.createBitmap(decor.width, decor.height, Bitmap.Config.ARGB_8888)
        rule.runOnUiThread { decor.draw(Canvas(window)) }
        val bounds = rule.onNodeWithTag(tag).fetchSemanticsNode().boundsInWindow
        return Bitmap.createBitmap(
            window,
            bounds.left.roundToInt(),
            bounds.top.roundToInt(),
            bounds.width.roundToInt(),
            bounds.height.roundToInt()
        )
    }

    /** The pixels that differ between the two buttons, or a size mismatch. */
    private fun difference(): String {
        val material = image(MATERIAL)
        val migrely = image(MIGRELY)
        if (material.width != migrely.width || material.height != migrely.height) {
            return "size ${material.width}x${material.height} vs ${migrely.width}x${migrely.height}"
        }
        var differing = 0
        for (x in 0 until material.width) {
            for (y in 0 until material.height) {
                if (material.getPixel(x, y) != migrely.getPixel(x, y)) differing++
            }
        }
        return "$differing pixels differ"
    }

    @Test
    fun `At rest it draws as Material's icon button`() {
        showBoth()
        assertEquals("0 pixels differ", difference())
    }

    @Test
    fun `Disabled it draws as Material's icon button`() {
        showBoth(enabled = false)
        assertEquals("0 pixels differ", difference())
    }

    @Test
    @Config(sdk = [35], qualifiers = "w411dp-h891dp-420dpi")
    fun `A press lands on the drawn circle where it lands on Material's`() {
        val presses = mutableListOf<Offset>()
        rule.setContent {
            MigrelyTheme {
                CompositionLocalProvider(LocalIndication provides RecordingIndication(presses)) {
                    Box(Modifier.testTag(SLOT)) {
                        MigrelyIconButton(onClick = {
                        }, contentDescription = "Close") { CloseIcon() }
                    }
                }
            }
        }
        // The middle of the 48 dp slot, 126 px at density 2.625. Material's 40 dp circle (105 px) is centred in its 48 dp
        // as Alignment.Center places it, at round(10.5) = 11 px, so its ripple starts at 63 - 11 = 52 px.
        rule.onNodeWithTag(SLOT).performTouchInput {
            down(center)
            up()
        }
        rule.waitForIdle()
        assertEquals(listOf(Offset(52f, 52f)), presses)
    }
}

@Composable
private fun CloseIcon() = Icon(MigrelyIcons.Close, contentDescription = "Close")

/** An indication that only records where each press lands on what it draws. */
private class RecordingIndication(private val presses: MutableList<Offset>) :
    IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode =
        object : Modifier.Node() {
            override fun onAttach() {
                coroutineScope.launch {
                    interactionSource.interactions.collect {
                        if (it is PressInteraction.Press) {
                            presses +=
                                it.pressPosition
                        }
                    }
                }
            }
        }

    override fun equals(other: Any?) = other === this

    override fun hashCode() = System.identityHashCode(this)
}
