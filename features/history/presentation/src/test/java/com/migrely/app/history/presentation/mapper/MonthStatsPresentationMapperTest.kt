package com.migrely.app.history.presentation.mapper

import com.migrely.app.history.domain.model.MonthStatsDomainModel
import com.migrely.app.history.domain.model.PainLevelDomainModel
import com.migrely.app.history.domain.model.ProgressDomainModel
import com.migrely.app.history.presentation.model.IntensityLevelPresentationModel
import com.migrely.app.history.presentation.model.MonthStatsPresentationModel
import com.migrely.app.history.presentation.model.ProgressPresentationModel
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Test

private val AUGUST = YearMonth.of(2026, 8)

class MonthStatsPresentationMapperTest {
    private val classUnderTest = MonthStatsPresentationMapper(PainLevelPresentationMapper())

    private fun stats(
        averagePeak: PainLevelDomainModel? = PainLevelDomainModel.Moderate,
        progress: ProgressDomainModel? = null
    ) = MonthStatsDomainModel(4, 6, 9, averagePeak, 5, chronic = true, progress = progress)

    @Test
    fun `When mapped then the numbers and the average peak`() {
        assertEquals(
            MonthStatsPresentationModel(
                migraines = 4,
                migraineDays = 6,
                painFreeDays = 9,
                averagePeak = IntensityLevelPresentationModel.Moderate,
                attackMedicationDays = 5,
                chronic = true,
                progress = null
            ),
            classUnderTest.toPresentation(stats())
        )
        assertEquals(null, classUnderTest.toPresentation(stats(averagePeak = null)).averagePeak)
    }

    @Test
    fun `Given fewer, more or as many migraine days when mapped then the matching progress`() {
        val wholeAugust = ProgressDomainModel.ComparedWith.WholeMonth(AUGUST)
        val sameDays = ProgressDomainModel.ComparedWith.SameDaysLastMonth

        assertEquals(
            ProgressPresentationModel.Fewer(
                3,
                ProgressPresentationModel.ComparedWith.WholeMonth(AUGUST)
            ),
            classUnderTest.toPresentation(
                stats(progress = ProgressDomainModel(-3, wholeAugust))
            ).progress
        )
        assertEquals(
            ProgressPresentationModel.More(
                2,
                ProgressPresentationModel.ComparedWith.SameDaysLastMonth
            ),
            classUnderTest.toPresentation(
                stats(progress = ProgressDomainModel(2, sameDays))
            ).progress
        )
        assertEquals(
            ProgressPresentationModel.Same(
                ProgressPresentationModel.ComparedWith.SameDaysLastMonth
            ),
            classUnderTest.toPresentation(
                stats(progress = ProgressDomainModel(0, sameDays))
            ).progress
        )
    }
}
