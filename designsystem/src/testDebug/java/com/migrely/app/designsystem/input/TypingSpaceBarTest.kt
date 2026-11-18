package com.migrely.app.designsystem.input

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Insets
import android.view.WindowInsets
import android.view.WindowInsetsAnimation
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.icon.MigrelyIcons
import com.migrely.app.designsystem.layout.MigrelyInsets
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

// QA's API 36 360 dp window (`wm size 945x2100`, landscape): the status bar, and Gboard's landscape keyboard, in px.
private const val STATUS_PX = 63
private const val KEYBOARD_PX = 627

private const val TOP_BAR = "topBar"
private const val BACK = "Back"
private const val TITLE = "Title"
private const val ABOVE = "Above"
private const val HEIGHT_TOLERANCE = 1f

/**
 * The top bar steps aside with the keyboard's own movement and leaves the accessibility tree while aside (BRD D192,
 * EC-UX-76; ux R3, R6, R11; design D2, D3; task 2.3): its height follows the keyboard's inset animation down to the
 * status bar, in one step where the inset jumps, its content keeps its place and colours as it is clipped, and while aside
 * accessibility services do not get Back or the title (Tab: [TypingSpaceFocusTest]).
 */
@OptIn(ExperimentalMaterial3Api::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w800dp-h360dp-land-420dpi", fontScale = 2f)
class TypingSpaceBarTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private var value by mutableStateOf(TextFieldValue(""))

    private fun show() {
        rule.runOnUiThread { rule.activity.drawEdgeToEdge() }
        rule.setContent {
            MigrelyTheme {
                val space = rememberTypingSpace()
                Column(Modifier.fillMaxSize()) {
                    TopAppBar(
                        title = { Text(TITLE) },
                        navigationIcon = {
                            IconButton(onClick = {
                            }) { Icon(MigrelyIcons.Back, contentDescription = BACK) }
                        },
                        windowInsets = MigrelyInsets.bars.only(
                            WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                        ),
                        modifier = Modifier.testTag(TOP_BAR).then(space.barModifier)
                    )
                    Column(
                        Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .windowInsetsPadding(
                                MigrelyInsets.barsAndKeyboard.only(WindowInsetsSides.Bottom)
                            )
                            .then(space.pageModifier)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                    ) {
                        TextButton(onClick = {}) { Text(ABOVE) }
                        ProvideTypingSpace(space) {
                            CursorFollowingTextField(
                                value = value,
                                onValueChange = { value = it },
                                minLines = 3,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(Modifier.height(400.dp))
                    }
                }
            }
        }
        rule.waitForIdle()
        rule.runOnUiThread { rule.activity.setInsets(keyboardPx = 0, statusPx = STATUS_PX) }
        rule.waitForIdle()
        rule.onNode(hasSetTextAction()).requestFocus()
        rule.waitForIdle()
    }

    private fun insets(keyboard: Int): WindowInsets = WindowInsets.Builder()
        .setInsets(WindowInsets.Type.statusBars(), Insets.of(0, STATUS_PX, 0, 0))
        .setVisible(WindowInsets.Type.statusBars(), true)
        .setInsets(WindowInsets.Type.ime(), Insets.of(0, 0, 0, keyboard))
        .setVisible(WindowInsets.Type.ime(), keyboard > 0)
        .build()

    /** The keyboard's own animation from [from] to [to] px, as Android runs it from API 30. */
    private inner class KeyboardAnimation(private val from: Int, private val to: Int) {
        private val decor get() = rule.activity.window.decorView
        private val animation =
            WindowInsetsAnimation(WindowInsets.Type.ime(), null, ANIMATION_MILLIS)

        fun start() {
            rule.runOnUiThread {
                decor.dispatchWindowInsetsAnimationPrepare(animation)
                // Android reports the end state first; androidx keeps it as the target.
                decor.dispatchApplyWindowInsets(insets(to))
                decor.dispatchWindowInsetsAnimationStart(
                    animation,
                    WindowInsetsAnimation.Bounds(Insets.NONE, Insets.of(0, 0, 0, maxOf(from, to)))
                )
            }
            rule.waitForIdle()
        }

        fun progress(fraction: Float) {
            rule.runOnUiThread {
                animation.fraction = fraction
                val keyboard = from + ((to - from) * fraction).toInt()
                decor.dispatchWindowInsetsAnimationProgress(insets(keyboard), listOf(animation))
            }
            rule.waitForIdle()
        }

        fun end() {
            rule.runOnUiThread { decor.dispatchWindowInsetsAnimationEnd(animation) }
            rule.waitForIdle()
        }
    }

    private fun density() = rule.activity.resources.displayMetrics.density

    private fun barHeightPx(): Float {
        val bounds = rule.onNodeWithTag(
            TOP_BAR,
            useUnmergedTree = true
        ).fetchSemanticsNode().boundsInRoot
        return bounds.height
    }

    /**
     * Whether Back or the title is in the merged tree, the one accessibility services are given: the test's unmerged
     * tree still lists what `clearAndSetSemantics` removed for them.
     */
    private fun barInTree(): Boolean =
        rule.onAllNodes(hasContentDescription(BACK)).fetchSemanticsNodes().isNotEmpty() ||
            rule.onAllNodes(hasText(TITLE)).fetchSemanticsNodes().isNotEmpty()

    @Test
    fun `The bar's height follows the keyboard's animation down to the status bar and back`() {
        show()
        val full = barHeightPx()
        val opening = KeyboardAnimation(from = 0, to = KEYBOARD_PX)

        opening.start()
        listOf(0f, 0.25f, 0.5f, 1f).forEach { fraction ->
            opening.progress(fraction)
            val expected = full - (full - STATUS_PX) * fraction
            assertEquals("opening at $fraction", expected, barHeightPx(), HEIGHT_TOLERANCE)
        }
        opening.end()
        assertEquals("open", STATUS_PX.toFloat(), barHeightPx(), HEIGHT_TOLERANCE)

        val closing = KeyboardAnimation(from = KEYBOARD_PX, to = 0)
        closing.start()
        listOf(0f, 0.25f, 0.5f, 1f).forEach { fraction ->
            closing.progress(fraction)
            val expected = STATUS_PX + (full - STATUS_PX) * fraction
            assertEquals("closing at $fraction", expected, barHeightPx(), HEIGHT_TOLERANCE)
        }
        closing.end()
        assertEquals("closed", full, barHeightPx(), HEIGHT_TOLERANCE)
    }

    @Test
    fun `A keyboard that jumps moves the bar in one step`() {
        show()
        val full = barHeightPx()

        rule.runOnUiThread {
            rule.activity.setInsets(keyboardPx = KEYBOARD_PX, statusPx = STATUS_PX)
        }
        rule.waitForIdle()
        assertEquals("open", STATUS_PX.toFloat(), barHeightPx(), HEIGHT_TOLERANCE)

        rule.runOnUiThread { rule.activity.setInsets(keyboardPx = 0, statusPx = STATUS_PX) }
        rule.waitForIdle()
        assertEquals("closed", full, barHeightPx(), HEIGHT_TOLERANCE)
    }

    @Test
    fun `A keyboard changing height while the bar is aside never brings it back`() {
        show()
        rule.runOnUiThread {
            rule.activity.setInsets(keyboardPx = KEYBOARD_PX, statusPx = STATUS_PX)
        }
        rule.waitForIdle()

        val symbols = KeyboardAnimation(from = KEYBOARD_PX, to = KEYBOARD_PX + SYMBOLS_EXTRA_PX)
        symbols.start()
        listOf(0f, 0.5f, 1f).forEach { fraction ->
            symbols.progress(fraction)
            assertEquals(
                "taller keyboard at $fraction",
                STATUS_PX.toFloat(),
                barHeightPx(),
                HEIGHT_TOLERANCE
            )
        }
        symbols.end()
        val letters = KeyboardAnimation(from = KEYBOARD_PX + SYMBOLS_EXTRA_PX, to = KEYBOARD_PX)
        letters.start()
        listOf(0f, 0.5f, 1f).forEach { fraction ->
            letters.progress(fraction)
            assertEquals(
                "shorter keyboard at $fraction",
                STATUS_PX.toFloat(),
                barHeightPx(),
                HEIGHT_TOLERANCE
            )
        }
        letters.end()
    }

    @Test
    fun `The bar's content is clipped in place`() {
        show()
        val full = barHeightPx().toInt()
        val before = drawWindow()
        val opening = KeyboardAnimation(from = 0, to = KEYBOARD_PX)
        opening.start()

        opening.progress(0.5f)
        val shown = barHeightPx().toInt()
        val during = drawWindow()

        assertTrue("the bar did not move: $shown of $full px", shown < full)
        // The drawing is live: the page moved up into what the bar left.
        assertTrue(
            "the page did not move in the drawing",
            (shown until full).any {
                !before.sameRow(during, it)
            }
        )
        // What is still shown of the bar is the same pixels as before: no other colour, no move. Robolectric draws
        // in software, where a layer's alpha is not applied, so this cannot see a fade.
        (0 until shown).forEach { row ->
            assertTrue("row $row of the bar changed", before.sameRow(during, row))
        }
    }

    // `captureToImage` never returns under Robolectric here; the window draws itself the same way.
    private fun drawWindow(): Bitmap {
        val decor = rule.activity.window.decorView
        val bitmap = Bitmap.createBitmap(decor.width, decor.height, Bitmap.Config.ARGB_8888)
        rule.runOnUiThread { decor.draw(Canvas(bitmap)) }
        return bitmap
    }

    private fun Bitmap.sameRow(other: Bitmap, row: Int): Boolean {
        val mine = IntArray(width).also { getPixels(it, 0, width, 0, row, width, 1) }
        val theirs = IntArray(other.width).also {
            other.getPixels(it, 0, other.width, 0, row, other.width, 1)
        }
        return mine.contentEquals(theirs)
    }

    @Test
    fun `While aside the bar is gone from the accessibility tree and back after the keyboard`() {
        show()
        assertTrue("the bar is not in the tree to begin with", barInTree())

        rule.runOnUiThread {
            rule.activity.setInsets(keyboardPx = KEYBOARD_PX, statusPx = STATUS_PX)
        }
        rule.waitForIdle()
        assertFalse("Back or the title is in the tree while the bar is aside", barInTree())

        rule.runOnUiThread { rule.activity.setInsets(keyboardPx = 0, statusPx = STATUS_PX) }
        rule.waitForIdle()
        assertTrue("the bar did not come back to the tree", barInTree())
    }

    private companion object {
        const val ANIMATION_MILLIS = 250L

        // Gboard's symbols and emoji pages are taller than its letters.
        const val SYMBOLS_EXTRA_PX = 80
    }
}
