package com.migrely.app.navigation.mapper

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import com.migrely.app.onboarding.presentation.navigation.OnboardingPresentationNavigationEvent
import com.migrely.app.ui.main.route.PaywallGate
import com.migrely.app.ui.main.route.YourData
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class OnboardingNavigationEventDestinationMapperTest {
    private val classUnderTest = OnboardingNavigationEventDestinationMapper()

    @Test
    fun `Given onboarding is finished when toUi then the paywall gate takes its place`() {
        // Given
        val navController: NavController = mock()

        // When
        classUnderTest.toUi(
            OnboardingPresentationNavigationEvent.OnFinished
        ).navigate(navController)

        // Then
        val options = argumentCaptor<NavOptions>()
        verify(navController).navigate(
            eq(PaywallGate),
            options.capture(),
            anyOrNull<Navigator.Extras>()
        )
        assertTrue(options.firstValue.isPopUpToInclusive())
        assertTrue(options.firstValue.shouldLaunchSingleTop())
    }

    @Test
    fun `Given restoring a backup when toUi then opens Your data once`() {
        // Given
        val navController: NavController = mock()

        // When
        classUnderTest.toUi(
            OnboardingPresentationNavigationEvent.OnRestoreBackup
        ).navigate(navController)

        // Then
        val options = argumentCaptor<NavOptions>()
        verify(
            navController
        ).navigate(eq(YourData), options.capture(), anyOrNull<Navigator.Extras>())
        assertTrue(options.firstValue.shouldLaunchSingleTop())
    }
}
