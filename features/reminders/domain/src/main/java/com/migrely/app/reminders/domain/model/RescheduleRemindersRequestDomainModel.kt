package com.migrely.app.reminders.domain.model

/**
 * Why the reminders are planned again (BRD D176): a start of the app's process keeps a daily run already planned, a
 * clock or time zone change moves it to the new local time.
 */
enum class RescheduleRemindersRequestDomainModel {
    AppStart,
    ClockChange
}
