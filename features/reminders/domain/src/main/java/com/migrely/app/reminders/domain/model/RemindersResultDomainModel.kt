package com.migrely.app.reminders.domain.model

sealed interface RemindersResultDomainModel {
    data class Loaded(val reminders: RemindersDomainModel) : RemindersResultDomainModel

    data object Failed : RemindersResultDomainModel
}
