package com.migrely.app.attack.ui.notification

import com.migrely.app.attack.ui.model.AttackDetailsEventUiModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Hands events from [com.migrely.app.attack.ui.mapper.AttackNotificationUiMapper] to the details screen, which shows
 * them as inline errors, the safety card or a snackbar. One instance is shared through `AttackDetailsDependencies`.
 */
class AttackDetailsEventPresenter {
    private val _events = MutableSharedFlow<AttackDetailsEventUiModel>(
        extraBufferCapacity = EVENT_BUFFER
    )
    val events: SharedFlow<AttackDetailsEventUiModel> = _events.asSharedFlow()

    fun show(event: AttackDetailsEventUiModel) {
        _events.tryEmit(event)
    }

    private companion object {
        const val EVENT_BUFFER = 8
    }
}
