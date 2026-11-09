package com.migrely.app.subscription.ui.view

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.subscription.ui.model.PremiumNoteUiModel
import com.migrely.app.subscription.ui.model.PremiumPlanUiModel
import com.migrely.app.subscription.ui.model.PremiumSectionUiModel
import com.migrely.app.subscription.ui.model.PremiumStatusUiModel
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val OFFLINE_RESTORE =
    "You're offline. Connect to the internet to restore purchases. Your diary works offline."
private const val OFFLINE_MANAGE =
    "You're offline. Connect to the internet to manage your subscription. Your diary works offline."

private val POLITE = SemanticsMatcher.expectValue(
    SemanticsProperties.LiveRegion,
    LiveRegionMode.Polite
)
private val YEARLY = PremiumStatusUiModel.Subscribed(PremiumPlanUiModel.Yearly, null)

/** Settings' and the trial banner's offline notes (BRD D166): their text, polite, and spoken again. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class OfflineNotesTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `Given Restore or Manage offline then Settings' note says so, politely`() {
        var note by mutableStateOf(PremiumNoteUiModel.OfflineToRestore)
        rule.setContent {
            MigrelyTheme {
                PremiumSectionContents(
                    section = PremiumSectionUiModel(YEARLY, note),
                    checking = false,
                    actions = PremiumSectionActions({}, {}, {}),
                    noteNumber = 1
                )
            }
        }

        rule.onNodeWithText(OFFLINE_RESTORE).assertIsDisplayed().assert(POLITE)
        note = PremiumNoteUiModel.OfflineToManage
        rule.onNodeWithText(OFFLINE_MANAGE).assertIsDisplayed().assert(POLITE)
    }

    @Test
    fun `Given the same note again then it is a new node, which TalkBack speaks again`() {
        var number by mutableIntStateOf(1)
        rule.setContent {
            MigrelyTheme {
                PremiumSectionContents(
                    section = PremiumSectionUiModel(YEARLY, PremiumNoteUiModel.OfflineToManage),
                    checking = false,
                    actions = PremiumSectionActions({}, {}, {}),
                    noteNumber = number
                )
            }
        }
        val first = rule.onNodeWithText(OFFLINE_MANAGE).fetchSemanticsNode().id

        number = 2

        assertNotEquals(first, rule.onNodeWithText(OFFLINE_MANAGE).fetchSemanticsNode().id)
    }

    @Test
    fun `Given Manage offline then the trial banner says so under Manage, politely`() {
        var note by mutableStateOf<Int?>(null)
        rule.setContent {
            MigrelyTheme {
                TrialBannerContents(
                    endsToday = true,
                    renews = true,
                    offlineNote = note,
                    onManage = {},
                    onDismiss = {}
                )
            }
        }
        rule.onNodeWithText(OFFLINE_MANAGE).assertDoesNotExist()

        note = 1
        rule.onNodeWithText(OFFLINE_MANAGE).assertIsDisplayed().assert(POLITE)
        val first = rule.onNodeWithText(OFFLINE_MANAGE).fetchSemanticsNode().id

        note = 2
        assertNotEquals(first, rule.onNodeWithText(OFFLINE_MANAGE).fetchSemanticsNode().id)
    }
}
