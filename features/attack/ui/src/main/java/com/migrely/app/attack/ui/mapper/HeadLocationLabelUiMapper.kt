package com.migrely.app.attack.ui.mapper

import androidx.annotation.StringRes
import com.migrely.app.attack.ui.R
import com.migrely.app.attack.ui.model.HeadLocationUiModel

class HeadLocationLabelUiMapper {
    @StringRes
    fun toUi(headLocation: HeadLocationUiModel): Int = when (headLocation) {
        HeadLocationUiModel.Left -> R.string.location_left
        HeadLocationUiModel.Right -> R.string.location_right
        HeadLocationUiModel.BothSides -> R.string.location_both_sides
        HeadLocationUiModel.Forehead -> R.string.location_forehead
        HeadLocationUiModel.BackOfHead -> R.string.location_back_of_head
        HeadLocationUiModel.AroundEyes -> R.string.location_around_eyes
        HeadLocationUiModel.WholeHead -> R.string.location_whole_head
    }
}
