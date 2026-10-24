package com.migrely.app.reminders.ui.di

import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.notification.mapper.NotificationUiMapper
import com.migrely.app.architecture.ui.view.BaseComposeHolder
import com.migrely.app.reminders.presentation.model.RemindersPresentationNotification
import com.migrely.app.reminders.presentation.model.RemindersViewState
import com.migrely.app.reminders.presentation.navigation.RemindersPresentationNavigationEvent
import com.migrely.app.reminders.presentation.viewmodel.RemindersViewModel
import com.migrely.app.reminders.ui.mapper.ReminderKindUiToPresentationMapper
import com.migrely.app.reminders.ui.mapper.RemindersEventPresenter
import com.migrely.app.reminders.ui.mapper.RemindersViewStateUiMapper

class RemindersDependencies(
    val remindersViewModel: RemindersViewModel,
    val remindersViewStateUiMapper: RemindersViewStateUiMapper,
    val remindersEventPresenter: RemindersEventPresenter,
    val reminderKindUiToPresentationMapper: ReminderKindUiToPresentationMapper,
    navigationMapper: NavigationEventDestinationMapper<RemindersPresentationNavigationEvent>,
    notificationMapper: NotificationUiMapper<RemindersPresentationNotification>
) : BaseComposeHolder<RemindersViewState, RemindersPresentationNotification>(
    remindersViewModel,
    navigationMapper,
    notificationMapper
)
