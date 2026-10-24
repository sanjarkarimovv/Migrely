package com.migrely.app.reminders.ui.di

import com.migrely.app.reminders.presentation.viewmodel.DailyReminderOfferViewModel
import com.migrely.app.reminders.ui.mapper.DailyReminderOfferEventUiMapper
import com.migrely.app.reminders.ui.mapper.DailyReminderOfferUiMapper

/** The daily reminder offer that onboarding hosts in a slot (BRD D80). */
class DailyReminderOfferDependencies(
    val dailyReminderOfferViewModel: DailyReminderOfferViewModel,
    val dailyReminderOfferUiMapper: DailyReminderOfferUiMapper,
    val dailyReminderOfferEventUiMapper: DailyReminderOfferEventUiMapper
)
