package com.migrely.app.reminders.ui.model

/** What the screen does in reply to the view model: ask the system for permission, or show a message. */
sealed interface RemindersEventUiModel {
    data class AskForPermission(val kind: ReminderKindUiModel) : RemindersEventUiModel

    data class Message(val message: RemindersMessageUiModel) : RemindersEventUiModel
}

enum class RemindersMessageUiModel {
    StorageFull,
    SomethingWentWrong
}
