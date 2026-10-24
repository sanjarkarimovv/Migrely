package com.migrely.app.attack.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CustomTagUiModel(
    val id: String,
    val kind: TagKindUiModel,
    val name: String,
    val archived: Boolean
) : Parcelable
