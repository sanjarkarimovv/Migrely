package com.migrely.app.report.ui.view

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.report.ui.model.ReportOptionsUiModel
import com.migrely.app.report.ui.model.ReportPeriodUiModel
import com.migrely.app.report.ui.model.ReportViewStateUiModel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val TITLE = "Mention Migrely in the report"
private const val SUMMARY =
    "One small line on the last page: \"Made with Migrely, a personal migraine diary app. " +
        "Not a medical device. migrely.health\""

/** The credit switch on the Doctor report (BRD REP-F5, D186, EC-REP-11–13). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class ReportCreditSwitchTest {
    @get:Rule
    val rule = createComposeRule()

    private var viewState by mutableStateOf<ReportViewStateUiModel>(ReportViewStateUiModel.Loading)
    private val credits = mutableListOf<Boolean>()
    private val created = mutableListOf<Boolean>()

    private fun ready(creditShown: Boolean) = ReportViewStateUiModel.Ready(
        ReportOptionsUiModel(20_660, 20_751, anythingLogged = true, creditShown = creditShown)
    )

    private fun show() = rule.setContent {
        MigrelyTheme {
            ReportContents(
                viewState = viewState,
                period = ReportPeriodUiModel.Default,
                includeNotes = false,
                actions = ReportActions(
                    onBack = {},
                    onPeriod = {},
                    onCustom = {},
                    onNotes = {},
                    onCredit = { credits += it },
                    onCreate = { created += it }
                ),
                messages = {}
            )
        }
    }

    private fun switch() = rule.onNodeWithText(TITLE, substring = true)

    @Test
    fun `While loading the switch is not shown, so it never flips from on to off`() {
        show()

        switch().assertDoesNotExist()
    }

    @Test
    fun `When ready then one switch node with title, quoting summary and state`() {
        viewState = ready(creditShown = true)
        show()

        switch().performScrollTo()
            .assert(hasText(TITLE, substring = true))
            .assert(hasText(SUMMARY, substring = true))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch))
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.ToggleableState,
                    ToggleableState.On
                )
            )
            .assertIsEnabled()
            .assertIsOn()
    }

    @Test
    fun `When tapped then the change is asked for, stored value shown until saved`() {
        viewState = ready(creditShown = true)
        show()

        switch().performScrollTo().performClick()

        assertEquals(listOf(false), credits)
        // Nothing saved yet: a failed save leaves it as it is, never looking changed (EC-REP-12).
        switch().assertIsOn()
    }

    @Test
    fun `While a report is made the switch stays, disabled`() {
        viewState = ready(creditShown = false)
        show()
        rule.waitForIdle()

        viewState = ReportViewStateUiModel.Working

        switch().performScrollTo().assertIsNotEnabled().assertIsOff()
    }

    @Test
    fun `When Create report is tapped then the report takes the switch as shown`() {
        viewState = ready(creditShown = false)
        show()

        rule.onNodeWithText("Create report").performScrollTo().performClick()

        assertEquals(listOf(false), created)
    }
}
