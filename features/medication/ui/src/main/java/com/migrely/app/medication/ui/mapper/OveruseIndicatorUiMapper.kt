package com.migrely.app.medication.ui.mapper

import com.migrely.app.medication.presentation.model.OveruseIndicatorViewState
import com.migrely.app.medication.ui.model.OveruseIndicatorRowUiModel
import com.migrely.app.medication.ui.model.OveruseIndicatorUiModel

class OveruseIndicatorUiMapper(private val overuseCategoryUiMapper: OveruseCategoryUiMapper) {
    fun toUi(viewState: OveruseIndicatorViewState) = OveruseIndicatorUiModel(
        rows = when (viewState) {
            OveruseIndicatorViewState.Hidden -> emptyList()
            is OveruseIndicatorViewState.Shown -> viewState.rows.map { row ->
                OveruseIndicatorRowUiModel(
                    category = overuseCategoryUiMapper.toUi(row.category),
                    days = row.days,
                    limit = row.limit,
                    reached = row.reached
                )
            }
        }
    )
}
