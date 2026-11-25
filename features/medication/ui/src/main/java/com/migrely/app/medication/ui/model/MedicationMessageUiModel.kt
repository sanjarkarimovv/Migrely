package com.migrely.app.medication.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** Snackbar messages of the medication screens; Parcelable so a message on screen survives rotation. */
sealed interface MedicationMessageUiModel : Parcelable {
    /** Offers "Undo" for the archive (BRD EC-MED-01). */
    @Parcelize
    data class Archived(val medicationId: String) : MedicationMessageUiModel

    @Parcelize
    data object Undone : MedicationMessageUiModel

    @Parcelize
    data object IntakeUndone : MedicationMessageUiModel

    /** "Undo" found the disk full; the intake is still stored (BRD D190 (2)). */
    @Parcelize
    data object IntakeKeptStorageFull : MedicationMessageUiModel

    /** "Undo" failed another way; the intake is still stored (BRD D190 (2)). */
    @Parcelize
    data object IntakeKeptFailed : MedicationMessageUiModel

    @Parcelize
    data object InFuture : MedicationMessageUiModel

    @Parcelize
    data object LimitReached : MedicationMessageUiModel

    @Parcelize
    data object StorageFull : MedicationMessageUiModel

    @Parcelize
    data object SomethingWentWrong : MedicationMessageUiModel
}
