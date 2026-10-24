package com.migrely.app.attack.ui.di

import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.notification.mapper.NotificationUiMapper
import com.migrely.app.architecture.ui.view.BaseComposeHolder
import com.migrely.app.attack.presentation.model.AttackDetailsPresentationNotification
import com.migrely.app.attack.presentation.model.AttackDetailsViewState
import com.migrely.app.attack.presentation.navigation.AttackDetailsPresentationNavigationEvent
import com.migrely.app.attack.presentation.viewmodel.AttackDetailsViewModel
import com.migrely.app.attack.ui.mapper.AttackDetailsViewStateUiMapper
import com.migrely.app.attack.ui.mapper.AttackMessageBehaviourUiMapper
import com.migrely.app.attack.ui.mapper.AttackTagUiToPresentationMapper
import com.migrely.app.attack.ui.mapper.AttackTimeUiFormatter
import com.migrely.app.attack.ui.mapper.FunctionalImpactUiToPresentationMapper
import com.migrely.app.attack.ui.mapper.IntensityPointUiToPresentationMapper
import com.migrely.app.attack.ui.mapper.TagKindUiToPresentationMapper
import com.migrely.app.attack.ui.mapper.TimesUndoUiToPresentationMapper
import com.migrely.app.attack.ui.notification.AttackDetailsEventPresenter
import com.migrely.app.attack.ui.view.AttackLabels

@Suppress("LongParameterList")
class AttackDetailsDependencies(
    val attackDetailsViewModel: AttackDetailsViewModel,
    val attackDetailsViewStateUiMapper: AttackDetailsViewStateUiMapper,
    val attackTimeUiFormatter: AttackTimeUiFormatter,
    val attackDetailsEventPresenter: AttackDetailsEventPresenter,
    val attackMessageBehaviourUiMapper: AttackMessageBehaviourUiMapper,
    val labels: AttackLabels,
    val attackTagUiToPresentationMapper: AttackTagUiToPresentationMapper,
    val functionalImpactUiToPresentationMapper: FunctionalImpactUiToPresentationMapper,
    val tagKindUiToPresentationMapper: TagKindUiToPresentationMapper,
    val intensityPointUiToPresentationMapper: IntensityPointUiToPresentationMapper,
    val timesUndoUiToPresentationMapper: TimesUndoUiToPresentationMapper,
    attackDetailsNavigationMapper:
    NavigationEventDestinationMapper<AttackDetailsPresentationNavigationEvent>,
    attackNotificationMapper: NotificationUiMapper<AttackDetailsPresentationNotification>
) : BaseComposeHolder<AttackDetailsViewState, AttackDetailsPresentationNotification>(
    attackDetailsViewModel,
    attackDetailsNavigationMapper,
    attackNotificationMapper
)
