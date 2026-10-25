package com.migrely.app.settings.ui.mapper

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** Tells the screen that deleting all data stopped partway. */
class DeleteAllDataEventPresenter {
    private val _notDeleted = MutableSharedFlow<Unit>(extraBufferCapacity = EVENT_BUFFER)
    val notDeleted: SharedFlow<Unit> = _notDeleted.asSharedFlow()

    fun showNotDeleted() {
        _notDeleted.tryEmit(Unit)
    }

    private companion object {
        const val EVENT_BUFFER = 8
    }
}
