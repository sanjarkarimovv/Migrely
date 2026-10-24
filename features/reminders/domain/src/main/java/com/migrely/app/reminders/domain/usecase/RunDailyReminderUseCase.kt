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
import java.time.LocalDate

/**
 * One run of the daily reminder for the date it was planned for, or today (BRD D66). It first plans the next run, then
 * shows "How was today?" about that date unless the run slipped past its day, the reminder is off, notifications are
 * off, it already showed for that date, or the date is answered: marked pain-free, or touched by an attack by the
 * calendar's rule (D36).
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
) : BackgroundExecutingUseCase<LocalDate?, DailyReminderRunDomainModel>(coroutineContextProvider) {
    private val dailyReminderTime = DailyReminderTime()

    override fun executeInBackground(request: LocalDate?): DailyReminderRunDomainModel {
        val settings = when (val result = getReminderSettingsRepository.reminderSettings()) {
            is ReminderSettingsResultDomainModel.Loaded -> result.settings
            ReminderSettingsResultDomainModel.Failed -> return DailyReminderRunDomainModel.Failed
        }
        if (!settings.dailyEnabled) {
            cancelDailyReminderRepository.cancelDailyReminder()
            return DailyReminderRunDomainModel.Skipped
        }
        val now = getCurrentTimeRepository.currentTime()
        val today = now.instant.atZone(now.zone).toLocalDate()
        val date = request ?: today
        val next = dailyReminderTime.after(date, now, settings.dailyTime)
        scheduleDailyReminderRepository.scheduleDailyReminder(
            next,
            dailyReminderTime.dateOf(next, now.zone)
        )
        // A run that slipped past midnight would ask about a day not yet lived; the next run asks about that one.
        if (date != today ||
            !getNotificationsAllowedRepository.notificationsAllowed() ||
            settings.dailyLastShownOn == date
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
        showDailyReminderRepository.showDailyReminder(date)
        setDailyShownRepository.setDailyShown(date)
        return DailyReminderRunDomainModel.Shown
    }
}
