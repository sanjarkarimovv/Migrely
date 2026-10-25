package com.migrely.app.medication.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.migrely.app.medication.ui.R
import com.migrely.app.medication.ui.mapper.MedicationCategoryLabelUiMapper
import com.migrely.app.medication.ui.mapper.MedicationTypeLabelUiMapper
import com.migrely.app.medication.ui.model.MedicationCategoryUiModel
import com.migrely.app.medication.ui.model.MedicationUiModel
import com.migrely.app.medication.ui.model.SaveProblemUiModel

private val categoryLabels = MedicationCategoryLabelUiMapper()
private val typeLabels = MedicationTypeLabelUiMapper(categoryLabels)

@Composable
internal fun categoryLabel(category: MedicationCategoryUiModel?): String =
    stringResource(categoryLabels.toUi(category))

@Composable
internal fun categoryExamples(category: MedicationCategoryUiModel?): String? = when (category) {
    MedicationCategoryUiModel.SimplePainkiller -> stringResource(R.string.category_simple_examples)
    MedicationCategoryUiModel.Triptan -> stringResource(R.string.category_triptan_examples)
    MedicationCategoryUiModel.CombinationOrOther ->
        stringResource(R.string.category_combination_examples)
    null -> null
}

/** "Ibuprofen · 400 mg", or the name alone without a dose. */
internal fun MedicationUiModel.title(): String =
    listOfNotNull(name, defaultDose).joinToString(" · ")

/**
 * The title as screen readers say it: "Ibuprofen, 400 mg". A comma gives a pause, while a middle dot is read aloud
 * or skipped, depending on the punctuation setting (QA #29).
 */
internal fun MedicationUiModel.spokenTitle(): String =
    listOfNotNull(name, defaultDose).joinToString(", ")

/** What a medication is, for lists and screen readers: its type, or "Preventive". */
@Composable
internal fun MedicationUiModel.typeLabel(): String = stringResource(typeLabels.toUi(this))

@Composable
internal fun problemText(problem: SaveProblemUiModel): String = when (problem) {
    is SaveProblemUiModel.Duplicate -> stringResource(
        R.string.problem_duplicate,
        problem.existingName
    )
    is SaveProblemUiModel.ArchivedName -> stringResource(
        R.string.problem_archived_name,
        problem.archivedName
    )
    SaveProblemUiModel.InvalidName -> stringResource(R.string.problem_invalid_name)
    SaveProblemUiModel.InvalidDose -> stringResource(R.string.problem_invalid_dose)
    SaveProblemUiModel.LimitReached -> stringResource(R.string.message_limit_reached)
}

/** The four choices for an acute medication's type, "Not sure" last (BRD MED-F2, EC-MED-04). */
internal val CATEGORY_CHOICES: List<MedicationCategoryUiModel?> =
    MedicationCategoryUiModel.entries + listOf(null)
