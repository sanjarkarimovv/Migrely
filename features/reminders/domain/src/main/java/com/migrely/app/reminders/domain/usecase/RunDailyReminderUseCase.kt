package com.migrely.app.reminders.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.attackdays.domain.AttackDays
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.reminders.domain.model.DailyReminderRunDomainModel
import com.migrely.app.reminders.domain.model.ReminderSettingsResultDomainModel
import com.migrely.app.reminders.domain.model.TodayEntriesResultDomainModel
import com.migrely.app.reminders.domain.repository.CancelDailyReminderRepository
import com.migrely.app.reminders.domain.repository.GetCurrentTimeRepository
import com.migrely.app.reminders.domain.repository.GetNotificationsAllowedRepository
import com.migrely.app.reminders.domain.repository.GetReminderSettingsRepository
import com.migrely.app.reminders.domain.repository.GetTodayEntriesRepository
import com.migrely.app.reminders.domain.repository.ScheduleDailyReminderRepository
import com.migrely.app.reminders.domain.repository.SetDailyShownRepository
import com.migrely.app.reminders.domain.repository.ShowDailyReminderRepository

/**
 * One run of the daily reminder (BRD D66). It first plans tomorrow's run, then shows "How was today?" unless the
 * reminder is off, notifications are off, it already showed today, or today is answered: marked pain-free, or touched
 * by an attack by the calendar's rule (D36).
 */
@Suppress("LongParameterList")
class RunDailyReminderUseCase(
    private val getReminderSettingsRepository: GetReminderSettingsRepository,
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    private val getNotificationsAllowedRepository: GetNotificationsAllowedRepository,
    private val getTodayEntriesRepository: GetTodayEntriesRepository,
    private val scheduleDailyReminderRepository: ScheduleDailyReminderRepository,
    private val cancelDailyReminderRepository: CancelDailyReminderRepository,
    private val showDailyReminderRepository: ShowDailyReminderRepository,
    private val setDailyShownRepository: SetDailyShownRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Unit, DailyReminderRunDomainModel>(coroutineContextProvider) {
    private val dailyReminderTime = DailyReminderTime()

    override fun executeInBackground(request: Unit): DailyReminderRunDomainModel {
        val settings = when (val result = getReminderSettingsRepository.reminderSettings()) {
            is ReminderSettingsResultDomainModel.Loaded -> result.settings
            ReminderSettingsResultDomainModel.Failed -> return DailyReminderRunDomainModel.Failed
        }
        if (!settings.dailyEnabled) {
            cancelDailyReminderRepository.cancelDailyReminder()
            return DailyReminderRunDomainModel.Skipped
        }
        val now = getCurrentTimeRepository.currentTime()
        scheduleDailyReminderRepository.scheduleDailyReminder(
            dailyReminderTime.tomorrow(now, settings.dailyTime)
        )
        val today = now.instant.atZone(now.zone).toLocalDate()
        if (!getNotificationsAllowedRepository.notificationsAllowed() ||
            settings.dailyLastShownOn == today
        ) {
            return DailyReminderRunDomainModel.Skipped
        }
        val entries = getTodayEntriesRepository.todayEntries(
            AttackDays.windowStart(today, now.zone),
            AttackDays.windowEnd(today, now.zone),
            today
        )
        val answered = when (entries) {
            is TodayEntriesResultDomainModel.Loaded -> entries.painFree || entries.attacks.any {
                today in AttackDays.datesTouched(it.start, it.startZone, it.end, now.instant)
            }
            TodayEntriesResultDomainModel.Failed -> return DailyReminderRunDomainModel.Failed
        }
        if (answered) return DailyReminderRunDomainModel.Skipped
        showDailyReminderRepository.showDailyReminder()
        setDailyShownRepository.setDailyShown(today)
        return DailyReminderRunDomainModel.Shown
    }
}
