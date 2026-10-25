package com.migrely.app.settings.presentation.viewmodel

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.architecture.presentation.viewmodel.BaseViewModel
import com.migrely.app.settings.domain.model.DeleteAllDataResultDomainModel
import com.migrely.app.settings.domain.usecase.DeleteAllDataUseCase
import com.migrely.app.settings.presentation.mapper.DeleteConfirmationResolver
import com.migrely.app.settings.presentation.model.DeleteAllDataPresentationNotification
import com.migrely.app.settings.presentation.model.DeleteAllDataViewState
import com.migrely.app.settings.presentation.navigation.DeleteAllDataPresentationNavigationEvent

/**
 * "Delete all data" (BRD EC-STO-03, D96), a screen of its own reached from Settings.
 *
 * Deleting can outlast a rotation, so this view model lives as long as the app and keeps, only for delivery, whether
 * the deletion is going on and how it ended: finished, or failed and not yet shown. A screen that comes back sees the
 * progress, is told of the failure it missed, or is sent on to the restart (ARCHITECTURE section 13).
 */
class DeleteAllDataViewModel(
    private val deleteAllDataUseCase: DeleteAllDataUseCase,
    private val deleteConfirmationResolver: DeleteConfirmationResolver,
    useCaseExecutor: UseCaseExecutor
) : BaseViewModel<DeleteAllDataViewState, DeleteAllDataPresentationNotification>(useCaseExecutor) {
    private var deleting = false
    private var allDeleted = false
    private var failureNotShown = false

    /** The screen is composed, after a rotation too, and listens: where the deletion stands, and what it missed. */
    fun onEnter() {
        updateViewState(state())
        if (failureNotShown) notify(DeleteAllDataPresentationNotification.NotDeleted)
        if (allDeleted) {
            emitNavigationEvent(DeleteAllDataPresentationNavigationEvent.OnAllDataDeleted)
        }
    }

    /** Leaving is not possible once deleting has started. */
    fun onBackAction() {
        if (deleting || allDeleted) return
        emitNavigationEvent(DeleteAllDataPresentationNavigationEvent.OnBack)
    }

    /** The user confirmed after typing [typed]; only the confirmation word, exactly, deletes. */
    fun onDeleteAction(typed: String) {
        if (!deleteConfirmationResolver.resolve(typed) || deleting || allDeleted) return
        deleting = true
        failureNotShown = false
        updateViewState(state())
        deleteAllDataUseCase(
            onResult = { result ->
                when (result) {
                    // The progress stays until the app starts again.
                    DeleteAllDataResultDomainModel.Deleted -> {
                        allDeleted = true
                        emitNavigationEvent(
                            DeleteAllDataPresentationNavigationEvent.OnAllDataDeleted
                        )
                    }

                    DeleteAllDataResultDomainModel.Failed -> notDeleted()
                }
            },
            onException = { notDeleted() }
        )
    }

    /** The screen showed that deleting failed, so it is not told again. */
    fun onFailureShownAction() {
        failureNotShown = false
    }

    private fun state() = if (deleting || allDeleted) {
        DeleteAllDataViewState.Deleting
    } else {
        DeleteAllDataViewState.Confirming
    }

    /** Kept until the screen takes it, so a rotation never loses it. */
    private fun notDeleted() {
        deleting = false
        failureNotShown = true
        updateViewState(state())
        notify(DeleteAllDataPresentationNotification.NotDeleted)
    }
}
