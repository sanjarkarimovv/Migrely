package com.migrely.app.attack.ui.view

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.navigation.NavController
import com.migrely.app.attack.presentation.model.AttackDetailsPresentationNotification
import com.migrely.app.attack.presentation.model.CustomTagPresentationModel
import com.migrely.app.attack.presentation.model.TagKindPresentationModel
import com.migrely.app.attack.ui.mapper.AttackIntakeUiMapper
import com.migrely.app.attack.ui.mapper.AttackNotificationUiMapper
import com.migrely.app.attack.ui.mapper.AttackRangeUiMapper
import com.migrely.app.attack.ui.mapper.CustomTagUiMapper
import com.migrely.app.attack.ui.mapper.TagKindUiMapper
import com.migrely.app.attack.ui.mapper.TimeFieldUiMapper
import com.migrely.app.attack.ui.notification.AttackDetailsEventPresenter
import com.migrely.app.designsystem.snackbar.LocalMessageClock
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val CUSTOM_CHIP = "Jaw pain"
private const val REMOVE = "Remove from my list"
private const val UNDO = "Undo"
private const val UNDONE = "Undone."
private const val LIST_FULL = "Your list is full. Remove one to add another."
private const val SETTLE_MILLIS = 1_000L

/** 40 characters as the app counts them, an emoji and accents: the spoken Undo name carries it whole (spec "Long and unusual names"). */
private const val LONG_NAME = "Kopfschmerz nach Kälte 🧊 und Wind, früh"

private fun tag(id: String, name: String) =
    CustomTagPresentationModel(id, TagKindPresentationModel.Symptom, name, archived = false)

private val JAW_PAIN = tag("j", CUSTOM_CHIP)

/**
 * "Undo" on "Removed from your list" (BRD D199 (2), (3), EC-ATK-26, EC-ATK-27): its spoken name names the tag, a tap
 * hands the tag back to the ViewModel, and the ViewModel's answers show as the screen's messages. The ViewModel is a
 * mock answering as the real one does (`AttackDetailsViewModelTest`), through the real notification mapper.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w400dp-h700dp")
class CustomTagUndoTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val events = AttackDetailsEventPresenter()
    private val dependencies = attackDetailsTestDependencies(events)

    /** The message clocks move with the Compose test clock, as an Undo message times itself by them (D182). */
    private val messageClock by lazy { ComposeMessageClock(rule.mainClock) }
    private val notifications = AttackNotificationUiMapper(
        events,
        TimeFieldUiMapper(),
        AttackRangeUiMapper(),
        AttackIntakeUiMapper(),
        CustomTagUiMapper(TagKindUiMapper())
    )
    private val restored = mutableListOf<CustomTagPresentationModel>()
    private var undoAnswer: AttackDetailsPresentationNotification =
        AttackDetailsPresentationNotification.Undone

    @Before
    fun holdTheClock() {
        rule.mainClock.autoAdvance = false
    }

    /**
     * Every scroll and touch runs here, with the clock running: a scroll waits for its own frames and never ends under
     * a held clock (it once ran a test JVM out of memory for minutes), and Robolectric can drop a tap there
     * (`SaveMessageTest`). The clock is held again right after, as it was, so an Undo message's countdown is not run to
     * its end by the waits. JUnit's timeout rule cannot guard this: it runs the test off Robolectric's main thread.
     */
    private fun <T> withClockRunning(input: () -> T): T {
        val before = rule.mainClock.autoAdvance
        rule.mainClock.autoAdvance = true
        try {
            return input()
        } finally {
            rule.mainClock.autoAdvance = before
        }
    }

    @Before
    fun answer() {
        doAnswer { call ->
            removed(call.getArgument(1))
        }.whenever(dependencies.attackDetailsViewModel).onArchiveCustomTagAction(any(), any())
        doAnswer { call ->
            restored += call.getArgument<CustomTagPresentationModel>(1)
            notifications.toUi(undoAnswer).present()
        }.whenever(dependencies.attackDetailsViewModel).onUndoRemoveCustomTagAction(any(), any())
    }

    /** What the ViewModel sends once [tag] left the list. */
    private fun removed(tag: CustomTagPresentationModel) = notifications.toUi(
        AttackDetailsPresentationNotification.CustomTagRemoved("a", tag)
    ).present()

    private fun show() {
        rule.setContent {
            MigrelyTheme {
                CompositionLocalProvider(LocalMessageClock provides messageClock) {
                    dependencies.AttackDetails(
                        attackId = "a",
                        announceMerge = false,
                        navController = NavController(rule.activity)
                    )
                }
            }
        }
        settle()
    }

    private fun settle() {
        rule.mainClock.advanceTimeBy(SETTLE_MILLIS)
        rule.waitForIdle()
    }

    private fun removeJawPain(actionLabel: String = REMOVE) {
        // The scroll to the chip waits for its own frames.
        val chip = withClockRunning {
            rule.onNode(hasContentDescription(CUSTOM_CHIP)).performScrollTo().fetchSemanticsNode()
        }
        val action = chip.config[SemanticsActions.CustomActions].single { it.label == actionLabel }
        rule.runOnUiThread { action.action() }
        settle()
    }

    private fun tapUndo(spoken: String) {
        // Its click action, as TalkBack and Switch Access use it: no touch, so the clock stays held and the Undo's
        // countdown is not run out while the button is looked for.
        rule.onNodeWithContentDescription(spoken).performSemanticsAction(SemanticsActions.OnClick)
        settle()
    }

    @Test
    fun `The message offers Undo, spoken with the tag's name`() {
        show()

        removeJawPain()

        rule.assertOneSpokenName(spoken = "Undo removing Jaw pain", visible = UNDO)
    }

    @Test
    fun `Undo hands the tag back and Undone shows`() {
        show()
        removeJawPain()

        tapUndo("Undo removing Jaw pain")

        assertEquals(listOf(JAW_PAIN), restored)
        rule.onNodeWithText(UNDONE).assertExists()
    }

    @Test
    fun `Given the list filled up then Undo says the list is full`() {
        undoAnswer = AttackDetailsPresentationNotification.TagLimitReached
        show()
        removeJawPain()

        tapUndo("Undo removing Jaw pain")

        rule.onNodeWithText(LIST_FULL).assertExists()
    }

    @Test
    fun `A second removal replaces the message and Undo brings back only the second tag`() {
        val perfume = tag("p", "Perfume")
        show()
        removeJawPain()

        rule.runOnUiThread { removed(perfume) }
        settle()

        rule.onNodeWithContentDescription("Undo removing Jaw pain").assertDoesNotExist()
        tapUndo("Undo removing Perfume")
        assertEquals(listOf(perfume), restored)
    }

    @Test
    fun `A long name with an emoji and accents is spoken whole`() {
        show()

        rule.runOnUiThread { removed(tag("l", LONG_NAME)) }
        settle()

        // The most a name may be, as the app counts it (`MAXIMUM_TAG_NAME_LENGTH`, UTF-16 units).
        assertEquals(40, LONG_NAME.length)
        rule.assertOneSpokenName(spoken = "Undo removing $LONG_NAME", visible = UNDO)
    }

    @Test
    @Config(qualifiers = "de")
    fun `In German the message, its button and its spoken name read as the spec says`() {
        show()

        removeJawPain(actionLabel = "Aus meiner Liste entfernen")

        rule.onNodeWithText("Aus deiner Liste entfernt").assertExists()
        rule.assertOneSpokenName(
            spoken = "Rückgängig: Jaw pain zurück in die Liste",
            visible = "Rückgängig"
        )
    }
}
