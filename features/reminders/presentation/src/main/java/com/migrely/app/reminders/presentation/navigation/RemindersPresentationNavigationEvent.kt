package com.migrely.app.reminders.presentation.navigation

import com.migrely.app.architecture.presentation.navigation.PresentationNavigationEvent

sealed interface RemindersPresentationNavigationEvent : PresentationNavigationEvent {
    data object OnBack : RemindersPresentationNavigationEvent
}
