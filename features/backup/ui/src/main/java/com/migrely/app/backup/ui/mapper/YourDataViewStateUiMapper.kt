package com.migrely.app.backup.ui.mapper

import com.migrely.app.backup.presentation.model.YourDataViewState
import com.migrely.app.backup.ui.model.YourDataViewStateUiModel

class YourDataViewStateUiMapper(private val automaticCopyUiMapper: AutomaticCopyUiMapper) {
    fun toUi(viewState: YourDataViewState) = YourDataViewStateUiModel(
        working = when (viewState) {
            is YourDataViewState.Idle -> false
            is YourDataViewState.Working -> true
        },
        automaticCopy = viewState.automaticCopy?.let(automaticCopyUiMapper::toUi)
    )
}
