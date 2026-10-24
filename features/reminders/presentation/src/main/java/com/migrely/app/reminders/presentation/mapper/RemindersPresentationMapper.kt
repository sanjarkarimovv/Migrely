package com.migrely.app.reminders.presentation.mapper

import com.migrely.app.reminders.domain.model.RemindersResultDomainModel
import com.migrely.app.reminders.presentation.model.RemindersPresentationModel
import com.migrely.app.reminders.presentation.model.RemindersViewState

class RemindersPresentationMapper {
    fun toPresentation(result: RemindersResultDomainModel): RemindersViewState = when (result) {
        is RemindersResultDomainModel.Loaded -> RemindersViewState.Ready(
            RemindersPresentationModel(
                dailyEnabled = result.reminders.dailyEnabled,
                dailyTime = result.reminders.dailyTime,
                effectEnabled = result.reminders.effectEnabled,
                notificationsAllowed = result.reminders.notificationsAllowed,
                mayAskPermission = result.reminders.mayAskPermission
            )
        )
        RemindersResultDomainModel.Failed -> RemindersViewState.Unavailable
    }
}
