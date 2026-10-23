package com.migrely.app.navigation.mapper

import com.migrely.app.architecture.ui.navigation.exception.UnhandledNavigationException
import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.navigation.model.UiDestination
import com.migrely.app.home.presentation.navigation.HomePresentationNavigationEvent

class HomeNavigationEventDestinationMapper :
    NavigationEventDestinationMapper<HomePresentationNavigationEvent>(
        HomePresentationNavigationEvent::class
    ) {
    // HomePresentationNavigationEvent has no subtypes yet, and Kotlin rejects an empty `when`.
    // Replace this with an exhaustive `when` (no `else`) when the first event is added.
    override fun mapTypedEvent(navigationEvent: HomePresentationNavigationEvent): UiDestination =
        throw UnhandledNavigationException(navigationEvent)
}
