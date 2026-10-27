package com.migrely.app.navigation.mapper

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import com.migrely.app.history.presentation.navigation.HistoryPresentationNavigationEvent
import com.migrely.app.report.presentation.navigation.ReportPresentationNavigationEvent
import com.migrely.app.ui.main.route.Report
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class ReportNavigationEventDestinationMapperTest {
    @Test
    fun `Given History asks for the report when toUi then opens it once`() {
        // Given
        val navController: NavController = mock()

        // When
        HistoryNavigationEventDestinationMapper().toUi(
            HistoryPresentationNavigationEvent.OnOpenReport
        )
            .navigate(navController)

        // Then
        val options = argumentCaptor<NavOptions>()
        verify(navController).navigate(eq(Report), options.capture(), anyOrNull<Navigator.Extras>())
        assertTrue(options.firstValue.shouldLaunchSingleTop())
    }

    @Test
    fun `Given going back when toUi then leaves the report`() {
        // Given
        val navController: NavController = mock()

        // When
        ReportNavigationEventDestinationMapper().toUi(
            ReportPresentationNavigationEvent.OnBack
        ).navigate(navController)

        // Then
        verify(navController).popBackStack(Report::class, true, false)
    }
}
