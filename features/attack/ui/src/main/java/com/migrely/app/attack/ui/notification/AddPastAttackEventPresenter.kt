package com.migrely.app.attack.ui.notification

import com.migrely.app.attack.ui.model.AddPastAttackEventUiModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Hands events from [com.migrely.app.attack.ui.mapper.AddPastAttackNotificationUiMapper] to the add screen. One
 * instance is shared through `AddPastAttackDependencies`.
 */
class AddPastAttackEventPresenter {
    private val _events = MutableSharedFlow<AddPastAttackEventUiModel>(
        extraBufferCapacity = EVENT_BUFFER
    )
    val events: SharedFlow<AddPastAttackEventUiModel> = _events.asSharedFlow()

    fun show(event: AddPastAttackEventUiModel) {
        _events.tryEmit(event)
    }

    private companion object {
        const val EVENT_BUFFER = 8
    }
}
