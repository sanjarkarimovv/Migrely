package com.migrely.app.designsystem.dialog

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.isPopup
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** More frames than the menu's opening or closing takes (Material's spring, about 0.3 s). */
private const val MAXIMUM_FRAMES = 120

/**
 * The menu tells when its window is gone (issue #179, BRD D199): once, in the first frame with no popup after it closed,
 * or when it left the screen while open; never while it opens, stays open or is still closing.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MigrelyDropdownMenuClosedTest {
    @get:Rule
    val composeRule = createComposeRule()

    private var expanded by mutableStateOf(false)
    private var shown by mutableStateOf(true)
    private var closed by mutableIntStateOf(0)

    @Before
    fun show() {
        composeRule.setContent {
            if (shown) {
                Box {
                    Text("Tag")
                    MigrelyDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        onClosed = { closed++ }
                    ) {
                        DropdownMenuItem(text = { Text("Remove") }, onClick = { expanded = false })
                    }
                }
            }
        }
        composeRule.waitForIdle()
    }

    private fun isMenuUp() = composeRule.onAllNodes(isPopup(), useUnmergedTree = true)
        .fetchSemanticsNodes().isNotEmpty()

    private fun frame() {
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.waitForIdle()
    }

    private fun openAndSettle() {
        composeRule.runOnUiThread { expanded = true }
        repeat(MAXIMUM_FRAMES) { frame() }
        assertTrue("the menu opened", isMenuUp())
    }

    @Test
    fun `It tells once, in the first frame its window is gone`() {
        assertEquals("never shown", 0, closed)
        openAndSettle()
        assertEquals("open", 0, closed)
        composeRule.mainClock.autoAdvance = false

        composeRule.runOnUiThread { expanded = false }
        var frames = 0
        while (isMenuUp() && frames++ < MAXIMUM_FRAMES) {
            assertEquals("still closing, frame $frames", 0, closed)
            frame()
        }

        assertTrue("the menu closed, after $frames frames", !isMenuUp() && frames > 1)
        assertEquals("told in the first frame with no popup", 1, closed)
        repeat(MAXIMUM_FRAMES) { frame() }
        assertEquals("told once", 1, closed)
    }

    @Test
    fun `It tells when it leaves the screen while open`() {
        openAndSettle()

        composeRule.runOnUiThread { shown = false }
        composeRule.waitForIdle()

        assertTrue("the menu went with its anchor", !isMenuUp())
        assertEquals(1, closed)
    }
}
