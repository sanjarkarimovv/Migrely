package com.migrely.app.navigation.mapper

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import com.migrely.app.architecture.presentation.navigation.PresentationNavigationEvent
import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.history.presentation.model.PremiumFeaturePresentationModel
import com.migrely.app.history.presentation.navigation.HistoryPresentationNavigationEvent
import com.migrely.app.subscription.presentation.model.PaywallContextPresentationModel
import com.migrely.app.subscription.presentation.navigation.PaywallGatePresentationNavigationEvent
import com.migrely.app.subscription.presentation.navigation.PaywallPresentationNavigationEvent
import com.migrely.app.subscription.presentation.navigation.PaywallTriggerPresentationNavigationEvent
import com.migrely.app.subscription.presentation.navigation.PremiumSectionPresentationNavigationEvent
import com.migrely.app.subscription.ui.model.PaywallContextUiModel
import com.migrely.app.ui.main.route.Home
import com.migrely.app.ui.main.route.Insights
import com.migrely.app.ui.main.route.Paywall
import com.migrely.app.ui.main.route.Report
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

/** Each event that opens the paywall opens it once, on top, with its origin. */
@RunWith(Parameterized::class)
class OpenPaywallNavigationEventDestinationMappersTest(
    private val mapper: NavigationEventDestinationMapper<*>,
    private val event: PresentationNavigationEvent,
    private val expectedRoute: Paywall
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{1} -> {2}")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(
                HistoryNavigationEventDestinationMapper(),
                HistoryPresentationNavigationEvent.OnOpenPaywall(
                    PremiumFeaturePresentationModel.Report
                ),
                Paywall("Report")
            ),
            arrayOf(
                HistoryNavigationEventDestinationMapper(),
                HistoryPresentationNavigationEvent.OnOpenPaywall(
                    PremiumFeaturePresentationModel.Insights
                ),
                Paywall("Insights")
            ),
            arrayOf(
                PremiumSectionNavigationEventDestinationMapper(),
                PremiumSectionPresentationNavigationEvent.OnOpenPaywall,
                Paywall("Settings")
            ),
            arrayOf(
                PaywallTriggerNavigationEventDestinationMapper(),
                PaywallTriggerPresentationNavigationEvent.OnOpenPaywall,
                Paywall("Patterns")
            )
        )
    }

    @Test
    fun `When toUi then opens the paywall once on top`() {
        val navController: NavController = mock()

        mapper.toUi(event).navigate(navController)

        val options = argumentCaptor<NavOptions>()
        verify(navController).navigate(
            eq(expectedRoute),
            options.capture(),
            anyOrNull<Navigator.Extras>()
        )
        assertTrue(options.firstValue.shouldLaunchSingleTop())
        assertEquals(-1, options.firstValue.popUpToId)
    }
}

/** Where the paywall leads: the feature or home in its place, or back. */
@RunWith(Parameterized::class)
class PaywallReplacingNavigationEventDestinationMapperTest(
    private val event: PaywallPresentationNavigationEvent,
    private val expectedRoute: Any
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0} -> {1}")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(
                PaywallPresentationNavigationEvent.OnClose(
                    PaywallContextPresentationModel.Onboarding
                ),
                Home()
            ),
            arrayOf(
                PaywallPresentationNavigationEvent.OnPremiumUnlocked(
                    PaywallContextPresentationModel.Onboarding
                ),
                Home()
            ),
            arrayOf(
                PaywallPresentationNavigationEvent.OnPremiumUnlocked(
                    PaywallContextPresentationModel.Report
                ),
                Report
            ),
            arrayOf(
                PaywallPresentationNavigationEvent.OnPremiumUnlocked(
                    PaywallContextPresentationModel.Insights
                ),
                Insights
            ),
            arrayOf(
                PaywallPresentationNavigationEvent.OnPremiumUnlocked(
                    PaywallContextPresentationModel.Patterns
                ),
                Insights
            )
        )
    }

    @Test
    fun `When toUi then the route takes the paywall's place`() {
        val navController: NavController = mock()

        PaywallNavigationEventDestinationMapper().toUi(event).navigate(navController)

        val options = argumentCaptor<NavOptions>()
        verify(navController).navigate(
            eq(expectedRoute),
            options.capture(),
            anyOrNull<Navigator.Extras>()
        )
        assertTrue(options.firstValue.isPopUpToInclusive())
        assertTrue(options.firstValue.shouldLaunchSingleTop())
    }
}

class PaywallLeavingNavigationEventDestinationMapperTest {
    private val classUnderTest = PaywallNavigationEventDestinationMapper()

    @Test
    fun `Given the paywall was tapped open when closing then it pops`() {
        PaywallContextPresentationModel.entries
            .filter { it != PaywallContextPresentationModel.Onboarding }
            .forEach { context ->
                val navController: NavController = mock()

                classUnderTest.toUi(PaywallPresentationNavigationEvent.OnClose(context))
                    .navigate(navController)

                verify(navController).popBackStack(Paywall::class, true, false)
            }
    }

    @Test
    fun `Given Premium unlocked from Settings then it goes back to Settings`() {
        val navController: NavController = mock()

        classUnderTest.toUi(
            PaywallPresentationNavigationEvent.OnPremiumUnlocked(
                PaywallContextPresentationModel.Settings
            )
        ).navigate(navController)

        verify(navController).popBackStack(Paywall::class, true, false)
    }
}

class PaywallGateNavigationEventDestinationMapperTest {
    private val classUnderTest = PaywallGateNavigationEventDestinationMapper()

    @Test
    fun `Given the paywall shows itself after onboarding then it takes the gate's place`() {
        val navController: NavController = mock()

        classUnderTest.toUi(PaywallGatePresentationNavigationEvent.OnShowPaywall)
            .navigate(navController)

        val options = argumentCaptor<NavOptions>()
        verify(navController).navigate(
            eq(Paywall("Onboarding")),
            options.capture(),
            anyOrNull<Navigator.Extras>()
        )
        assertTrue(options.firstValue.isPopUpToInclusive())
    }

    @Test
    fun `Given no paywall is due then home takes the gate's place`() {
        val navController: NavController = mock()

        classUnderTest.toUi(PaywallGatePresentationNavigationEvent.OnContinue)
            .navigate(navController)

        val options = argumentCaptor<NavOptions>()
        verify(navController).navigate(eq(Home()), options.capture(), anyOrNull<Navigator.Extras>())
        assertTrue(options.firstValue.isPopUpToInclusive())
        assertFalse(options.firstValue.shouldRestoreState())
    }
}

class PaywallOriginUiMapperTest {
    @Test
    fun `Given each origin then the paywall gets its context, and an unknown one is Settings`() {
        val mapper = PaywallOriginUiMapper()

        assertEquals(
            listOf(
                PaywallContextUiModel.Onboarding,
                PaywallContextUiModel.Patterns,
                PaywallContextUiModel.Report,
                PaywallContextUiModel.Insights,
                PaywallContextUiModel.Settings,
                PaywallContextUiModel.Settings
            ),
            listOf("Onboarding", "Patterns", "Report", "Insights", "Settings", "Family")
                .map(mapper::toUi)
        )
    }
}
