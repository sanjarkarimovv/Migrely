package com.migrely.app.reminders.domain.model

sealed interface TodayEntriesResultDomainModel {
    /** The attacks that may touch today, and whether today is marked pain-free. */
    data class Loaded(val attacks: List<ReminderAttackDomainModel>, val painFree: Boolean) :
        TodayEntriesResultDomainModel

    data object Failed : TodayEntriesResultDomainModel
}
