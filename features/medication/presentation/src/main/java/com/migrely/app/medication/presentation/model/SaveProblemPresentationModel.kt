package com.migrely.app.medication.presentation.model

/** Why a medication could not be saved (BRD EC-MED-07, D35). */
sealed interface SaveProblemPresentationModel {
    data class Duplicate(val existingName: String) : SaveProblemPresentationModel

    data class ArchivedName(val archivedName: String) : SaveProblemPresentationModel

    data object LimitReached : SaveProblemPresentationModel

    data object InvalidName : SaveProblemPresentationModel

    data object InvalidDose : SaveProblemPresentationModel
}
