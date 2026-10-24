package com.migrely.app.reminders.ui.mapper

import com.migrely.app.reminders.presentation.model.ReminderKindPresentationModel
import com.migrely.app.reminders.ui.model.ReminderKindUiModel

class ReminderKindUiToPresentationMapper {
    fun toPresentation(kind: ReminderKindUiModel) = when (kind) {
        ReminderKindUiModel.Daily -> ReminderKindPresentationModel.Daily
        ReminderKindUiModel.Effect -> ReminderKindPresentationModel.Effect
    }
}
