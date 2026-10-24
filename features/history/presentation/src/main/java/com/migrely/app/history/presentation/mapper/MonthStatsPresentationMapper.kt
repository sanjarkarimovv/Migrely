package com.migrely.app.history.presentation.mapper

import com.migrely.app.history.domain.model.MonthStatsDomainModel
import com.migrely.app.history.domain.model.ProgressDomainModel
import com.migrely.app.history.presentation.model.MonthStatsPresentationModel
import com.migrely.app.history.presentation.model.ProgressPresentationModel

class MonthStatsPresentationMapper(
    private val painLevelPresentationMapper: PainLevelPresentationMapper
) {
    fun toPresentation(stats: MonthStatsDomainModel) = MonthStatsPresentationModel(
        migraines = stats.migraines,
        migraineDays = stats.migraineDays,
        painFreeDays = stats.painFreeDays,
        averagePeak = stats.averagePeak?.let(painLevelPresentationMapper::toPresentation),
        attackMedicationDays = stats.attackMedicationDays,
        chronic = stats.chronic,
        progress = stats.progress?.toPresentation()
    )

    private fun ProgressDomainModel.toPresentation(): ProgressPresentationModel {
        val comparedWith = when (val against = comparedWith) {
            is ProgressDomainModel.ComparedWith.WholeMonth ->
                ProgressPresentationModel.ComparedWith.WholeMonth(against.month)
            ProgressDomainModel.ComparedWith.SameDaysLastMonth ->
                ProgressPresentationModel.ComparedWith.SameDaysLastMonth
        }
        return when {
            difference < 0 -> ProgressPresentationModel.Fewer(-difference, comparedWith)
            difference > 0 -> ProgressPresentationModel.More(difference, comparedWith)
            else -> ProgressPresentationModel.Same(comparedWith)
        }
    }
}
