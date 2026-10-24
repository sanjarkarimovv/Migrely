package com.migrely.app.backup.ui.mapper

import com.migrely.app.backup.presentation.model.YourDataViewState
import com.migrely.app.backup.ui.model.YourDataViewStateUiModel

class YourDataViewStateUiMapper {
    fun toUi(viewState: YourDataViewState) = when (viewState) {
        YourDataViewState.Idle -> YourDataViewStateUiModel.Idle
        YourDataViewState.Working -> YourDataViewStateUiModel.Working
    }
}
