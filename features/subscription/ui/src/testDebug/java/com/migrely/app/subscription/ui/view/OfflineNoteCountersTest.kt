package com.migrely.app.subscription.ui.view

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.NavController
import androidx.test.core.app.ApplicationProvider
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.subscription.presentation.model.PremiumNotePresentationModel
import com.migrely.app.subscription.presentation.model.PremiumPlanPresentationModel
import com.migrely.app.subscription.presentation.model.PremiumSectionViewState
import com.migrely.app.subscription.presentation.model.PremiumStatusPresentationModel
import com.migrely.app.subscription.presentation.model.TrialBannerViewState
import com.migrely.app.subscription.presentation.viewmodel.PremiumSectionViewModel
import com.migrely.app.subscription.presentation.viewmodel.TrialBannerViewModel
import com.migrely.app.subscription.ui.di.PremiumSectionDependencies
import com.migrely.app.subscription.ui.di.TrialBannerDependencies
import com.migrely.app.subscription.ui.mapper.PremiumPlanUiMapper
import com.migrely.app.subscription.ui.mapper.PremiumSectionShownResolver
import com.migrely.app.subscription.ui.mapper.PremiumSectionViewStateUiMapper
import com.migrely.app.subscription.ui.mapper.PremiumStatusUiMapper
import com.migrely.app.subscription.ui.mapper.TrialBannerViewStateUiMapper
import java.time.Instant
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val OFFLINE_MANAGE =
    "You're offline. Connect to the internet to manage your subscription. Your diary works offline."
private const val EVENT_BUFFER = 8

/**
 * The counters behind "two offline taps are spoken twice" (BRD D166): Settings' and the trial banner's own screens,
 * given the same offline state twice, show the note as two different nodes, so TalkBack hears two live regions.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class OfflineNoteCountersTest {
    @get:Rule
    val rule = createComposeRule()

    private fun noteId() = rule.onNodeWithText(OFFLINE_MANAGE).fetchSemanticsNode().id

    @Test
    fun `Given the same offline state twice then the trial banner's note is a new node`() {
        val states = MutableSharedFlow<TrialBannerViewState>(extraBufferCapacity = EVENT_BUFFER)
        val viewModel: TrialBannerViewModel = mock { on { viewState } doReturn states }
        val dependencies = TrialBannerDependencies(viewModel, TrialBannerViewStateUiMapper())
        val offline = TrialBannerViewState.Shown(
            Instant.parse("2026-11-16T09:00:00Z"),
            endsToday = false,
            renews = true,
            offlineToManage = true
        )
        rule.setContent { MigrelyTheme { dependencies.TrialBanner(refreshKey = 0) } }
        rule.waitForIdle()

        states.tryEmit(offline)
        rule.waitForIdle()
        val first = noteId()
        states.tryEmit(offline)
        rule.waitForIdle()

        assertNotEquals(first, noteId())
    }

    @Test
    fun `Given the same offline note twice then Settings' note is a new node`() {
        val states = MutableSharedFlow<PremiumSectionViewState>(extraBufferCapacity = EVENT_BUFFER)
        val viewModel: PremiumSectionViewModel = mock {
            on { viewState } doReturn states
            on { notification } doReturn emptyFlow()
            on { navigationEvent } doReturn emptyFlow()
        }
        val dependencies = PremiumSectionDependencies(
            premiumSectionViewModel = viewModel,
            purchaseUpdates = emptyFlow(),
            premiumSectionViewStateUiMapper =
            PremiumSectionViewStateUiMapper(PremiumStatusUiMapper(PremiumPlanUiMapper())),
            premiumSectionShownResolver = PremiumSectionShownResolver(),
            navigationMapper = mock(),
            notificationMapper = mock()
        )
        val offline = PremiumSectionViewState.Ready(
            PremiumStatusPresentationModel.Subscribed(PremiumPlanPresentationModel.Yearly, null),
            PremiumNotePresentationModel.OfflineToManage
        )
        val navController = NavController(ApplicationProvider.getApplicationContext())
        rule.setContent { MigrelyTheme { dependencies.PremiumSection(navController) } }
        rule.waitForIdle()

        states.tryEmit(offline)
        rule.waitForIdle()
        val first = noteId()
        states.tryEmit(offline)
        rule.waitForIdle()

        assertNotEquals(first, noteId())
    }
}
