package com.migrely.app.attack.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class IntensityPointUiModel(val atEpochMillis: Long, val value: Int) : Parcelable
