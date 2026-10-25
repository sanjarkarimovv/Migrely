package com.migrely.app.settings.ui.mapper

import com.migrely.app.settings.presentation.model.DeleteAllDataViewState
import com.migrely.app.settings.ui.model.DeleteAllDataViewStateUiModel

class DeleteAllDataViewStateUiMapper {
    fun toUi(viewState: DeleteAllDataViewState) = when (viewState) {
        DeleteAllDataViewState.Confirming -> DeleteAllDataViewStateUiModel.Confirming
        DeleteAllDataViewState.Deleting -> DeleteAllDataViewStateUiModel.Deleting
    }
}
