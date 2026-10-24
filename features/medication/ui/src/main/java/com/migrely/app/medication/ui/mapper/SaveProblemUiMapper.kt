package com.migrely.app.medication.ui.mapper

import com.migrely.app.medication.presentation.model.SaveProblemPresentationModel
import com.migrely.app.medication.ui.model.SaveProblemUiModel

class SaveProblemUiMapper {
    fun toUi(problem: SaveProblemPresentationModel): SaveProblemUiModel = when (problem) {
        is SaveProblemPresentationModel.Duplicate -> SaveProblemUiModel.Duplicate(
            problem.existingName
        )
        is SaveProblemPresentationModel.ArchivedName ->
            SaveProblemUiModel.ArchivedName(problem.archivedName)
        SaveProblemPresentationModel.LimitReached -> SaveProblemUiModel.LimitReached
        SaveProblemPresentationModel.InvalidName -> SaveProblemUiModel.InvalidName
        SaveProblemPresentationModel.InvalidDose -> SaveProblemUiModel.InvalidDose
    }
}
