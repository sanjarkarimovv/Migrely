package com.migrely.app.attack.ui.mapper

import com.migrely.app.attack.presentation.model.TimeFieldPresentationModel
import com.migrely.app.attack.ui.model.TimeFieldUiModel

class TimeFieldUiToPresentationMapper {
    fun toPresentation(timeField: TimeFieldUiModel): TimeFieldPresentationModel = when (timeField) {
        TimeFieldUiModel.Start -> TimeFieldPresentationModel.Start
        TimeFieldUiModel.End -> TimeFieldPresentationModel.End
    }
}
