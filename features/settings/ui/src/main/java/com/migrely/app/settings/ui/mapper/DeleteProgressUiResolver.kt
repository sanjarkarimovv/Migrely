package com.migrely.app.settings.ui.mapper

import com.migrely.app.settings.ui.model.DeleteAllDataViewStateUiModel

/**
 * Whether the screen shows its progress, and so cannot be left (BRD D96): from the moment the user confirms until the
 * app starts again, or until the deletion fails. [started] is the screen's own, kept across a rotation, and covers the
 * moments before the view model's state arrives; the view model's covers a screen rebuilt while it deletes. A
 * finished deletion keeps the progress: only a failure ends it.
 */
class DeleteProgressUiResolver {
    fun resolve(started: Boolean, state: DeleteAllDataViewStateUiModel): Boolean = when (state) {
        DeleteAllDataViewStateUiModel.Deleting -> true
        DeleteAllDataViewStateUiModel.Confirming -> started
    }
}
