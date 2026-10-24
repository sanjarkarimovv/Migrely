package com.migrely.app.history.ui.notification

import com.migrely.app.history.ui.model.HistoryMessageUiModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Hands notification messages from [com.migrely.app.history.ui.mapper.HistoryNotificationUiMapper] to History, which
 * owns the snackbar. One instance is shared by the mapper and the screen through `HistoryDependencies`.
 */
class HistoryMessagePresenter {
    private val _messages =
        MutableSharedFlow<HistoryMessageUiModel>(extraBufferCapacity = MESSAGE_BUFFER)
    val messages: SharedFlow<HistoryMessageUiModel> = _messages.asSharedFlow()

    fun show(message: HistoryMessageUiModel) {
        _messages.tryEmit(message)
    }

    private companion object {
        const val MESSAGE_BUFFER = 8
    }
}
