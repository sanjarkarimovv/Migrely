package com.migrely.app.settings.ui.view

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.test.withKeyDown
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.settings.ui.model.SettingsUiModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val SHARE = "Share Migrely"
private const val RATE = "Rate Migrely on Google Play"
private const val SECTION = "Share and rate"

/** The "Share and rate" section of Settings (BRD SET-F2, D185, EC-SHR-03, 05, 07, 12, 13). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class ShareAndRateTest {
    @get:Rule
    val rule = createComposeRule()

    private var settings by mutableStateOf<SettingsUiModel?>(null)
    private val opened = mutableListOf<String>()
    private var now = 10_000L

    private fun show(guard: OpeningGuard = OpeningGuard(300L) { now }) = rule.setContent {
        MigrelyTheme {
            SettingsContents(
                settings = settings,
                actions = PREVIEW_SETTINGS_ACTIONS.copy(
                    onShare = { opened += SHARE },
                    onRate = { opened += RATE }
                ),
                messages = {},
                openingGuard = guard
            )
        }
    }

    private fun row(title: String) = rule.onNodeWithText(title, substring = true)

    @Test
    fun `While Settings loads the section is absent`() {
        show()

        row(SECTION).assertDoesNotExist()
        row(SHARE).assertDoesNotExist()
        row(RATE).assertDoesNotExist()
    }

    @Test
    fun `During an attack the header and both rows are absent`() {
        settings = PREVIEW_SETTINGS.copy(showShareAndRate = false)
        show()

        row(SECTION).assertDoesNotExist()
        row(SHARE).assertDoesNotExist()
        row(RATE).assertDoesNotExist()
    }

    @Test
    fun `With no attack each row is one button node named by its title, with its summary and action`() {
        settings = PREVIEW_SETTINGS
        show()

        row(SECTION).performScrollTo()
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
        listOf(
            Triple(
                SHARE,
                "Send someone the link to Migrely. Your entries aren't included.",
                "choose an app to share with"
            ),
            Triple(RATE, "Opens Migrely's store page, outside the app", "open Google Play")
        ).forEach { (title, summary, action) ->
            val node = row(title).performScrollTo()
                .assert(hasText(summary))
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
                .fetchSemanticsNode()
            assertEquals(title, node.config[SemanticsProperties.Text].first().text)
            assertEquals(action, node.config[SemanticsActions.OnClick].label)
        }
    }

    @Test
    fun `The section comes after Version and before Delete all data, and Warning signs stays first in Help`() {
        settings = PREVIEW_SETTINGS
        show()

        fun top(text: String) = row(text).fetchSemanticsNode().positionInRoot.y
        assertTrue(top("Help") < top("Warning signs"))
        assertTrue(top("Warning signs") < top("About Migrely"))
        assertTrue(top("Version") < top(SECTION))
        assertTrue(top(SECTION) < top(SHARE))
        assertTrue(top(SHARE) < top(RATE))
        assertTrue(top(RATE) < top("Delete all data"))
    }

    @Test
    fun `A quick double tap on Share opens one share sheet`() {
        settings = PREVIEW_SETTINGS
        show()

        row(SHARE).performScrollTo().performClick()
        now += 100
        row(SHARE).performClick()

        assertEquals(listOf(SHARE), opened)
    }

    @Test
    fun `Share then Rate in a burst opens only the share sheet, and TalkBack's double tap counts too`() {
        settings = PREVIEW_SETTINGS
        show()

        row(SHARE).performScrollTo().performSemanticsAction(SemanticsActions.OnClick)
        now += 100
        row(RATE).performScrollTo().performSemanticsAction(SemanticsActions.OnClick)

        assertEquals(listOf(SHARE), opened)
    }

    @Test
    fun `Without the guard the same double tap opens twice (negative control)`() {
        settings = PREVIEW_SETTINGS
        show(guard = OpeningGuard(timeoutMillis = 0L) { now })

        row(SHARE).performScrollTo().performClick()
        now += 100
        row(SHARE).performClick()

        assertEquals(listOf(SHARE, SHARE), opened)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `A held Enter on Rate opens one Google Play page`() {
        settings = PREVIEW_SETTINGS
        show()

        row(RATE).performScrollTo().requestFocus()
        row(RATE).performKeyInput {
            withKeyDown(Key.Enter) { advanceEventTime(2_000L) }
        }

        assertEquals(listOf(RATE), opened)
    }

    @Test
    fun `When the section leaves with keyboard focus on a row then focus goes to Open-source licences`() {
        settings = PREVIEW_SETTINGS
        show()
        row(SHARE).performScrollTo().requestFocus()
        row(SHARE).assertIsFocused()

        settings = PREVIEW_SETTINGS.copy(showShareAndRate = false)
        rule.mainClock.advanceTimeBy(1_000L)

        row("Open-source licences").assertIsFocused()
        row(SHARE).assertDoesNotExist()
    }
}
