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
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
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
}
