package com.migrely.app.reminders.ui.mapper

import com.migrely.app.reminders.presentation.model.DailyReminderOfferViewState
import com.migrely.app.reminders.ui.model.DailyReminderOfferUiModel

private const val MINUTES_PER_HOUR = 60

class DailyReminderOfferUiMapper {
    /** Null while loading. */
    fun toUi(viewState: DailyReminderOfferViewState): DailyReminderOfferUiModel? =
        when (viewState) {
            DailyReminderOfferViewState.Loading -> null
            is DailyReminderOfferViewState.Ready ->
                DailyReminderOfferUiModel(
                    viewState.time.hour * MINUTES_PER_HOUR + viewState.time.minute
                )
        }
}
