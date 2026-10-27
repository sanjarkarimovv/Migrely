package com.migrely.app.designsystem.input

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TapGuardTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private var buttonClicks = 0
    private var textButtonClicks = 0
    private var selections = 0
    private var guardKey by mutableStateOf(1)

    @Before
    fun setUp() {
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            MigrelyTheme {
                Box {
                    Column {
                        Button(onClick = { buttonClicks++ }) { Text("Button") }
                        TextButton(onClick = { textButtonClicks++ }) { Text("Link") }
                        var selected by mutableStateOf(false)
                        Text(
                            "Chip",
                            Modifier.selectable(selected = selected) {
                                selected = true
                                selections++
                            }
                        )
                    }
                    TapGuard(key = guardKey, durationMillis = 1_000L, Modifier.matchParentSize())
                }
            }
        }
        composeTestRule.mainClock.advanceTimeByFrame()
    }

    private fun tapAll() {
        listOf("Button", "Link", "Chip").forEach {
            composeTestRule.onNodeWithText(it).performClick()
        }
        composeTestRule.mainClock.advanceTimeByFrame()
    }

    @Test
    fun tapsWithinTheGuardDoNothing() {
        composeTestRule.mainClock.advanceTimeBy(900L)

        tapAll()

        assertEquals(listOf(0, 0, 0), listOf(buttonClicks, textButtonClicks, selections))
    }

    @Test
    fun tapsAfterTheGuardWork() {
        composeTestRule.mainClock.advanceTimeBy(1_100L)

        tapAll()

        assertEquals(listOf(1, 1, 1), listOf(buttonClicks, textButtonClicks, selections))
    }

    @Test
    fun aNewKeyArmsTheGuardAgain() {
        composeTestRule.mainClock.advanceTimeBy(1_100L)
        guardKey = 2
        composeTestRule.mainClock.advanceTimeBy(100L)

        tapAll()

        assertEquals(listOf(0, 0, 0), listOf(buttonClicks, textButtonClicks, selections))
    }
}
