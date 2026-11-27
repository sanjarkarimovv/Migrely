package com.migrely.app.designsystem.dialog

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.isPopup
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.migrely.app.designsystem.input.drawEdgeToEdge
import com.migrely.app.designsystem.input.setInsets
import com.migrely.app.designsystem.theme.MigrelyTheme
import kotlin.math.roundToInt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val ANCHOR = "anchor"
private const val TAG_NAME = "Jaw pain"
private const val REMOVE = "Remove from my list"
private const val FRAMES_TO_SETTLE = 30
private const val MANY_ITEMS = 8

/** How far down the window the chip's top sits, as QA's `n1` sweep placed it. */
private const val AT_20 = 0.2f
private const val AT_40 = 0.4f
private const val AT_60 = 0.6f
private const val AT_80 = 0.8f

/** 1080 × 900 px and 1080 × 700 px at 420 dpi, QA's `n1rep` windows for #179; and the full phone. */
private const val WINDOW_900 = "w411dp-h343dp-420dpi"
private const val WINDOW_700 = "w411dp-h267dp-420dpi"
private const val WINDOW_FULL = "w411dp-h914dp-420dpi"

/** QA's API 36 Gboard cell in the 360 dp landscape window at 200% (#245): status bar and the keyboard's top, px. */
private const val WINDOW_360_LAND = "w800dp-h360dp-land-420dpi"
private const val STATUS_PX = 63
private const val KEYBOARD_TOP_360_PX = 318

private val MARGIN = 8.dp
private val ONE_ITEM = 48.dp
private val CHIP_START = 24.dp

/**
 * Where the popup menu opens (BRD EC-UX-91, D199 (1), #179): below its chip when it fits above the safe bottom edge
 * with 8 dp, else above with 8 dp, else on the larger side, capped, its content scrolling. It never covers its chip,
 * in any frame of its opening. The chip has `ChoiceChip`'s size at 200%; the item is the real one. Robolectric places
 * the popup's window where its position provider says, which the bounds below read; it draws no system bars, so a
 * test that needs them gives them as insets.
 *
 * Whether "no room for one item on either side" can happen in a supported window: yes. In the 360 dp tall landscape
 * window at 200% with Gboard up (QA's cell: status bar 63 px, keyboard top 318 px), the safe band less both margins is
 * about 81 dp and the chip about 57 dp, which leaves about 12 dp a side, less than a 48 dp item; the last test proves
 * it and what the menu does then: a strip of about 12 dp that scrolls, off its chip and off the keyboard. Without a
 * keyboard, every window here leaves room on one side.
 */
