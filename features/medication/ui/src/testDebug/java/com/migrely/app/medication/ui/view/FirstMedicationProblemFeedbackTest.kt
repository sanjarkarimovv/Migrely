package com.migrely.app.medication.ui.view

import androidx.activity.ComponentActivity
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.requestFocus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val ADD_AND_LOG = "Add and log"

/**
 * Log medication's first-use form after "Add and log" finds a problem in Name (BRD D191; ux F1 to F12), in the normal
 * page and in the dark room of attack mode: the same mechanism as Edit medication.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h891dp")
class FirstMedicationProblemFeedbackTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val screen = ProblemFeedbackHarness(rule, FakeDiary())
    private val name get() = rule.onNode(nameField)

    private fun addAndLog() {
        rule.onNode(hasScrollAction()).performScrollToNode(hasText(ADD_AND_LOG))
        rule.onNodeWithText(ADD_AND_LOG).performClick()
    }

    private fun announcesOnceAndAgain(darkRoom: Boolean) {
        screen.showFirstUse(darkRoom)

        addAndLog()
        screen.awaitNode(announcement(ENTER_A_NAME))
        rule.mainClock.advanceTimeBy(PAST_ANNOUNCEMENT_MILLIS)
        rule.waitForIdle()
        assertFalse(screen.exists(announcement(ENTER_A_NAME)))

        addAndLog()

        screen.awaitNode(announcement(ENTER_A_NAME))
        assertTrue(screen.diary.medications.isEmpty())
    }

    private fun comesIntoView(darkRoom: Boolean) {
        screen.showFirstUse(darkRoom)
        name.performClick()
        screen.keyboard(KEYBOARD_DP)
        rule.onNode(hasScrollAction()).performScrollToNode(hasText(ADD_AND_LOG))
        assertFalse("the name is already in view", screen.isInView(name))

        rule.onNodeWithText(ADD_AND_LOG).performClick()
        screen.awaitProblem(ENTER_A_NAME)
        rule.waitForIdle()

        assertTrue("the name is not in view", screen.isInView(name))
    }

    @Test
    fun `An empty name is announced once, and again on the next Add and log`() =
        announcesOnceAndAgain(darkRoom = false)

    @Test
    fun `In the dark room an empty name is announced once, and again`() =
        announcesOnceAndAgain(darkRoom = true)

    @Test
    fun `Name carries the real error`() {
        screen.showFirstUse()

        addAndLog()
        screen.awaitProblem(ENTER_A_NAME)

        assertEquals(
            ENTER_A_NAME,
            name.fetchSemanticsNode().config.getOrNull(SemanticsProperties.Error)
        )
    }

    // A shorter window than the class's, so that Add and log in view leaves Name out of it above the keyboard.
    @Test
    @Config(sdk = [35], qualifiers = "w411dp-h700dp")
    fun `With the keyboard up and the page at Add and log, the name comes into view`() =
        comesIntoView(darkRoom = false)

    @Test
    @Config(sdk = [35], qualifiers = "w411dp-h700dp")
    fun `In the dark room the name comes into view`() = comesIntoView(darkRoom = true)

    @Test
    fun `With a hardware keyboard, Enter on Add and log moves focus to the name`() {
        screen.touch = false
        screen.showFirstUse()
        rule.onNodeWithContentDescription("Back").requestFocus()
        repeat(FOCUS_STOPS) {
            if (rule.focusedName() != ADD_AND_LOG) rule.press(Key.Tab)
        }
        assertEquals(ADD_AND_LOG, rule.focusedName())

        rule.press(Key.Enter)
        screen.awaitProblem(ENTER_A_NAME)
        rule.waitForIdle()

        assertEquals(listOf(name.fetchSemanticsNode().id), screen.focusedNodes().map { it.id })
    }

    private companion object {
        const val FOCUS_STOPS = 10
    }
}
