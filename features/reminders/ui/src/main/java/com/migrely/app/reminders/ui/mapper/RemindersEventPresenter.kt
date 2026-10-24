package com.migrely.app.reminders.ui.mapper

import com.migrely.app.reminders.ui.model.RemindersEventUiModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class RemindersEventPresenter {
    private val _events =
        MutableSharedFlow<RemindersEventUiModel>(extraBufferCapacity = EVENT_BUFFER)
    val events: SharedFlow<RemindersEventUiModel> = _events.asSharedFlow()

    fun show(event: RemindersEventUiModel) {
        _events.tryEmit(event)
    }

    private companion object {
        const val EVENT_BUFFER = 8
    }
}
