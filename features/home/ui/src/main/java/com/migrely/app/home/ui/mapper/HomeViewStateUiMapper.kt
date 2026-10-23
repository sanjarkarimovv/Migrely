package com.migrely.app.home.ui.mapper

import com.migrely.app.home.presentation.model.HomeViewState
import com.migrely.app.home.ui.model.HomeViewStateUiModel

class HomeViewStateUiMapper {
    fun toUi(viewState: HomeViewState): HomeViewStateUiModel = when (viewState) {
        HomeViewState.Loading -> HomeViewStateUiModel.Loading
        HomeViewState.Ready -> HomeViewStateUiModel.Ready
    }
}
