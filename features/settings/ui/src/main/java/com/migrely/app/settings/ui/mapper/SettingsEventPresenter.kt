package com.migrely.app.settings.ui.mapper

import com.migrely.app.settings.ui.model.SettingsEventUiModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** Tells the screen a setting was not saved, or that Share or Rate was chosen. */
class SettingsEventPresenter {
    private val _events =
        MutableSharedFlow<SettingsEventUiModel>(extraBufferCapacity = EVENT_BUFFER)
    val events: SharedFlow<SettingsEventUiModel> = _events.asSharedFlow()

    fun show(event: SettingsEventUiModel) {
        _events.tryEmit(event)
    }

    private companion object {
        const val EVENT_BUFFER = 8
    }
}
