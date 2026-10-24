package com.migrely.app.backup.ui.mapper

import com.migrely.app.backup.ui.model.YourDataEventUiModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class YourDataEventPresenter {
    private val _events =
        MutableSharedFlow<YourDataEventUiModel>(extraBufferCapacity = EVENT_BUFFER)
    val events: SharedFlow<YourDataEventUiModel> = _events.asSharedFlow()

    fun show(event: YourDataEventUiModel) {
        _events.tryEmit(event)
    }

    private companion object {
        const val EVENT_BUFFER = 8
    }
}
