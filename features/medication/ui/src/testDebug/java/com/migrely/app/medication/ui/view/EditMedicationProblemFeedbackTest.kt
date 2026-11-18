package com.migrely.app.medication.ui.view

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.text.TextRange
import java.util.concurrent.CountDownLatch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val SAVE = "Save"
private const val DUPLICATE = "You already have Ibuprofen."

/**
 * Edit medication after Save finds a problem in a field (BRD D191, EC-UX-66 to EC-UX-71; ux F1 to F12): the problem
 * is announced once per result, the field reads its own error, typing stays quiet, the field comes into view, and
 * focus moves only where a keyboard is in use.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h891dp")
class EditMedicationProblemFeedbackTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val screen = ProblemFeedbackHarness(rule, FakeDiary(IBUPROFEN_MEDICATION))
    private val name get() = rule.onNode(nameField)
    private val dose get() = rule.onNode(doseField)

    private fun save() {
        rule.onNode(hasScrollAction()).performScrollToNode(hasText(SAVE))
        rule.onNodeWithText(SAVE).performClick()
    }

    private fun scrollValue(): Float = rule.onNode(hasScrollAction()).fetchSemanticsNode()
        .config[SemanticsProperties.VerticalScrollAxisRange].value()

    // 2.1 Announced once per result (F5, F7)

    @Test
    fun `An empty name is announced once, and again on the next Save`() {
        screen.showEdit()

        save()
        screen.awaitNode(announcement(ENTER_A_NAME))
        assertEquals(1, rule.onAllNodes(announcement(ENTER_A_NAME)).fetchSemanticsNodes().size)
        rule.mainClock.advanceTimeBy(PAST_ANNOUNCEMENT_MILLIS)
        rule.waitForIdle()
        assertFalse("the announcement stays a stop", screen.exists(announcement(ENTER_A_NAME)))

        save()

        screen.awaitNode(announcement(ENTER_A_NAME))
    }

    // 2.2 The field reads its real error, as one stop (F6)

    @Test
    fun `Name says its real error once, and only the announcement says it besides`() {
        screen.showEdit()

        save()
        screen.awaitProblem(ENTER_A_NAME)

        val field = name.fetchSemanticsNode()
        assertEquals(ENTER_A_NAME, field.config.getOrNull(SemanticsProperties.Error))
        // Read as "Name, edit box, error: Enter a name.": the visible line is not read a second time (ux F6).
        val saying = rule.onAllNodes(hasText(ENTER_A_NAME, substring = true)).fetchSemanticsNodes()
        assertEquals(emptyList<Int>(), saying.map { it.id })
        screen.awaitNode(announcement(ENTER_A_NAME))
        val spoken = rule.onNode(announcement(ENTER_A_NAME)).fetchSemanticsNode()
        assertFalse(spoken.config.contains(SemanticsActions.RequestFocus))
        assertFalse(spoken.config.contains(SemanticsActions.OnClick))
        assertFalse(spoken.config.contains(SemanticsProperties.Focused))
    }

    // 2.3 Typing is quiet (F8)

    @Test
    fun `Typing after the error adds no live region`() {
        screen.showEdit()
        save()
        screen.awaitProblem(ENTER_A_NAME)

        name.performTextInput("Ib")
        rule.waitForIdle()

        val live = screen.allNodes().filter { it.config.contains(SemanticsProperties.LiveRegion) }
        assertEquals(emptyList<String>(), live.map { it.config.toString() })
    }

    // 2.4 The field comes into view (F2)

    @Test
    fun `With the keyboard up and the page at Save, the name comes into view above the keyboard`() {
        screen.showEdit()
        name.performClick()
        screen.keyboard(KEYBOARD_DP)
        rule.onNode(hasScrollAction()).performScrollToNode(hasText(SAVE))
        assertFalse("the name is already in view", screen.isInView(name))

        rule.onNodeWithText(SAVE).performClick()
        screen.awaitProblem(ENTER_A_NAME)
        rule.waitForIdle()

        assertTrue("the name is not in view", screen.isInView(name))
    }

    @Test
    @Config(sdk = [35], qualifiers = "w411dp-h480dp")
    fun `With no keyboard and nothing focused, the name comes into view`() {
        screen.showEdit()
        rule.onNode(hasScrollAction()).performScrollToNode(hasText(SAVE))
        assertFalse("the name is already in view", screen.isInView(name))

        rule.onNodeWithText(SAVE).performClick()
        screen.awaitProblem(ENTER_A_NAME)
        rule.waitForIdle()

        assertTrue("the name is not in view", screen.isInView(name))
        assertEquals(emptyList<String>(), screen.focusedLabels())
    }

    @Test
    fun `A name already in view does not move the page`() {
        screen.showEdit()
        assertTrue(screen.isInView(name))
        val before = scrollValue()

        rule.onNodeWithText(SAVE).performClick()
        screen.awaitProblem(ENTER_A_NAME)
        rule.waitForIdle()

        assertEquals(before, scrollValue(), 0.5f)
    }

    // 2.5 Focus and keyboard (F3, F4)

    @Test
    fun `With the keyboard up in the dose, focus moves to the name with the cursor at the end`() {
        screen.showEdit()
        name.performTextInput(IBUPROFEN_NAME)
        name.performTextInputSelection(TextRange.Zero)
        dose.performClick()
        screen.keyboard(KEYBOARD_DP)

        save()
        screen.awaitProblem(DUPLICATE)
        rule.waitForIdle()

        val focused = screen.focusedNodes()
        assertEquals(listOf(name.fetchSemanticsNode().id), focused.map { it.id })
        assertEquals(
            TextRange(IBUPROFEN_NAME.length),
            name.fetchSemanticsNode().config[SemanticsProperties.TextSelectionRange]
        )
    }

    @Test
    fun `With no keyboard, focus does not move`() {
        screen.showEdit()

        save()
        screen.awaitProblem(ENTER_A_NAME)
        rule.waitForIdle()

        assertEquals(emptyList<String>(), screen.focusedLabels())
    }

    @Test
    fun `With the keyboard up in the name, focus stays there`() {
        screen.showEdit()
        name.performClick()
        screen.keyboard(KEYBOARD_DP)

        save()
        screen.awaitProblem(ENTER_A_NAME)
        rule.waitForIdle()

        assertEquals(listOf(name.fetchSemanticsNode().id), screen.focusedNodes().map { it.id })
    }

    // 2.6 Pending and one field (F1, F12)

    @Test
    fun `Two quick presses save once and announce once`() {
        screen.showEdit()
        name.performTextInput(IBUPROFEN_NAME)
        val release = CountDownLatch(1)
        screen.diary.holdLookup = release

        rule.onNodeWithText(SAVE).performClick()
        screen.waitFor { screen.diary.lookups.isNotEmpty() }
        rule.onNodeWithText(SAVE).performClick()
        release.countDown()
        screen.awaitNode(announcement(DUPLICATE))
        rule.waitForIdle()

        assertEquals(1, screen.diary.lookups.size)
        assertEquals(1, rule.onAllNodes(announcement(DUPLICATE)).fetchSemanticsNodes().size)
    }

    @Test
    fun `An empty name shows its error on the name only`() {
        screen.showEdit()
        dose.performTextInput("400 mg")

        save()
        screen.awaitProblem(ENTER_A_NAME)

        val errors = rule.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Error))
            .fetchSemanticsNodes()
        assertEquals(listOf(name.fetchSemanticsNode().id), errors.map { it.id })
    }
}
