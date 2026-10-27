package com.migrely.app.reminders.presentation.model

sealed interface RemindersViewState {
    data object Loading : RemindersViewState

    data class Ready(val reminders: RemindersPresentationModel) : RemindersViewState

    data object Unavailable : RemindersViewState
}
