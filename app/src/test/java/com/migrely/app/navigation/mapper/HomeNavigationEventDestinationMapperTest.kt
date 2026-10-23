package com.migrely.app.navigation.mapper

import com.migrely.app.architecture.presentation.navigation.PresentationNavigationEvent
import com.migrely.app.architecture.ui.navigation.exception.UnhandledNavigationException
import org.junit.Before
import org.junit.Test

class HomeNavigationEventDestinationMapperTest {
    private lateinit var classUnderTest: HomeNavigationEventDestinationMapper

    @Before
    fun setUp() {
        classUnderTest = HomeNavigationEventDestinationMapper()
    }

    // The home screen emits no navigation events yet. Parameterized cases are added with the first
    // HomePresentationNavigationEvent subtype (change add-attack-logging).
    @Test(expected = UnhandledNavigationException::class)
    fun `Given an event home does not own when toUi then throws UnhandledNavigationException`() {
        // Given
        val givenEvent = object : PresentationNavigationEvent {}

        // When
        classUnderTest.toUi(givenEvent)

        // Then throws UnhandledNavigationException
    }
}
