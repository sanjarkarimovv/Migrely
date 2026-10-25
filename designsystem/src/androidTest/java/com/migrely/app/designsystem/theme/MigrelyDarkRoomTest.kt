package com.migrely.app.designsystem.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Dark rooms in the light theme (change add-settings-and-safety): each one shown counts once, while it is active and
 * until it leaves, so the app keeps the system bars dark exactly then; inside, the dark scheme and its text colour.
 */
@RunWith(AndroidJUnit4::class)
class MigrelyDarkRoomTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val screens = DarkScreens()
    private var outerShown by mutableStateOf(true)
    private var outerActive by mutableStateOf(true)
    private var innerShown by mutableStateOf(true)
    private var innerActive by mutableStateOf(true)
    private var innerBackground = Color.Unspecified
    private var innerContentColor = Color.Unspecified

    @Before
    fun setUp() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalDarkScreens provides screens) {
                MigrelyTheme(dark = false) {
                    if (outerShown) {
                        MigrelyDarkRoom(active = outerActive) {
                            if (innerShown) {
                                MigrelyDarkRoom(active = innerActive) {
                                    val background = MaterialTheme.colorScheme.background
                                    val contentColor = LocalContentColor.current
                                    SideEffect {
                                        innerBackground = background
                                        innerContentColor = contentColor
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun change(block: () -> Unit) {
        composeTestRule.runOnIdle(block)
        composeTestRule.waitForIdle()
    }

    @Test
    fun nestedRooms_countEach_andLeavingTakesThemOff() {
        assertEquals(2, screens.shown)

        change { innerShown = false }
        assertEquals(1, screens.shown)

        change { innerShown = true }
        assertEquals(2, screens.shown)

        change { outerShown = false }
        assertEquals(0, screens.shown)
    }

    @Test
    fun anInactiveRoom_isNotCounted_andTurningItOnCountsItOnce() {
        change { innerActive = false }
        assertEquals(1, screens.shown)

        change { innerActive = true }
        assertEquals(2, screens.shown)

        change { innerActive = false }
        change { outerShown = false }
        assertEquals(0, screens.shown)
    }

    @Test
    fun insideARoom_theDarkScheme_andItsTextColour() {
        assertEquals(Background, innerBackground)
        assertEquals(OnBackground, innerContentColor)
    }

    @Test
    fun anInactiveRoomInADarkOne_staysDark() {
        change { innerActive = false }

        // The room around it is dark, so the theme around the inactive room is the dark one.
        assertEquals(Background, innerBackground)
    }

    @Test
    fun inactiveRoomsInTheLightTheme_countNothing_andKeepTheLightScheme() {
        change {
            outerActive = false
            innerActive = false
        }

        assertEquals(0, screens.shown)
        assertEquals(LightBackground, innerBackground)
    }
}
