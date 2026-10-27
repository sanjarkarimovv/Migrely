package com.migrely.app.navigation.mapper

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import com.migrely.app.architecture.presentation.navigation.PresentationNavigationEvent
import com.migrely.app.architecture.ui.navigation.exception.UnhandledNavigationException
import com.migrely.app.home.presentation.navigation.HomePresentationNavigationEvent
import com.migrely.app.ui.main.route.AddPastAttack
import com.migrely.app.ui.main.route.AttackDetails
import com.migrely.app.ui.main.route.Settings
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
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

    @Test
    fun `Given adding a past attack when toUi then opens the add screen once`() {
        // Given
        val navController: NavController = mock()

        // When
        classUnderTest.toUi(HomePresentationNavigationEvent.OnAddPastAttack).navigate(navController)

        // Then
        val options = argumentCaptor<NavOptions>()
        verify(navController).navigate(
            eq(AddPastAttack()),
            options.capture(),
            anyOrNull<Navigator.Extras>()
        )
        assertTrue(options.firstValue.shouldLaunchSingleTop())
    }

    @Test
    fun `Given opening settings when toUi then opens Settings once`() {
        // Given
        val navController: NavController = mock()

        // When
        classUnderTest.toUi(HomePresentationNavigationEvent.OnOpenSettings).navigate(navController)

        // Then
        val options = argumentCaptor<NavOptions>()
        verify(
            navController
        ).navigate(eq(Settings), options.capture(), anyOrNull<Navigator.Extras>())
        assertTrue(options.firstValue.shouldLaunchSingleTop())
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
