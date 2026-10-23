package com.migrely.app.navigation.mapper

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import com.migrely.app.architecture.presentation.navigation.PresentationNavigationEvent
import com.migrely.app.architecture.ui.navigation.exception.UnhandledNavigationException
import com.migrely.app.home.presentation.navigation.HomePresentationNavigationEvent
import com.migrely.app.ui.main.route.AttackDetails
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class HomeNavigationEventDestinationMapperTest {
    private lateinit var classUnderTest: HomeNavigationEventDestinationMapper

    @Before
    fun setUp() {
        classUnderTest = HomeNavigationEventDestinationMapper()
    }

    @Test
    fun `Given viewing an attack's details when toUi then navigates to its details route`() {
        // Given
        val navController: NavController = mock()

        // When
        classUnderTest
            .toUi(HomePresentationNavigationEvent.OnViewAttackDetails("a")).navigate(navController)

        // Then
        verify(navController).navigate(
            eq(AttackDetails("a")),
            anyOrNull<NavOptions>(),
            anyOrNull<Navigator.Extras>()
        )
    }

    @Test(expected = UnhandledNavigationException::class)
    fun `Given an event home does not own when toUi then throws UnhandledNavigationException`() {
        // Given
        val givenEvent = object : PresentationNavigationEvent {}

        // When
        classUnderTest.toUi(givenEvent)

        // Then throws UnhandledNavigationException
    }
}
