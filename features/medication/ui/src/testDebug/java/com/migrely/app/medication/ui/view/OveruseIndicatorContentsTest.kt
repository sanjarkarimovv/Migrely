package com.migrely.app.medication.ui.view

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.medication.ui.model.OveruseCategoryUiModel
import com.migrely.app.medication.ui.model.OveruseIndicatorRowUiModel
import com.migrely.app.medication.ui.model.OveruseIndicatorUiModel
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val TITLE = "Last 30 days"
private const val LINE = "Taking medication this often, month after month, can make headaches " +
    "more frequent. It's worth talking to your doctor."
private const val WATCH = "Doctors watch for"

/**
 * A reached row and the line under the rows always show together, and no number shows before it is
 * reached: that pairing is what passed the medical check (BRD D151). The text comes from the real
 * resources and is compared whole, so a changed word fails here and goes back to the medical check.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w400dp-h1200dp")
class OveruseIndicatorContentsTest {
    @get:Rule
    val rule = createComposeRule()

    private fun setRows(vararg rows: OveruseIndicatorRowUiModel) = rule.setContent {
        MigrelyTheme { OveruseIndicatorContents(OveruseIndicatorUiModel(rows.toList())) }
    }

    private fun assertLineCount(count: Int) = rule.onAllNodesWithText(LINE).assertCountEquals(count)

    private fun assertNoNumberShown() =
        rule.onAllNodesWithText(WATCH, substring = true).assertCountEquals(0)

    @Test
    fun `Given one day below the number then the count only, with no line`() {
        setRows(row(OveruseCategoryUiModel.SimplePainkiller, days = 14, limit = 15))

        rule.onNodeWithText("Simple painkillers: 14 days").assertIsDisplayed()
        rule.onAllNodesWithText("15", substring = true).assertCountEquals(0)
        assertNoNumberShown()
        assertLineCount(0)
    }

    @Test
    fun `Given one day then the singular, with no line`() {
        setRows(row(OveruseCategoryUiModel.Triptan, days = 1, limit = 10))

        rule.onNodeWithText("Triptans: 1 day").assertIsDisplayed()
        assertNoNumberShown()
        assertLineCount(0)
    }

    @Test
    fun `Given each category below its number then its name and count only`() {
        setRows(
            row(OveruseCategoryUiModel.SimplePainkiller, days = 7, limit = 15),
            row(OveruseCategoryUiModel.Triptan, days = 5, limit = 10),
            row(OveruseCategoryUiModel.CombinationOrOther, days = 3, limit = 10),
            row(OveruseCategoryUiModel.Multiple, days = 9, limit = 10)
        )

        rule.onNodeWithText("Simple painkillers: 7 days").assertIsDisplayed()
        rule.onNodeWithText("Triptans: 5 days").assertIsDisplayed()
        rule.onNodeWithText("Combination or other: 3 days").assertIsDisplayed()
        rule.onNodeWithText("All attack medication: 9 days").assertIsDisplayed()
        assertNoNumberShown()
        assertLineCount(0)
    }

    @Test
    fun `Given exactly the number then the row states it, followed by the line`() {
        setRows(row(OveruseCategoryUiModel.Triptan, days = 10, limit = 10))

        rule.onNodeWithText("Triptans: 10 days. Doctors watch for 10 or more days a month.")
            .assertIsDisplayed()
        assertLineCount(1)
    }

    @Test
    fun `Given each category past its number then name and count, then the threshold`() {
        val cases = listOf(
            row(OveruseCategoryUiModel.SimplePainkiller, days = 16, limit = 15) to
                "Simple painkillers: 16 days. Doctors watch for 15 or more days a month.",
            row(OveruseCategoryUiModel.Triptan, days = 11, limit = 10) to
                "Triptans: 11 days. Doctors watch for 10 or more days a month.",
            row(OveruseCategoryUiModel.CombinationOrOther, days = 12, limit = 10) to
                "Combination or other: 12 days. Doctors watch for 10 or more days a month.",
            row(OveruseCategoryUiModel.Multiple, days = 16, limit = 10) to
                "All attack medication: 16 days. Doctors watch for 10 or more days a month."
        )
        setRows(*cases.map { it.first }.toTypedArray())

        cases.forEach { (_, text) -> rule.onNodeWithText(text).assertIsDisplayed() }
        assertLineCount(1)
    }

    @Test
    fun `Given a reached row among others then only it has a number, and the line is last`() {
        setRows(
            row(OveruseCategoryUiModel.SimplePainkiller, days = 7, limit = 15),
            row(OveruseCategoryUiModel.Triptan, days = 5, limit = 10),
            row(OveruseCategoryUiModel.Multiple, days = 12, limit = 10)
        )

        rule.onNodeWithText("Simple painkillers: 7 days").assertIsDisplayed()
        rule.onNodeWithText("Triptans: 5 days").assertIsDisplayed()
        val reached = "All attack medication: 12 days. Doctors watch for 10 or more days a month."
        rule.onNodeWithText(reached).assertIsDisplayed()
        rule.onAllNodesWithText(WATCH, substring = true).assertCountEquals(1)
        assertLineCount(1)
        assertTrue(topOf(LINE) > topOf(reached))
    }

    @Test
    fun `Given no rows then nothing shows`() {
        setRows()

        rule.onAllNodesWithText(TITLE).assertCountEquals(0)
        assertLineCount(0)
    }

    private fun topOf(text: String) =
        rule.onNodeWithText(text).fetchSemanticsNode().boundsInRoot.top

    private fun row(category: OveruseCategoryUiModel, days: Int, limit: Int) =
        OveruseIndicatorRowUiModel(category, days, limit, reached = days >= limit)
}
