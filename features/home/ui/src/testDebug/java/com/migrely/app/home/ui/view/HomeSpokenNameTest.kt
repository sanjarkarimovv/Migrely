package com.migrely.app.home.ui.view

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.home.ui.mapper.SkyAppearanceUiMapper
import com.migrely.app.home.ui.model.AttackInProgressUiModel
import com.migrely.app.home.ui.model.IntensityLevelUiModel
import com.migrely.app.home.ui.model.SkyUiModel
import com.migrely.app.home.ui.model.TodayUiModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val ATTACK =
    AttackInProgressUiModel("a", 0L, "Asia/Tashkent", IntensityLevelUiModel.Moderate, false)

/** "Started earlier?" chips: what each shows, and what it says. */
private val MOVE_START = listOf("15 min", "30 min", "1 h", "2 h")

/**
 * Home's controls whose spoken label differs from their text read the label once (BRD D181 (2),
 * EC-UX-45): attack mode's Details and "Started earlier?" chips, and Today's Period chip.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h891dp")
class HomeSpokenNameTest {
    @get:Rule
    val rule = createComposeRule()

    private fun showAttackMode() = rule.setContent {
        MigrelyTheme {
            AttackModeContents(
                attack = ATTACK,
                nowMillis = 0L,
                reduceMotion = true,
                messageBottom = { 0.dp },
                timeUiFormatter = previewFormatter,
                actions = previewActions
            )
        }
    }

    @Test
    fun `In attack mode, Details has one spoken name`() {
        showAttackMode()

        rule.assertOneSpokenName(spoken = "Details of this migraine", visible = "Details")
    }

    @Test
    fun `In attack mode, each Started earlier chip has one spoken name`() {
        showAttackMode()

        MOVE_START.forEach { shown ->
            rule.assertOneSpokenName(spoken = "Move the start $shown earlier", visible = shown)
        }
    }

    @Test
    fun `On Today, the Period chip has one spoken name`() {
        rule.setContent {
            MigrelyTheme {
                TodayContents(
                    today = TodayUiModel(SkyUiModel.Calm, 0, null),
                    dayTitle = "Today",
                    nowMillis = 0L,
                    skyAppearanceUiMapper = SkyAppearanceUiMapper(),
                    timeUiFormatter = previewFormatter,
                    onStartAttack = {},
                    onAttackDetails = {},
                    onAddPastAttack = {}
                )
            }
        }

        rule.assertOneSpokenName(spoken = "Period today", visible = "Period")
        // Its state is still read with it.
        rule.onNodeWithContentDescription("Period today").assert(isSelectable())
    }
}
