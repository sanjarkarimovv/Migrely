package com.migrely.app.medication.presentation.viewmodel

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.architecture.presentation.viewmodel.BaseViewModel
import com.migrely.app.medication.domain.usecase.GetOveruseStatusUseCase
import com.migrely.app.medication.presentation.mapper.OveruseIndicatorPresentationMapper
import com.migrely.app.medication.presentation.model.OveruseIndicatorPresentationNotification
import com.migrely.app.medication.presentation.model.OveruseIndicatorViewState

/** History's standing overuse indicator (BRD EC-MED-06, D59), placed by `:app` in History's month summary. */
class OveruseIndicatorViewModel(
    private val getOveruseStatusUseCase: GetOveruseStatusUseCase,
    private val overuseIndicatorPresentationMapper: OveruseIndicatorPresentationMapper,
    useCaseExecutor: UseCaseExecutor
) : BaseViewModel<OveruseIndicatorViewState, OveruseIndicatorPresentationNotification>(
    useCaseExecutor
) {
    fun onEnter() {
        getOveruseStatusUseCase(
            onResult = { status ->
                updateViewState(overuseIndicatorPresentationMapper.toPresentation(status))
            }
        )
    }
}
