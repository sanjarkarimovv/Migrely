package com.migrely.app.report.ui.view

import android.app.Application
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.report.ui.model.ReportOptionsUiModel
import com.migrely.app.report.ui.model.ReportPeriodUiModel
import com.migrely.app.report.ui.model.ReportViewStateUiModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The PDF stays English in 1.4.0, so the report screen says so in one line when the app's language is not English, and
 * shows no such line in English (BRD D195, EC-L10N-09).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class ReportPdfLanguageNoteTest {
    @get:Rule
    val rule = createComposeRule()

    private fun showReport() {
        rule.setContent {
            MigrelyTheme {
                ReportContents(
                    viewState = ReportViewStateUiModel.Ready(
                        ReportOptionsUiModel(20_660, 20_751, anythingLogged = true)
                    ),
                    period = ReportPeriodUiModel.Default,
                    includeNotes = false,
                    actions = ReportActions({}, {}, {}, {}, {}, {}),
                    messages = {}
                )
            }
        }
    }

    @Test
    @Config(qualifiers = "de-rDE")
    fun `Given German then the screen says the PDF is in English`() {
        showReport()

        rule.onNodeWithText("Das PDF ist auf Englisch.").assertExists()
    }

    @Test
    @Config(qualifiers = "en-rUS")
    fun `Given English then no line about the PDF's language`() {
        showReport()

        rule.onAllNodesWithText("The PDF is in English.").assertCountEquals(0)
        rule.onNodeWithText("Create report").assertExists()
    }
}
