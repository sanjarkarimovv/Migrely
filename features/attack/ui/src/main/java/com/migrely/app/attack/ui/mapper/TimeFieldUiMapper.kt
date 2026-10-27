package com.migrely.app.attack.ui.mapper

import com.migrely.app.attack.presentation.model.TimeFieldPresentationModel
import com.migrely.app.attack.ui.model.TimeFieldUiModel

class TimeFieldUiMapper {
    fun toUi(timeField: TimeFieldPresentationModel): TimeFieldUiModel = when (timeField) {
        TimeFieldPresentationModel.Start -> TimeFieldUiModel.Start
        TimeFieldPresentationModel.End -> TimeFieldUiModel.End
    }
}
