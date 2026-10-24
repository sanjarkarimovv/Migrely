package com.migrely.app.reminders.domain.repository

interface GetNotificationsAllowedRepository {
    fun notificationsAllowed(): Boolean
}
