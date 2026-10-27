package com.migrely.app.attack.ui.mapper

import com.migrely.app.attack.presentation.model.TimesUndoPresentationModel
import com.migrely.app.attack.ui.model.TimesUndoUiModel
import java.time.Instant

class TimesUndoUiToPresentationMapper(
    private val intensityPointUiToPresentationMapper: IntensityPointUiToPresentationMapper
) {
    fun toPresentation(undo: TimesUndoUiModel) = TimesUndoPresentationModel(
        attackId = undo.attackId,
        start = Instant.ofEpochMilli(undo.startEpochMillis),
        end = undo.endEpochMillis?.let(Instant::ofEpochMilli),
        removedPoints = undo.removedPoints.map(intensityPointUiToPresentationMapper::toPresentation)
    )
}
