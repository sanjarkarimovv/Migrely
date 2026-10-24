package com.migrely.app.navigation.mapper

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import com.migrely.app.architecture.presentation.navigation.PresentationNavigationEvent
import com.migrely.app.architecture.ui.navigation.exception.UnhandledNavigationException
import com.migrely.app.history.presentation.navigation.HistoryPresentationNavigationEvent
import com.migrely.app.insights.presentation.navigation.InsightsPresentationNavigationEvent
import com.migrely.app.ui.main.route.Insights
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class InsightsNavigationEventDestinationMapperTest {
    @Test
    fun `Given History asks for insights when toUi then opens them once`() {
        // Given
        val navController: NavController = mock()

        // When
        HistoryNavigationEventDestinationMapper().toUi(
            HistoryPresentationNavigationEvent.OnOpenInsights
        ).navigate(navController)

        // Then
        val options = argumentCaptor<NavOptions>()
        verify(navController).navigate(
            eq(Insights),
            options.capture(),
            anyOrNull<Navigator.Extras>()
        )
        assertTrue(options.firstValue.shouldLaunchSingleTop())
    }

    @Test
    fun `Given going back when toUi then leaves insights`() {
        // Given
        val navController: NavController = mock()

        // When
        InsightsNavigationEventDestinationMapper().toUi(
            InsightsPresentationNavigationEvent.OnBack
        ).navigate(navController)

        // Then
        verify(navController).popBackStack(Insights::class, true, false)
    }

    @Test(expected = UnhandledNavigationException::class)
    fun `Given an event Insights does not own when toUi then throws`() {
        InsightsNavigationEventDestinationMapper().toUi(object : PresentationNavigationEvent {})
    }
}
