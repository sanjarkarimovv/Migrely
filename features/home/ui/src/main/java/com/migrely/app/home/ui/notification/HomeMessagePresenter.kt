package com.migrely.app.home.ui.notification

import com.migrely.app.home.ui.model.HomeMessageUiModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Hands notification messages from [com.migrely.app.home.ui.mapper.HomeNotificationUiMapper] to the home screen, which
 * owns the snackbar. One instance is shared by the mapper and the screen through `HomeDependencies`.
 */
class HomeMessagePresenter {
    private val _messages =
        MutableSharedFlow<HomeMessageUiModel>(extraBufferCapacity = MESSAGE_BUFFER)
    val messages: SharedFlow<HomeMessageUiModel> = _messages.asSharedFlow()

    fun show(message: HomeMessageUiModel) {
        _messages.tryEmit(message)
    }

    private companion object {
        const val MESSAGE_BUFFER = 8
    }
}
