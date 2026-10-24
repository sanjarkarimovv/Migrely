package com.migrely.app.navigation.mapper

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import com.migrely.app.architecture.presentation.navigation.PresentationNavigationEvent
import com.migrely.app.architecture.ui.navigation.exception.UnhandledNavigationException
import com.migrely.app.history.presentation.navigation.MonthSkyPresentationNavigationEvent
import com.migrely.app.ui.main.route.History
import java.time.LocalDate
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@RunWith(Parameterized::class)
class MonthSkyNavigationEventDestinationMapperTest(
    private val event: MonthSkyPresentationNavigationEvent,
    private val expectedRoute: History
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0} -> {1}")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(MonthSkyPresentationNavigationEvent.OnViewMonth, History()),
            arrayOf(
                MonthSkyPresentationNavigationEvent.OnViewDay(LocalDate.ofEpochDay(20_375L)),
                History(20_375L)
            )
        )
    }

    private val classUnderTest = MonthSkyNavigationEventDestinationMapper()

    @Test
    fun `When toUi then opens History once`() {
        val navController: NavController = mock()

        classUnderTest.toUi(event).navigate(navController)

        val options = argumentCaptor<NavOptions>()
        verify(navController).navigate(
            eq(expectedRoute),
            options.capture(),
            anyOrNull<Navigator.Extras>()
        )
        assertTrue(options.firstValue.shouldLaunchSingleTop())
    }
}

class MonthSkyNavigationEventDestinationMapperUnhandledTest {
    @Test(expected = UnhandledNavigationException::class)
    fun `Given an event the card does not own when toUi then throws`() {
        MonthSkyNavigationEventDestinationMapper().toUi(object : PresentationNavigationEvent {})
    }
}
