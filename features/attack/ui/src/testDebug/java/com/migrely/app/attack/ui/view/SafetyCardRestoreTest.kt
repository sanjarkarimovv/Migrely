package com.migrely.app.attack.ui.view

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onParent
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.test.core.app.ApplicationProvider
import com.migrely.app.attack.presentation.model.AttackDetailsViewState
import com.migrely.app.attack.presentation.viewmodel.AttackDetailsViewModel
import com.migrely.app.attack.ui.di.AttackDetailsDependencies
import com.migrely.app.attack.ui.mapper.AttackDetailsViewStateUiMapper
import com.migrely.app.attack.ui.model.AttackDetailsEventUiModel
import com.migrely.app.attack.ui.model.AttackDetailsViewStateUiModel
import com.migrely.app.attack.ui.notification.AttackDetailsEventPresenter
import com.migrely.app.designsystem.theme.MigrelyTheme
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val TITLE = "Sudden, worst-ever pain?"
private const val DELETE = "Delete migraine"

/**
 * The whole details screen, with its own counter of the card's events: a card restored by a rotation or a process
 * restore does not scroll, because its event does not come again (BRD D167).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w400dp-h700dp")
class SafetyCardRestoreTest {
    @get:Rule
    val rule = createComposeRule()

    private val events = AttackDetailsEventPresenter()
    private val viewModel: AttackDetailsViewModel = mock {
        on { viewState } doReturn flowOf(AttackDetailsViewState.Loading)
        on { notification } doReturn emptyFlow()
        on { navigationEvent } doReturn emptyFlow()
    }
    private val viewStateMapper: AttackDetailsViewStateUiMapper = mock {
        on { toUi(any()) } doReturn AttackDetailsViewStateUiModel.Ready(previewDetails())
    }
    private val dependencies = AttackDetailsDependencies(
        attackDetailsViewModel = viewModel,
        attackDetailsViewStateUiMapper = viewStateMapper,
        attackTimeUiFormatter = previewTimeFormatter,
        attackDetailsEventPresenter = events,
        attackMessageBehaviourUiMapper = mock(),
        labels = AttackLabels.Default,
        attackTagUiToPresentationMapper = mock(),
        functionalImpactUiToPresentationMapper = mock(),
        tagKindUiToPresentationMapper = mock(),
        intensityPointUiToPresentationMapper = mock(),
        timesUndoUiToPresentationMapper = mock(),
        attackIntakeUiToPresentationMapper = mock(),
        attackDetailsNavigationMapper = mock(),
        attackNotificationMapper = mock()
    )

    private val content get() = rule.onNode(hasScrollAction())

    private fun scrollValue() =
        content.fetchSemanticsNode().config[SemanticsProperties.VerticalScrollAxisRange].value()

    private fun cardTopFromContentTop() =
        rule.onNodeWithText(TITLE).onParent().getUnclippedBoundsInRoot().top -
            content.getUnclippedBoundsInRoot().top

    @Test
    fun `Given a card that came into view then a restore scrolled away does not scroll`() {
        val restoration = StateRestorationTester(rule)
        val navController = NavController(ApplicationProvider.getApplicationContext())
        restoration.setContent {
            MigrelyTheme {
                dependencies.AttackDetails(
                    attackId = "a",
                    announceMerge = false,
                    navController = navController
                )
            }
        }
        content.performScrollToNode(hasText(DELETE))
        events.show(AttackDetailsEventUiModel.FirstMaximumRecorded)
        rule.mainClock.advanceTimeBy(1_000)
        // The event brought the card into view, as on a phone.
        assertTrue("card top ${cardTopFromContentTop()}", cardTopFromContentTop() in (-1).dp..1.dp)
        content.performScrollToNode(hasText(DELETE))
        rule.waitForIdle()
        val scrolledAway = scrollValue()

        restoration.emulateSavedInstanceStateRestore()
        rule.mainClock.advanceTimeBy(1_000)

        assertEquals(scrolledAway, scrollValue())
        rule.onNodeWithText(TITLE).assertExists()
    }
}