@RunWith(RobolectricTestRunner::class)
// Real text sizes: the legacy mode measures text as almost nothing.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], fontScale = 2f)
class MigrelyDropdownMenuPlacementTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private var expanded by mutableStateOf(false)
    private var chipTopPx by mutableStateOf(0f)
    private var direction by mutableStateOf(LayoutDirection.Ltr)
    private var material by mutableStateOf(false)
    private var items by mutableIntStateOf(1)

    private fun windowHeight() = rule.activity.window.decorView.height

    private fun show(fraction: Float, edgeToEdge: Boolean = false) {
        if (edgeToEdge) rule.runOnUiThread { rule.activity.drawEdgeToEdge() }
        rule.setContent {
            MigrelyTheme {
                CompositionLocalProvider(LocalLayoutDirection provides direction) {
                    // Not in the Surface's own content, which would pass the window's size to the chip.
                    Surface(Modifier.fillMaxSize()) {
                        Box(Modifier.fillMaxSize()) { ChipWithMenu() }
                    }
                }
            }
        }
        rule.waitForIdle()
        assertEquals("the font is at 200%", 2f, rule.density.fontScale)
        placeChipAt(windowHeight() * fraction)
    }

    private fun placeChipAt(topPx: Float) {
        rule.runOnUiThread { chipTopPx = topPx }
        rule.waitForIdle()
    }

    @Composable
    private fun ChipWithMenu() {
        // The page reads the keyboard's inset, as Attack details does: Compose takes insets given after a reader is.
        WindowInsets.ime.getBottom(LocalDensity.current)
        Box(
            Modifier.offset {
                IntOffset(CHIP_START.roundToPx(), chipTopPx.roundToInt())
            }.testTag(ANCHOR)
        ) {
            // `ChoiceChip`'s size: 48 dp at least, 16 and 12 dp around its label.
            Surface(Modifier.heightIn(min = 48.dp)) {
                Text(
                    TAG_NAME,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
            val onDismiss = { expanded = false }
            if (material) {
                DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) { Items() }
            } else {
                MigrelyDropdownMenu(expanded = expanded, onDismissRequest = onDismiss) { Items() }
            }
        }
    }

    @Composable
    private fun Items() = repeat(items) { DropdownMenuItem(text = { Text(REMOVE) }, onClick = {}) }

    private fun popup(): SemanticsNode? = rule.onAllNodes(isPopup(), useUnmergedTree = true)
        .fetchSemanticsNodes().firstOrNull()

    private fun chip() =
        rule.onNodeWithTag(ANCHOR, useUnmergedTree = true).fetchSemanticsNode().screenRect()

    private fun frames(count: Int, eachFrame: () -> Unit = {}) = repeat(count) {
        rule.mainClock.advanceTimeByFrame()
        rule.waitForIdle()
        eachFrame()
    }

    /** Opens the menu; [eachFrame] checks every frame of the opening. Returns the settled menu's bounds. */
    private fun open(eachFrame: (menu: Rect) -> Unit = {}): Rect {
        rule.mainClock.autoAdvance = false
        rule.runOnUiThread { expanded = true }
        frames(FRAMES_TO_SETTLE) { popup()?.let { eachFrame(it.screenRect()) } }
        return checkNotNull(popup()) { "the menu did not open" }.screenRect()
    }

    private fun close() {
        rule.runOnUiThread { expanded = false }
        frames(FRAMES_TO_SETTLE)
        assertEquals("the menu did not close", null, popup())
    }

    private fun px(dp: Dp) = with(rule.density) { dp.toPx() }

    private fun assertApart(where: String, menu: Rect, chip: Rect) = assertTrue(
        "$where: the menu $menu covers its chip $chip",
        menu.top >= chip.bottom || menu.bottom <= chip.top
    )

    private fun assertNeverCovers(fraction: Float) {
        show(fraction)
        val chip = chip()
        val where = "${(fraction * 100).roundToInt()}% down"

        val menu = open { frame -> assertApart("$where, opening", frame, chip) }

        assertApart(where, menu, chip)
    }

    @Test
    @Config(qualifiers = WINDOW_900, fontScale = 2f)
    fun `At 20 percent of a 900 px window the menu does not cover its chip`() =
        assertNeverCovers(AT_20)

    /**
     * QA's `n1rep` case (2 of 2 on a device). It is no negative control here: on a device Material's menu takes the
     * window's visible frame, less the status and navigation bars, while Robolectric's popup sees the whole window, so
     * develop's menu fits below here. The device walk runs develop's build first (task 5.3 (a)).
     */
    @Test
    @Config(qualifiers = WINDOW_900, fontScale = 2f)
    fun `At 40 percent of a 900 px window the menu does not cover its chip`() =
        assertNeverCovers(AT_40)

    @Test
    @Config(qualifiers = WINDOW_900, fontScale = 2f)
    fun `At 60 percent of a 900 px window the menu does not cover its chip`() =
        assertNeverCovers(AT_60)

    @Test
    @Config(qualifiers = WINDOW_900, fontScale = 2f)
    fun `At 80 percent of a 900 px window the menu does not cover its chip`() =
        assertNeverCovers(AT_80)

    @Test
    @Config(qualifiers = WINDOW_700, fontScale = 2f)
    fun `At 20 percent of a 700 px window the menu does not cover its chip`() =
        assertNeverCovers(AT_20)

    /**
     * The negative control: develop's menu covers the chip here, by Material's fallback to the anchor's middle, the
     * failure QA measured at 900 px on a device.
     */
    @Test
    @Config(qualifiers = WINDOW_700, fontScale = 2f)
    fun `At 40 percent of a 700 px window the menu does not cover its chip`() =
        assertNeverCovers(AT_40)

    @Test
    @Config(qualifiers = WINDOW_700, fontScale = 2f)
    fun `At 60 percent of a 700 px window the menu does not cover its chip`() =
        assertNeverCovers(AT_60)

    @Test
    @Config(qualifiers = WINDOW_700, fontScale = 2f)
    fun `At 80 percent of a 700 px window the menu does not cover its chip`() =
        assertNeverCovers(AT_80)

    @Test
    @Config(qualifiers = WINDOW_900, fontScale = 2f)
    fun `Where it fits below it opens right under the chip at its start edge`() {
        show(AT_20)
        val chip = chip()

        val menu = open()

        assertEquals(chip.bottom, menu.top, 1f)
        assertEquals(chip.left, menu.left, 1f)
    }

    @Test
    @Config(qualifiers = WINDOW_900, fontScale = 2f)
    fun `Where it fits only above it opens right over the chip`() {
        show(AT_80)
        val chip = chip()

        val menu = open()

        assertEquals(chip.top, menu.bottom, 1f)
    }

    @Test
    @Config(qualifiers = WINDOW_700, fontScale = 2f)
    fun `Where it fits neither side it opens on the larger side, capped, and scrolls`() {
        items = MANY_ITEMS
        show(AT_20)
        val chip = chip()
        val below = windowHeight() - px(MARGIN) - chip.bottom
        val above = chip.top - px(MARGIN)

        val menu = open()

        assertTrue("the larger side below is used", below > above)
        assertEquals(chip.bottom, menu.top, 1f)
        assertEquals("capped to the side", below, menu.height, 1f)
        val scrolls = rule.onAllNodes(isPopup(), useUnmergedTree = true).fetchSemanticsNodes()
            .flatMap { it.allDescendants() }
            .any { it.config.getOrNull(SemanticsProperties.VerticalScrollAxisRange) != null }
        assertTrue("the capped menu scrolls", scrolls)
    }

    /** Scenario "A full window is unchanged": where Material's menu opens under the chip, the helper's matches it. */
    @Test
    @Config(qualifiers = WINDOW_FULL, fontScale = 2f)
    fun `In a full window the menu opens where Material's opens`() {
        show(AT_20)
        listOf(AT_20, AT_40, AT_60).forEach { fraction ->
            placeChipAt(windowHeight() * fraction)
            val chip = chip()
            material = true
            val materials = open()
            close()
            assertEquals("Material's menu opens under the chip", chip.bottom, materials.top, 1f)

            material = false
            assertEquals("${(fraction * 100).roundToInt()}% down", materials, open())
            close()
        }
    }

    @Test
    @Config(qualifiers = WINDOW_900, fontScale = 2f)
    fun `An open keyboard counts as the window's bottom`() {
        show(AT_40, edgeToEdge = true)
        val keyboard = px(150.dp).roundToInt()
        rule.runOnUiThread { rule.activity.setInsets(keyboardPx = keyboard, statusPx = STATUS_PX) }
        rule.waitForIdle()
        val chip = chip()
        val keyboardTop = windowHeight() - keyboard

        val menu = open { frame -> assertApart("opening over a keyboard", frame, chip) }

        assertApart("over a keyboard", menu, chip)
        assertTrue(
            "the menu $menu reaches the keyboard at $keyboardTop",
            menu.bottom <= keyboardTop
        )
    }

    @Test
    @Config(qualifiers = WINDOW_900, fontScale = 2f)
    fun `Right to left the menu aligns to the chip's start edge`() {
        direction = LayoutDirection.Rtl
        show(AT_20)
        val chip = chip()

        val menu = open()

        assertEquals(chip.right, menu.right, 1f)
        assertEquals(chip.bottom, menu.top, 1f)
    }

    /**
     * "No room for one item on either side" is reachable: QA's 360 dp landscape cell at 200% with Gboard up. The menu
     * still opens on the larger side, never on its chip, never under the keyboard or the status bar.
     */
    @Test
    @Config(qualifiers = WINDOW_360_LAND, fontScale = 2f)
    fun `With no room for one item on either side the menu keeps off its chip and the keyboard`() {
        show(fraction = 0f, edgeToEdge = true)
        rule.runOnUiThread {
            rule.activity.setInsets(
                keyboardPx = windowHeight() - KEYBOARD_TOP_360_PX,
                statusPx = STATUS_PX
            )
        }
        rule.waitForIdle()
        val chipHeight = chip().height
        // The chip in the middle of what the keyboard leaves, its best place.
        placeChipAt((STATUS_PX + KEYBOARD_TOP_360_PX - chipHeight) / 2f)
        val chip = chip()
        val largerSide = maxOf(
            KEYBOARD_TOP_360_PX - px(MARGIN) - chip.bottom,
            chip.top - STATUS_PX - px(MARGIN)
        )
        assertTrue(
            "reachable: the larger side has ${largerSide / rule.density.density} dp, less than one item",
            largerSide < px(ONE_ITEM)
        )

        val menu = open { frame -> assertApart("opening with no room", frame, chip) }

        assertApart("no room", menu, chip)
        assertEquals("a strip as tall as the larger side", largerSide, menu.height, 1f)
        assertTrue("the menu $menu reaches under the keyboard", menu.bottom <= KEYBOARD_TOP_360_PX)
        assertTrue("the menu $menu reaches under the status bar", menu.top >= STATUS_PX)
    }
}

private fun SemanticsNode.screenRect() = Rect(positionOnScreen, size.toSize())

private fun SemanticsNode.allDescendants(): List<SemanticsNode> =
    children.flatMap { listOf(it) + it.allDescendants() }
