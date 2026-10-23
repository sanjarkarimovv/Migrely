package com.migrely.app.home.presentation.viewmodel

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.architecture.presentation.notification.PresentationNotification
import com.migrely.app.architecture.presentation.viewmodel.BaseViewModel
import com.migrely.app.home.presentation.model.HomeViewState
import com.migrely.app.home.presentation.model.HomeViewState.Ready

class HomeViewModel(useCaseExecutor: UseCaseExecutor) :
    BaseViewModel<HomeViewState, PresentationNotification>(useCaseExecutor) {
    fun onEnter() {
        updateViewState(Ready)
    }
}
