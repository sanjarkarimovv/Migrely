package com.migrely.app.attack.ui.view

import com.migrely.app.attack.presentation.model.AttackDetailsViewState
import com.migrely.app.attack.presentation.viewmodel.AttackDetailsViewModel
import com.migrely.app.attack.ui.di.AttackDetailsDependencies
import com.migrely.app.attack.ui.mapper.AttackDetailsViewStateUiMapper
import com.migrely.app.attack.ui.mapper.AttackMessageBehaviourUiMapper
import com.migrely.app.attack.ui.model.AttackDetailsViewStateUiModel
import com.migrely.app.attack.ui.notification.AttackDetailsEventPresenter
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

/**
 * The whole details screen on the preview attack, with a ViewModel that does nothing: tests drive it through [events],
 * as the ViewModel's notifications do.
 */
internal fun attackDetailsTestDependencies(
    events: AttackDetailsEventPresenter
): AttackDetailsDependencies {
    val viewModel: AttackDetailsViewModel = mock {
        on { viewState } doReturn flowOf(AttackDetailsViewState.Loading)
        on { notification } doReturn emptyFlow()
        on { navigationEvent } doReturn emptyFlow()
    }
    val viewStateMapper: AttackDetailsViewStateUiMapper = mock {
        on { toUi(any()) } doReturn AttackDetailsViewStateUiModel.Ready(previewDetails())
    }
    return AttackDetailsDependencies(
        attackDetailsViewModel = viewModel,
        attackDetailsViewStateUiMapper = viewStateMapper,
        attackTimeUiFormatter = previewTimeFormatter,
        attackDetailsEventPresenter = events,
        attackMessageBehaviourUiMapper = AttackMessageBehaviourUiMapper(),
        labels = AttackLabels.Default,
        attackTagUiToPresentationMapper = mock(),
        functionalImpactUiToPresentationMapper = mock(),
        tagKindUiToPresentationMapper = mock(),
        intensityPointUiToPresentationMapper = mock(),
        timesUndoUiToPresentationMapper = mock(),
        attackIntakeUiToPresentationMapper = mock(),
        attackDetailsNavigationMapper = mock(),
        attackNotificationMapper = mock()
    )
}
