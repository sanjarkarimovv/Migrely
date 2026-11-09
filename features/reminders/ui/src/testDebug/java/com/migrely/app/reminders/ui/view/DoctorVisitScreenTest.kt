package com.migrely.app.reminders.ui.view

import android.app.Application
import android.content.Intent
import android.os.Looper
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.reminders.ui.model.RemindersUiModel
import com.migrely.app.reminders.ui.model.RemindersViewStateUiModel
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/** The day the screen was read for; the screen reads no clock of its own (issue #187). */
private val TODAY: LocalDate = LocalDate.of(2026, 11, 10)

/** Next year's November 20, so the row shows a date with its year. */
private val VISIT: LocalDate = LocalDate.of(2027, 11, 20)
private val SPOKEN = "${VISIT.dayOfWeek.name.lowercase().replaceFirstChar(
    Char::titlecase
)}, November 20, ${VISIT.year}"

/**
 * The Reminders screen's "Doctor visit" section (BRD D168, design D9) and home's card (D8), as TalkBack and the eye see
 * them. SDK 35: Robolectric's Android 16 sandbox needs a JDK internals export.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS")
class DoctorVisitScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private var removed = 0

    private fun reminders(
        nextVisit: LocalDate?,
        today: LocalDate = TODAY,
        allowed: Boolean = true,
        channelOff: Boolean = false
    ) = RemindersUiModel(
        dailyEnabled = false,
        dailyMinuteOfDay = 1200,
        effectEnabled = true,
        notificationsAllowed = allowed,
        mayAskPermission = true,
        todayEpochDay = today.toEpochDay(),
        nextVisitEpochDay = nextVisit?.toEpochDay(),
        // As GetRemindersUseCase decides it.
        visitRemovable = nextVisit?.isAfter(today) == true,
        visitChannelOff = channelOff
    )

    private fun show(
        nextVisit: LocalDate?,
        allowed: Boolean = true,
        channelOff: Boolean = false,
        fontScale: Float = 1f
    ) = show(
        mutableStateOf(reminders(nextVisit, allowed = allowed, channelOff = channelOff)),
        fontScale
    )

    private fun show(shown: State<RemindersUiModel>, fontScale: Float = 1f) {
        composeTestRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                MigrelyTheme {
                    RemindersContents(
                        viewState = RemindersViewStateUiModel.Ready(shown.value),
                        actions = RemindersActions(
                            onBack = {},
                            onDaily = {},
                            onDailyTime = {},
                            onEffect = {},
                            onOpenSettings = {},
                            onRemoveVisit = { removed++ }
                        ),
                        messages = {}
                    )
                }
            }
        }
    }

    private fun isHeading() = SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading)

    private fun isButton() = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)

    @Test
    fun givenNoVisit_thenTheSectionIsFirstWithAHeading_andTheRowIsOneButton() {
        show(nextVisit = null)

        composeTestRule.onNode(hasText("Doctor visit") and isHeading()).assertIsDisplayed()
        composeTestRule.onNode(
            hasContentDescription("Next visit, not set. Set a date.") and isButton()
        )
            .assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Remove").assertCountEquals(0)
        val section = composeTestRule.onNodeWithText(
            "Doctor visit"
        ).fetchSemanticsNode().boundsInRoot
        val daily = composeTestRule.onNodeWithText(
            "Daily reminder",
            substring = true
        ).fetchSemanticsNode().boundsInRoot
        assertTrue("the visit section comes first", section.top < daily.top)
    }

    @Test
    fun givenAVisit_thenTheRowReadsItInFull_andRemoveIsLabelled() {
        show(nextVisit = VISIT)

        composeTestRule.onNode(
            hasContentDescription("Next visit, $SPOKEN. Change it.") and isButton()
        )
            .assertIsDisplayed()
        composeTestRule.onNode(
            hasContentDescription("Remove the next visit") and hasClickAction()
        ).performClick()
        assertTrue(removed == 1)
    }

    @Test
    fun givenTheVisitIsToday_thenNoRemove_sinceTodaysVisitStaysAsTheLastOne() {
        show(nextVisit = TODAY)

        composeTestRule.onAllNodesWithText("Remove").assertCountEquals(0)
    }

    /** Issue #187: the state's day decides, not the device's clock, which here is a year before the visit. */
    @Test
    fun givenTheStateIsReadOnTheVisitDay_thenNoRemove_whateverTheDeviceClockSays() {
        show(mutableStateOf(reminders(nextVisit = VISIT, today = VISIT)))

        composeTestRule.onNode(
            hasContentDescription("Next visit, $SPOKEN. Change it.") and isButton()
        ).assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Remove").assertCountEquals(0)
    }

    /** Issue #187: the screen stays open past midnight; the new read takes Remove away, and the row stays. */
    @Test
    fun givenTheScreenStaysOpen_whenTheVisitDayStarts_thenRemoveGoes() {
        val shown = mutableStateOf(reminders(nextVisit = VISIT, today = VISIT.minusDays(1)))
        show(shown)
        composeTestRule.onAllNodesWithText("Remove").assertCountEquals(1)

        shown.value = reminders(nextVisit = VISIT, today = VISIT)

        composeTestRule.onAllNodesWithText("Remove").assertCountEquals(0)
        composeTestRule.onNode(
            hasContentDescription("Next visit, $SPOKEN. Change it.") and isButton()
        ).assertIsDisplayed()
    }

    /** Issue #187: midnight, a time set by hand and a new zone each make the screen read again. */
    @Test
    fun givenTheScreenIsStarted_whenTheDateTimeOrZoneChanges_thenItIsToldEachTime() {
        var changes = 0
        composeTestRule.setContent { ClockChangeEffect { changes++ } }
        val application = RuntimeEnvironment.getApplication()

        listOf(
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED
        ).forEach { application.sendBroadcast(Intent(it)) }
        shadowOf(Looper.getMainLooper()).idle()

        composeTestRule.runOnIdle { assertEquals(3, changes) }
    }

    @Test
    fun givenAVisitAndAppNotificationsOff_thenTheLineWithoutAButton_andNoChannelNotice() {
        show(nextVisit = VISIT, allowed = false, channelOff = true)

        composeTestRule.onNodeWithText("This reminder can show only on Home.").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Doctor visit notifications are off", substring = true)
            .assertCountEquals(0)
        // Only the screen's own notice has "Open settings".
        composeTestRule.onAllNodesWithText("Open settings").assertCountEquals(1)
    }

    @Test
    fun givenAVisitAndOnlyTheChannelOff_thenItsNoticeWithOpenSettings() {
        show(nextVisit = VISIT, channelOff = true)

        composeTestRule.onNodeWithText(
            "Doctor visit notifications are off, so this reminder can show only on Home."
        ).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(
            "Open notification settings"
        ).assertIsDisplayed()
        composeTestRule.onAllNodesWithText(
            "This reminder can show only on Home."
        ).assertCountEquals(0)
    }

    @Test
    fun givenNoVisitAndNotificationsOff_thenNothingUnderTheRow() {
        show(nextVisit = null, allowed = false)

        composeTestRule.onAllNodesWithText(
            "This reminder can show only on Home."
        ).assertCountEquals(0)
    }

    @Test
    fun givenTwiceTheFontSize_thenTheDateGoesUnderTheLabel_andIsNotCut() {
        show(nextVisit = VISIT, fontScale = 2f)

        // Measured here: 86 dp stacked, 56 dp (the row's minimum) side by side; the control without stacking fails.
        val row = composeTestRule.onNode(hasContentDescription("Next visit, $SPOKEN. Change it."))
            .fetchSemanticsNode().boundsInRoot
        val height = with(composeTestRule.density) { row.height.toDp() }
        assertTrue("the row is $height high", height >= 72.dp)
    }

    @Test
    fun theCard_readsTheFullDate_andItsButtonsAreLabelled() {
        var reports = 0
        var closes = 0
        composeTestRule.setContent {
            MigrelyTheme {
                VisitCardContents(VISIT, onReport = { reports++ }, onClose = { closes++ })
            }
        }

        composeTestRule.onNodeWithContentDescription(
            "Your doctor visit is on $SPOKEN."
        ).assertIsDisplayed()
        composeTestRule.onNode(hasText("Make a report") and hasClickAction()).performClick()
        composeTestRule.onNodeWithContentDescription("Close the visit reminder").performClick()
        assertTrue(reports == 1 && closes == 1)
    }
}
