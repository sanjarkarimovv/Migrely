package com.migrely.app.reminder

import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.history.domain.usecase.SetDayMarkUseCase
import com.migrely.app.reminders.domain.usecase.DismissDailyReminderUseCase
import com.migrely.app.reminders.domain.usecase.RescheduleRemindersUseCase
import com.migrely.app.reminders.domain.usecase.RunDailyReminderUseCase
import com.migrely.app.reminders.domain.usecase.ShowAttackLoggedTodayUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** What the worker and receivers need; Android creates them, so they reach the graph through this entry point. */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface ReminderEntryPoint {
    fun runDailyReminderUseCase(): RunDailyReminderUseCase

    fun rescheduleRemindersUseCase(): RescheduleRemindersUseCase

    fun setDayMarkUseCase(): SetDayMarkUseCase

    fun showAttackLoggedTodayUseCase(): ShowAttackLoggedTodayUseCase

    fun dismissDailyReminderUseCase(): DismissDailyReminderUseCase

    fun clockDataSource(): ClockDataSource
}
