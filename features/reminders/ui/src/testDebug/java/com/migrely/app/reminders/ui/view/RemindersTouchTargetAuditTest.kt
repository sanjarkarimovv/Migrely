package com.migrely.app.reminders.ui.view

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.reminders.ui.model.RemindersUiModel
import com.migrely.app.reminders.ui.model.RemindersViewStateUiModel
import com.migrely.app.test.talkback.auditTouchTargets
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private val TODAY = LocalDate.of(2026, 11, 19)
private val VISIT = TODAY.plusDays(3)

/**
 * Touch-target audit of Reminders and Today's visit card before exact bounds land on them (#255, design D5; lead decision 2026-11-19, option
 * 1): every control inside a page is 48 dp of its own, at 360 and 411 dp wide and 100% and 200% font.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class RemindersTouchTargetAuditTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val screens: Map<String, @Composable () -> Unit> = mapOf(
        "Reminders" to {
            MigrelyTheme {
                RemindersContents(
                    viewState = RemindersViewStateUiModel.Ready(
                        RemindersUiModel(
                            dailyEnabled = true,
                            dailyMinuteOfDay = 1200,
                            effectEnabled = true,
                            notificationsAllowed = true,
                            mayAskPermission = true,
                            todayEpochDay = TODAY.toEpochDay(),
                            nextVisitEpochDay = VISIT.toEpochDay(),
                            visitRemovable = true,
                            visitChannelOff = false
                        )
                    ),
                    actions = RemindersActions({}, {}, {}, {}, {}, {}),
                    messages = {}
                )
            }
        },
        "Today's visit card" to {
            MigrelyTheme {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    VisitCardContents(VISIT, onReport = {}, onClose = {})
                }
            }
        }
    )

    private fun underBar(fontScale: Float) = rule.auditTouchTargets(fontScale, screens)

    @Test
    @Config(sdk = [35], application = Application::class, qualifiers = "w360dp-h800dp")
    fun `360 dp, 100 percent`() = assertEquals(emptyList<String>(), underBar(1f))

    @Test
    @Config(sdk = [35], application = Application::class, qualifiers = "w411dp-h891dp")
    fun `411 dp, 100 percent`() = assertEquals(emptyList<String>(), underBar(1f))

    @Test
    @Config(sdk = [35], application = Application::class, qualifiers = "w360dp-h800dp")
    fun `360 dp, 200 percent`() = assertEquals(emptyList<String>(), underBar(2f))

    @Test
    @Config(sdk = [35], application = Application::class, qualifiers = "w411dp-h891dp")
    fun `411 dp, 200 percent`() = assertEquals(emptyList<String>(), underBar(2f))
}
