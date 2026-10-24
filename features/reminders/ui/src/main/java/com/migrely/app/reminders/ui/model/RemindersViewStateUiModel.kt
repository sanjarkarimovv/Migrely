package com.migrely.app.reminders.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface RemindersViewStateUiModel : Parcelable {
    @Parcelize
    data object Loading : RemindersViewStateUiModel

    @Parcelize
    data class Ready(val reminders: RemindersUiModel) : RemindersViewStateUiModel

    @Parcelize
    data object Unavailable : RemindersViewStateUiModel
}
