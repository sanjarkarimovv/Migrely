package com.migrely.app.medication.presentation.mapper

import com.migrely.app.medication.domain.model.OveruseStatusResultDomainModel
import com.migrely.app.medication.presentation.model.OveruseIndicatorRowPresentationModel
import com.migrely.app.medication.presentation.model.OveruseIndicatorViewState

class OveruseIndicatorPresentationMapper(
    private val overuseCategoryPresentationMapper: OveruseCategoryPresentationMapper
) {
    fun toPresentation(status: OveruseStatusResultDomainModel): OveruseIndicatorViewState =
        when (status) {
            is OveruseStatusResultDomainModel.Loaded -> if (status.rows.isEmpty()) {
                OveruseIndicatorViewState.Hidden
            } else {
                OveruseIndicatorViewState.Shown(
                    status.rows.map { row ->
                        OveruseIndicatorRowPresentationModel(
                            category = overuseCategoryPresentationMapper.toPresentation(
                                row.category
                            ),
                            days = row.days,
                            limit = row.limit,
                            reached = row.reached
                        )
                    }
                )
            }
            OveruseStatusResultDomainModel.Failed -> OveruseIndicatorViewState.Hidden
        }
}
