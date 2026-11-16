package com.migrely.app.history.ui.view

import android.app.Application
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.test.withKeyDown
import androidx.compose.ui.unit.Density
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.history.ui.model.HistoryAttackUiModel
import com.migrely.app.history.ui.model.HistoryMonthUiModel
import com.migrely.app.history.ui.model.HistoryViewStateUiModel
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val CHOSEN_DAY = 10
private const val ZONE = "Asia/Tashkent"

/**
 * History with a hardware keyboard (BRD D181 (3), EC-UX-46): Shift+Tab from the chosen day's first
 * control lands on the chosen day, fully in view at 200%, and Tab goes back to the control. The
 * first control is a migraine row, Period, or Pain-free when Period is hidden.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "w360dp-h640dp")
class HistoryKeyboardOrderTest {
    @get:Rule
    val rule = createComposeRule()

    private val chosen = LocalDate.of(2026, 10, CHOSEN_DAY)

    private fun month(attack: Boolean = false, periodOffered: Boolean = true): HistoryMonthUiModel {
        val preview = previewMonth()
        val start = chosen.atTime(9, 0).atZone(ZoneId.of(ZONE)).toInstant().toEpochMilli()
        val attacks = if (attack) {
            listOf(HistoryAttackUiModel("a", start, ZONE, start + 3_600_000L, 6))
        } else {
            emptyList()
        }
        return preview.copy(
            days = preview.days.map {
                if (it.epochDay == chosen.toEpochDay()) {
                    it.copy(attacks = attacks, periodOffered = periodOffered)
                } else {
                    it
                }
            }
        )
    }

    private fun show(month: HistoryMonthUiModel, fontScale: Float) = rule.setContent {
        val density = LocalDensity.current
        CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
            MigrelyTheme {
                HistoryContents(
                    viewState = HistoryViewStateUiModel.Ready(month, null),
                    selectedEpochDay = chosen.toEpochDay(),
                    mappers = previewMappers,
                    actions = previewActions,
                    messages = {},
                    overuseIndicator = {}
                )
            }
        }
    }

    private fun press(shift: Boolean) {
        rule.onRoot().performKeyInput {
            if (shift) withKeyDown(Key.ShiftLeft) { pressKey(Key.Tab) } else pressKey(Key.Tab)
        }
        rule.waitForIdle()
    }

    private fun focused(): SemanticsNode? =
        rule.onAllNodes(isFocused()).fetchSemanticsNodes().singleOrNull()

    private fun name(node: SemanticsNode?) =
        node?.config?.getOrNull(SemanticsProperties.ContentDescription)?.firstOrNull()
            ?: node?.config?.getOrNull(SemanticsProperties.Text)?.firstOrNull()?.text

    /** Shift+Tab from [firstControl] lands on the chosen day, in view; Tab goes back. */
    private fun assertShiftTabReachesTheChosenDay(firstControl: SemanticsMatcher) {
        rule.onNode(firstControl and hasClickAction()).requestFocus()
        val control = name(focused())
        assertNotNull("the first control takes focus", control)

        press(shift = true)

        val day = focused()
        assertNotNull("a stop with an accessibility node", day)
        assertNotNull(
            "the chosen day, read with its chosen state: ${name(day)}",
            day?.config?.getOrNull(SemanticsProperties.StateDescription)
        )
        // Unclipped: a day half scrolled out of view has clipped bounds inside the screen.
        val root = rule.onRoot().fetchSemanticsNode().boundsInRoot
        val top = checkNotNull(day).positionInRoot.y
        val bottom = top + day.size.height
        assertTrue("day $top..$bottom inside $root", top >= root.top && bottom <= root.bottom)

        press(shift = false)
        assertEquals(control, name(focused()))
    }

    @Test
    fun `From a migraine row, Shift+Tab lands on the chosen day at 200 percent`() {
        show(month(attack = true), fontScale = 2f)
        assertShiftTabReachesTheChosenDay(hasContentDescription("09:00", substring = true))
    }

    @Test
    fun `From Period, Shift+Tab lands on the chosen day at 200 percent`() {
        show(month(), fontScale = 2f)
        assertShiftTabReachesTheChosenDay(hasText("Period") and isToggleable())
    }

    @Test
    fun `From Pain-free with Period hidden, Shift+Tab lands on the chosen day at 200 percent`() {
        show(month(periodOffered = false), fontScale = 2f)
        assertShiftTabReachesTheChosenDay(hasText("Pain-free day") and isToggleable())
    }

    @Test
    fun `From Period, Shift+Tab lands on the chosen day at 100 percent`() {
        show(month(), fontScale = 1f)
        assertShiftTabReachesTheChosenDay(hasText("Period") and isToggleable())
    }
}
