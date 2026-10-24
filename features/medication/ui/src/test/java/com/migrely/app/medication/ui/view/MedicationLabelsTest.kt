package com.migrely.app.medication.ui.view

import com.migrely.app.medication.ui.model.MedicationCategoryUiModel
import com.migrely.app.medication.ui.model.MedicationKindUiModel
import com.migrely.app.medication.ui.model.MedicationUiModel
import org.junit.Assert.assertEquals
import org.junit.Test

class MedicationLabelsTest {
    private fun medication(dose: String?) = MedicationUiModel(
        id = "m1",
        name = "Ibuprofen",
        kind = MedicationKindUiModel.Acute,
        category = MedicationCategoryUiModel.SimplePainkiller,
        defaultDose = dose
    )

    @Test
    fun `Given a dose when titled then a middle dot to see and a comma to hear`() {
        assertEquals("Ibuprofen · 400 mg", medication("400 mg").title())
        assertEquals("Ibuprofen, 400 mg", medication("400 mg").spokenTitle())
    }

    @Test
    fun `Given no dose when titled then the name alone`() {
        assertEquals("Ibuprofen", medication(null).title())
        assertEquals("Ibuprofen", medication(null).spokenTitle())
    }
}
