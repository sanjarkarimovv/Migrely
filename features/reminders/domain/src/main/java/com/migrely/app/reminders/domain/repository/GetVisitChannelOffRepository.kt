package com.migrely.app.reminders.domain.repository

interface GetVisitChannelOffRepository {
    /** Whether the user turned off the "Doctor visit" notification channel in Android. */
    fun visitChannelOff(): Boolean
}
