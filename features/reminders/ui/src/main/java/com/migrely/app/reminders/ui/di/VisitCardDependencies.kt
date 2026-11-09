package com.migrely.app.reminders.ui.di

import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.notification.mapper.NotificationUiMapper
import com.migrely.app.architecture.ui.view.BaseComposeHolder
import com.migrely.app.reminders.presentation.model.VisitCardPresentationNotification
import com.migrely.app.reminders.presentation.model.VisitCardViewState
import com.migrely.app.reminders.presentation.navigation.VisitCardPresentationNavigationEvent
import com.migrely.app.reminders.presentation.viewmodel.VisitCardViewModel

/** Home's visit card, which `:app` puts in home's slot (BRD D168, design D1, D8). */
class VisitCardDependencies(
    val visitCardViewModel: VisitCardViewModel,
    navigationMapper: NavigationEventDestinationMapper<VisitCardPresentationNavigationEvent>,
    notificationMapper: NotificationUiMapper<VisitCardPresentationNotification>
) : BaseComposeHolder<VisitCardViewState, VisitCardPresentationNotification>(
    visitCardViewModel,
    navigationMapper,
    notificationMapper
)
