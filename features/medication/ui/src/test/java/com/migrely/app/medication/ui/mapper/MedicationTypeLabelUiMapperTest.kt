package com.migrely.app.medication.ui.mapper

import com.migrely.app.medication.ui.R
import com.migrely.app.medication.ui.model.MedicationCategoryUiModel
import com.migrely.app.medication.ui.model.MedicationKindUiModel
import com.migrely.app.medication.ui.model.MedicationUiModel
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

private fun medication(kind: MedicationKindUiModel, category: MedicationCategoryUiModel?) =
    MedicationUiModel(
        id = "m1",
        name = "Ibuprofen",
        kind = kind,
        category = category,
        defaultDose = null
    )

@RunWith(Parameterized::class)
class MedicationTypeLabelUiMapperTest(
    private val medication: MedicationUiModel,
    private val expected: Int
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0} is labelled {1}")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(
                medication(MedicationKindUiModel.Acute, MedicationCategoryUiModel.SimplePainkiller),
                R.string.category_simple
            ),
            arrayOf(
                medication(MedicationKindUiModel.Acute, MedicationCategoryUiModel.Triptan),
                R.string.category_triptan
            ),
            arrayOf(
                medication(
                    MedicationKindUiModel.Acute,
                    MedicationCategoryUiModel.CombinationOrOther
                ),
                R.string.category_combination
            ),
            arrayOf(medication(MedicationKindUiModel.Acute, null), R.string.category_not_sure),
            // The editor's "Preventive", not a lower-case label of its own (store polish).
            arrayOf(medication(MedicationKindUiModel.Preventive, null), R.string.kind_preventive)
        )
    }

    private val classUnderTest = MedicationTypeLabelUiMapper(MedicationCategoryLabelUiMapper())

    @Test
    fun `When labelled then the editor's sentence-case words`() {
        assertEquals(expected, classUnderTest.toUi(medication))
    }
}
