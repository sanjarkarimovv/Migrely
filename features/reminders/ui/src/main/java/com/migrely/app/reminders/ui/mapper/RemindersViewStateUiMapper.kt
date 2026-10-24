package com.migrely.app.reminders.ui.mapper

import com.migrely.app.reminders.presentation.model.RemindersViewState
import com.migrely.app.reminders.ui.model.RemindersUiModel
import com.migrely.app.reminders.ui.model.RemindersViewStateUiModel

private const val MINUTES_PER_HOUR = 60

class RemindersViewStateUiMapper {
    fun toUi(viewState: RemindersViewState): RemindersViewStateUiModel = when (viewState) {
        RemindersViewState.Loading -> RemindersViewStateUiModel.Loading
        is RemindersViewState.Ready -> RemindersViewStateUiModel.Ready(
            RemindersUiModel(
                dailyEnabled = viewState.reminders.dailyEnabled,
                dailyMinuteOfDay = viewState.reminders.dailyTime.hour * MINUTES_PER_HOUR +
                    viewState.reminders.dailyTime.minute,
                effectEnabled = viewState.reminders.effectEnabled,
                notificationsAllowed = viewState.reminders.notificationsAllowed,
                mayAskPermission = viewState.reminders.mayAskPermission
            )
        )
        RemindersViewState.Unavailable -> RemindersViewStateUiModel.Unavailable
    }
}
