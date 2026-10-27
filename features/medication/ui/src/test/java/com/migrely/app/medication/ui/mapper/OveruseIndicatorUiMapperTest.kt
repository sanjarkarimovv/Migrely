package com.migrely.app.medication.ui.mapper

import com.migrely.app.medication.presentation.model.OveruseCategoryPresentationModel
import com.migrely.app.medication.presentation.model.OveruseIndicatorRowPresentationModel
import com.migrely.app.medication.presentation.model.OveruseIndicatorViewState
import com.migrely.app.medication.ui.model.OveruseCategoryUiModel
import com.migrely.app.medication.ui.model.OveruseIndicatorRowUiModel
import com.migrely.app.medication.ui.model.OveruseIndicatorUiModel
import org.junit.Assert.assertEquals
import org.junit.Test

class OveruseIndicatorUiMapperTest {
    private val classUnderTest = OveruseIndicatorUiMapper(OveruseCategoryUiMapper())

    @Test
    fun `When mapped then each row with its category, days, limit and whether it is reached`() {
        assertEquals(
            OveruseIndicatorUiModel(
                listOf(
                    OveruseIndicatorRowUiModel(
                        OveruseCategoryUiModel.CombinationOrOther,
                        3,
                        10,
                        false
                    ),
                    OveruseIndicatorRowUiModel(OveruseCategoryUiModel.Multiple, 12, 10, true)
                )
            ),
            classUnderTest.toUi(
                OveruseIndicatorViewState.Shown(
                    listOf(
                        OveruseIndicatorRowPresentationModel(
                            OveruseCategoryPresentationModel.CombinationOrOther,
                            days = 3,
                            limit = 10,
                            reached = false
                        ),
                        OveruseIndicatorRowPresentationModel(
                            OveruseCategoryPresentationModel.Multiple,
                            days = 12,
                            limit = 10,
                            reached = true
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Given hidden when mapped then no rows`() {
        assertEquals(
            OveruseIndicatorUiModel(emptyList()),
            classUnderTest.toUi(OveruseIndicatorViewState.Hidden)
        )
    }
}
