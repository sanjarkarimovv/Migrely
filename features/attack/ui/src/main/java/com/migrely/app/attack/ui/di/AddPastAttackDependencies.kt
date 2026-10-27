package com.migrely.app.attack.ui.di

import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.notification.mapper.NotificationUiMapper
import com.migrely.app.architecture.ui.view.BaseComposeHolder
import com.migrely.app.attack.presentation.model.AddPastAttackPresentationNotification
import com.migrely.app.attack.presentation.model.AddPastAttackViewState
import com.migrely.app.attack.presentation.navigation.AddPastAttackPresentationNavigationEvent
import com.migrely.app.attack.presentation.viewmodel.AddPastAttackViewModel
import com.migrely.app.attack.ui.mapper.AttackMessageBehaviourUiMapper
import com.migrely.app.attack.ui.mapper.AttackTimeUiFormatter
import com.migrely.app.attack.ui.mapper.PastAttackDraftUiMapper
import com.migrely.app.attack.ui.notification.AddPastAttackEventPresenter

@Suppress("LongParameterList")
class AddPastAttackDependencies(
    val addPastAttackViewModel: AddPastAttackViewModel,
    val pastAttackDraftUiMapper: PastAttackDraftUiMapper,
    val attackTimeUiFormatter: AttackTimeUiFormatter,
    val addPastAttackEventPresenter: AddPastAttackEventPresenter,
    val attackMessageBehaviourUiMapper: AttackMessageBehaviourUiMapper,
    addPastAttackNavigationMapper:
    NavigationEventDestinationMapper<AddPastAttackPresentationNavigationEvent>,
    addPastAttackNotificationMapper: NotificationUiMapper<AddPastAttackPresentationNotification>
) : BaseComposeHolder<AddPastAttackViewState, AddPastAttackPresentationNotification>(
    addPastAttackViewModel,
    addPastAttackNavigationMapper,
    addPastAttackNotificationMapper
)
