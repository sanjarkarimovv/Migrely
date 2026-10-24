package com.migrely.app.settings.presentation.mapper

/**
 * Whether what the user typed confirms "Delete all data" (BRD D96): [word] exactly, in capitals, with no spaces around
 * it. The screen shows [word] in its hint and turns the button on by [resolve]; the view model checks again.
 */
class DeleteConfirmationResolver {
    val word = "DELETE"

    fun resolve(typed: String): Boolean = typed == word
}
