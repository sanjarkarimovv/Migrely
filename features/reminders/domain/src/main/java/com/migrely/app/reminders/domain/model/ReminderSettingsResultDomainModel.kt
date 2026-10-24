package com.migrely.app.reminders.domain.model

sealed interface ReminderSettingsResultDomainModel {
    data class Loaded(val settings: ReminderSettingsDomainModel) : ReminderSettingsResultDomainModel

    data object Failed : ReminderSettingsResultDomainModel
}
