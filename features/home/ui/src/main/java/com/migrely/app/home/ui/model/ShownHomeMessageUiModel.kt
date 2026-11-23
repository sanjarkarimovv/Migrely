package com.migrely.app.home.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** The message on screen and when it appeared, saved across rotation; the countdown is keyed to it (BRD D182). */
@Parcelize
data class ShownHomeMessageUiModel(val message: HomeMessageUiModel, val shownAtEpochMillis: Long) :
    Parcelable
