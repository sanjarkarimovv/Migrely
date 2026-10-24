package com.migrely.app.medication.ui.notification

import com.migrely.app.medication.ui.model.MedicationEventUiModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** Hands events from a screen's notification mapper to that screen. One instance per screen's `Dependencies`. */
class MedicationEventPresenter {
    private val _events =
        MutableSharedFlow<MedicationEventUiModel>(extraBufferCapacity = EVENT_BUFFER)
    val events: SharedFlow<MedicationEventUiModel> = _events.asSharedFlow()

    fun show(event: MedicationEventUiModel) {
        _events.tryEmit(event)
    }

    private companion object {
        const val EVENT_BUFFER = 8
    }
}
