package com.migrely.app.navigation.mapper

import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.navigation.model.UiDestination
import com.migrely.app.reminders.presentation.navigation.RemindersPresentationNavigationEvent
import com.migrely.app.ui.main.route.Reminders

class RemindersNavigationEventDestinationMapper :
    NavigationEventDestinationMapper<RemindersPresentationNavigationEvent>(
        RemindersPresentationNavigationEvent::class
    ) {
    override fun mapTypedEvent(
        navigationEvent: RemindersPresentationNavigationEvent
    ): UiDestination = when (navigationEvent) {
        RemindersPresentationNavigationEvent.OnBack -> UiDestination {
            it.popBackStack(Reminders::class, inclusive = true)
        }
    }
}
