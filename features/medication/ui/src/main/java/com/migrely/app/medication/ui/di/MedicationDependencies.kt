package com.migrely.app.medication.ui.di

import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.notification.mapper.NotificationUiMapper
import com.migrely.app.architecture.ui.view.BaseComposeHolder
import com.migrely.app.medication.presentation.model.EditMedicationPresentationNotification
import com.migrely.app.medication.presentation.model.EditMedicationViewState
import com.migrely.app.medication.presentation.model.LogMedicationPresentationNotification
import com.migrely.app.medication.presentation.model.LogMedicationViewState
import com.migrely.app.medication.presentation.model.MedicationsPresentationNotification
import com.migrely.app.medication.presentation.model.MedicationsViewState
import com.migrely.app.medication.presentation.navigation.EditMedicationPresentationNavigationEvent
import com.migrely.app.medication.presentation.navigation.LogMedicationPresentationNavigationEvent
import com.migrely.app.medication.presentation.navigation.MedicationsPresentationNavigationEvent
import com.migrely.app.medication.presentation.viewmodel.EditMedicationViewModel
import com.migrely.app.medication.presentation.viewmodel.LogMedicationViewModel
import com.migrely.app.medication.presentation.viewmodel.MedicationsViewModel
import com.migrely.app.medication.ui.mapper.EditMedicationViewStateUiMapper
import com.migrely.app.medication.ui.mapper.LogMedicationViewStateUiMapper
import com.migrely.app.medication.ui.mapper.MedicationCategoryUiToPresentationMapper
import com.migrely.app.medication.ui.mapper.MedicationKindUiToPresentationMapper
import com.migrely.app.medication.ui.mapper.MedicationMessageBehaviourUiMapper
import com.migrely.app.medication.ui.mapper.MedicationTimeUiFormatter
import com.migrely.app.medication.ui.mapper.MedicationUiToPresentationMapper
import com.migrely.app.medication.ui.mapper.MedicationsViewStateUiMapper
import com.migrely.app.medication.ui.notification.MedicationEventPresenter

@Suppress("LongParameterList")
class LogMedicationDependencies(
    val logMedicationViewModel: LogMedicationViewModel,
    val logMedicationViewStateUiMapper: LogMedicationViewStateUiMapper,
    val medicationUiToPresentationMapper: MedicationUiToPresentationMapper,
    val medicationCategoryUiToPresentationMapper: MedicationCategoryUiToPresentationMapper,
    val medicationTimeUiFormatter: MedicationTimeUiFormatter,
    val medicationEventPresenter: MedicationEventPresenter,
    val medicationMessageBehaviourUiMapper: MedicationMessageBehaviourUiMapper,
    navigationMapper: NavigationEventDestinationMapper<LogMedicationPresentationNavigationEvent>,
    notificationMapper: NotificationUiMapper<LogMedicationPresentationNotification>
) : BaseComposeHolder<LogMedicationViewState, LogMedicationPresentationNotification>(
    logMedicationViewModel,
    navigationMapper,
    notificationMapper
)

class MedicationsDependencies(
    val medicationsViewModel: MedicationsViewModel,
    val medicationsViewStateUiMapper: MedicationsViewStateUiMapper,
    val medicationEventPresenter: MedicationEventPresenter,
    val medicationMessageBehaviourUiMapper: MedicationMessageBehaviourUiMapper,
    navigationMapper: NavigationEventDestinationMapper<MedicationsPresentationNavigationEvent>,
    notificationMapper: NotificationUiMapper<MedicationsPresentationNotification>
) : BaseComposeHolder<MedicationsViewState, MedicationsPresentationNotification>(
    medicationsViewModel,
    navigationMapper,
    notificationMapper
)

@Suppress("LongParameterList")
class EditMedicationDependencies(
    val editMedicationViewModel: EditMedicationViewModel,
    val editMedicationViewStateUiMapper: EditMedicationViewStateUiMapper,
    val medicationKindUiToPresentationMapper: MedicationKindUiToPresentationMapper,
    val medicationCategoryUiToPresentationMapper: MedicationCategoryUiToPresentationMapper,
    val medicationEventPresenter: MedicationEventPresenter,
    val medicationMessageBehaviourUiMapper: MedicationMessageBehaviourUiMapper,
    navigationMapper: NavigationEventDestinationMapper<EditMedicationPresentationNavigationEvent>,
    notificationMapper: NotificationUiMapper<EditMedicationPresentationNotification>
) : BaseComposeHolder<EditMedicationViewState, EditMedicationPresentationNotification>(
    editMedicationViewModel,
    navigationMapper,
    notificationMapper
)
