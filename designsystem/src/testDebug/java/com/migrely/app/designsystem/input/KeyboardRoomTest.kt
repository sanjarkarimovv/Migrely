package com.migrely.app.designsystem.input

import androidx.activity.ComponentActivity
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val PAGE = 600.dp
private val CONTENT = 1500.dp
private val NAVIGATION_BAR = 48.dp
private val KEYBOARD = 300.dp

/**
 * A page whose visible part ends at the keyboard keeps its scroll offset when the keyboard closes (#238): a pulled
 * page is reported as a scroll, and TalkBack then moves its focus off the field to the first item it sees.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w400dp-h800dp")
class KeyboardRoomTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val scroll = ScrollState(0)
    private var bottom by mutableStateOf(KEYBOARD)

    // Read when laid out, as the keyboard's insets are: the object stays, its value moves.
    private val insets = object : WindowInsets {
        override fun getLeft(density: Density, layoutDirection: LayoutDirection) = 0

        override fun getTop(density: Density) = 0

        override fun getRight(density: Density, layoutDirection: LayoutDirection) = 0

        override fun getBottom(density: Density) = with(density) { bottom.roundToPx() }
    }

    private fun showPage() {
        rule.setContent {
            val room = rememberKeyboardRoom(scroll, insets)
            Column(
                Modifier
                    .height(PAGE)
                    .windowInsetsPadding(insets)
                    .verticalScroll(scroll)
                    .then(room.modifier)
            ) {
                Box(Modifier.height(CONTENT))
            }
        }
        rule.waitForIdle()
    }

    // Pads the bottom by what [bottom] reads when laid out, as a row below the page does.
    private fun Modifier.padBottom(bottom: Density.() -> Int) = layout { measurable, constraints ->
        val padding = bottom()
        val placeable = measurable.measure(constraints.offset(vertical = -padding))
        layout(placeable.width, placeable.height + padding) { placeable.place(0, 0) }
    }

    /**
     * A page whose visible part a layout ends, as Delete all data's row does (BRD D180 (8)): padded by a value, not an
     * inset, and the room told that value.
     */
    private fun showPageEndedByARow() {
        val rowBottom: Density.() -> Int = { bottom.roundToPx() }
        rule.setContent {
            val room = rememberKeyboardRoom(scroll, rowBottom)
            Column(
                Modifier
                    .height(PAGE)
                    .padBottom(rowBottom)
                    .verticalScroll(scroll)
                    .then(room.modifier)
            ) {
                Box(Modifier.height(CONTENT))
            }
        }
        rule.waitForIdle()
    }

    private fun scrollToEnd() {
        runBlocking { scroll.scrollTo(scroll.maxValue) }
        rule.waitForIdle()
    }

    private fun px(dp: Dp) = with(rule.density) { dp.roundToPx() }

    @Test
    fun closingTheKeyboardAtThePageEndKeepsTheOffset() {
        showPage()
        scrollToEnd()
        val before = scroll.value

        bottom = NAVIGATION_BAR
        rule.waitForIdle()

        assertEquals(before, scroll.value)
    }

    @Test
    fun theRoomGoesAsThePageIsScrolledBack() {
        showPage()
        scrollToEnd()
        bottom = NAVIGATION_BAR
        rule.waitForIdle()

        runBlocking { scroll.scrollTo(0) }
        rule.waitForIdle()

        assertEquals(px(CONTENT) - (px(PAGE) - px(NAVIGATION_BAR)), scroll.maxValue)
    }

    @Test
    fun reopeningTheKeyboardKeepsTheOffset() {
        showPage()
        scrollToEnd()
        bottom = NAVIGATION_BAR
        rule.waitForIdle()
        val before = scroll.value

        bottom = KEYBOARD
        rule.waitForIdle()

        assertEquals(before, scroll.value)
    }

    @Test
    fun aPageWithoutAKeyboardHasNoRoom() {
        bottom = NAVIGATION_BAR
        showPage()

        assertEquals(px(CONTENT) - (px(PAGE) - px(NAVIGATION_BAR)), scroll.maxValue)
    }

    @Test
    fun closingTheKeyboardOnAPageEndedByARowKeepsTheOffset() {
        showPageEndedByARow()
        scrollToEnd()
        val before = scroll.value

        bottom = NAVIGATION_BAR
        rule.waitForIdle()

        assertEquals(before, scroll.value)
    }

    @Test
    fun theRoomOfAPageEndedByARowGoesAsThePageIsScrolledBack() {
        showPageEndedByARow()
        scrollToEnd()
        bottom = NAVIGATION_BAR
        rule.waitForIdle()

        runBlocking { scroll.scrollTo(0) }
        rule.waitForIdle()

        assertEquals(px(CONTENT) - (px(PAGE) - px(NAVIGATION_BAR)), scroll.maxValue)
    }

    /**
     * From [below] px short of the page's end, closes the keyboard through [steps] (the visible part's padding at each
     * frame): the offset holds at every step. Returns the scroll after.
     */
    private fun closeWithBelow(below: Int, steps: List<Dp>): Int {
        showPage()
        runBlocking { scroll.scrollTo(scroll.maxValue - below) }
        rule.waitForIdle()
        val before = scroll.value
        steps.forEach { step ->
            bottom = step
            rule.waitForIdle()
            assertEquals("offset with the visible part padded by $step", before, scroll.value)
        }
        return before
    }

    @Test
    fun closingShortOfTheEndAddsOnlyWhatTheEndNeeds() {
        // Less than a keyboard below: the room is the keyboard's place less what lay below (ux R1).
        val before = closeWithBelow(BELOW_SHORT, listOf(NAVIGATION_BAR))

        assertEquals("the room went past the page's end", before, scroll.maxValue)
    }

    @Test
    fun closingShortOfTheEndInStepsKeepsTheOffset() {
        val steps = listOf(250.dp, 200.dp, 150.dp, 100.dp, NAVIGATION_BAR)
        val before = closeWithBelow(BELOW_SHORT, steps)

        assertEquals("the room went past the page's end", before, scroll.maxValue)
    }

    @Test
    fun closingFarFromTheEndAddsNoRoom() {
        closeWithBelow(px(KEYBOARD) + px(NAVIGATION_BAR), listOf(NAVIGATION_BAR))

        assertEquals(px(CONTENT) - (px(PAGE) - px(NAVIGATION_BAR)), scroll.maxValue)
    }

    private companion object {
        // Short of the page's end by less than the keyboard's place, and by no whole number of the 50 dp steps, so a
        // step outgrows what lies below and only the room keeps the offset.
        const val BELOW_SHORT = 70
    }
}
