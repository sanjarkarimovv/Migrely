package com.migrely.app.history.ui.mapper

import com.migrely.app.history.presentation.model.HistoryMonthPresentationModel
import com.migrely.app.history.presentation.model.IntensityLevelPresentationModel
import com.migrely.app.history.presentation.model.MonthStatsPresentationModel
import com.migrely.app.history.presentation.model.ProgressPresentationModel
import com.migrely.app.history.ui.model.MonthStatsUiModel
import com.migrely.app.history.ui.model.PainLevelUiModel
import com.migrely.app.history.ui.model.ProgressAgainstUiModel
import com.migrely.app.history.ui.model.ProgressTrendUiModel
import com.migrely.app.history.ui.model.ProgressUiModel
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Test

private val AUGUST = YearMonth.of(2026, 8)

class HistoryMonthUiMapperStatsTest {
    private val classUnderTest = HistoryMonthUiMapper()

    private fun mapped(
        averagePeak: IntensityLevelPresentationModel?,
        progress: ProgressPresentationModel?
    ): MonthStatsUiModel = classUnderTest.toUi(
        HistoryMonthPresentationModel(
            month = YearMonth.of(2026, 9),
            today = LocalDate.of(2026, 10, 24),
            hasPreviousMonth = true,
            hasNextMonth = true,
            days = emptyList(),
            attacks = emptyList(),
            stats = MonthStatsPresentationModel(4, 6, 9, averagePeak, 5, chronic = true, progress)
        )
    ).stats

    @Test
    fun `When mapped then the numbers, the average peak and fewer days against a whole month`() {
        assertEquals(
            MonthStatsUiModel(
                migraines = 4,
                migraineDays = 6,
                painFreeDays = 9,
                averagePeak = PainLevelUiModel.Severe,
                attackMedicationDays = 5,
                chronic = true,
                progress = ProgressUiModel(
                    ProgressTrendUiModel.Fewer,
                    3,
                    ProgressAgainstUiModel.WholeMonth(2026, 8)
                )
            ),
            mapped(
                IntensityLevelPresentationModel.Severe,
                ProgressPresentationModel.Fewer(
                    3,
                    ProgressPresentationModel.ComparedWith.WholeMonth(AUGUST)
                )
            )
        )
    }

    @Test
    fun `Given more or as many days against the same dates when mapped then that trend`() {
        val sameDays = ProgressPresentationModel.ComparedWith.SameDaysLastMonth

        assertEquals(
            ProgressUiModel(ProgressTrendUiModel.More, 2, ProgressAgainstUiModel.SameDaysLastMonth),
            mapped(null, ProgressPresentationModel.More(2, sameDays)).progress
        )
        assertEquals(
            ProgressUiModel(ProgressTrendUiModel.Same, 0, ProgressAgainstUiModel.SameDaysLastMonth),
            mapped(null, ProgressPresentationModel.Same(sameDays)).progress
        )
        assertEquals(null, mapped(null, null).averagePeak)
    }
}
