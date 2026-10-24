package com.migrely.app.settings.ui.mapper

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** Tells the screen a setting was not saved, or that deleting all data stopped partway. */
class SettingsEventPresenter {
    private val _notSaved = MutableSharedFlow<Unit>(extraBufferCapacity = EVENT_BUFFER)
    val notSaved: SharedFlow<Unit> = _notSaved.asSharedFlow()

    private val _notDeleted = MutableSharedFlow<Unit>(extraBufferCapacity = EVENT_BUFFER)
    val notDeleted: SharedFlow<Unit> = _notDeleted.asSharedFlow()

    fun showNotSaved() {
        _notSaved.tryEmit(Unit)
    }

    fun showNotDeleted() {
        _notDeleted.tryEmit(Unit)
    }

    private companion object {
        const val EVENT_BUFFER = 8
    }
}
