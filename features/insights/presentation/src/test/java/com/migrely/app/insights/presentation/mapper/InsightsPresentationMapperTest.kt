package com.migrely.app.insights.presentation.mapper

import com.migrely.app.insights.domain.model.InsightsCycleDomainModel
import com.migrely.app.insights.domain.model.InsightsDatesDomainModel
import com.migrely.app.insights.domain.model.InsightsDomainModel
import com.migrely.app.insights.domain.model.InsightsMedicationDomainModel
import com.migrely.app.insights.domain.model.InsightsMonthDomainModel
import com.migrely.app.insights.domain.model.InsightsPatternsDomainModel
import com.migrely.app.insights.domain.model.InsightsResultDomainModel
import com.migrely.app.insights.domain.model.InsightsTimeOfDayCountDomainModel
import com.migrely.app.insights.domain.model.InsightsTimeOfDayDomainModel
import com.migrely.app.insights.domain.model.InsightsTimesOfDayDomainModel
import com.migrely.app.insights.domain.model.InsightsTriggerDomainModel
import com.migrely.app.insights.domain.model.InsightsWeekdayDomainModel
import com.migrely.app.insights.domain.model.InsightsWeekdaysDomainModel
import com.migrely.app.insights.presentation.model.InsightsCyclePresentationModel
import com.migrely.app.insights.presentation.model.InsightsMedicationPresentationModel
import com.migrely.app.insights.presentation.model.InsightsMonthPresentationModel
import com.migrely.app.insights.presentation.model.InsightsPatternsPresentationModel
import com.migrely.app.insights.presentation.model.InsightsPresentationModel
import com.migrely.app.insights.presentation.model.InsightsTimeOfDayCountPresentationModel
import com.migrely.app.insights.presentation.model.InsightsTimeOfDayPresentationModel
import com.migrely.app.insights.presentation.model.InsightsTriggerPresentationModel
import com.migrely.app.insights.presentation.model.InsightsViewState
import com.migrely.app.insights.presentation.model.InsightsWeekdayPresentationModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Test

private val DATES = InsightsDatesDomainModel(LocalDate.of(2026, 4, 26), LocalDate.of(2026, 10, 25))
private val OCTOBER = InsightsMonthDomainModel(YearMonth.of(2026, 10), 5, 3, 2, 1)
private val OCTOBER_PRESENTATION =
    InsightsMonthPresentationModel(YearMonth.of(2026, 10), 5, 3, 2, 1)
private val TIMES = InsightsTimeOfDayDomainModel.entries.zip(
    InsightsTimeOfDayPresentationModel.entries
)

private fun insights(patterns: InsightsPatternsDomainModel) =
    InsightsResultDomainModel.Ready(InsightsDomainModel(6, DATES, patterns, listOf(OCTOBER)))

class InsightsPresentationMapperTest {
    private val classUnderTest = InsightsPresentationMapper()

    @Test
    fun `Given enough then each share as a rounded percent of its total`() {
        val patterns = InsightsPatternsDomainModel.Enough(
            migraines = 10,
            triggers = listOf(
                InsightsTriggerDomainModel("Stress", 6),
                InsightsTriggerDomainModel("Long drive", 1)
            ),
            weekdays = InsightsWeekdaysDomainModel(
                DayOfWeek.entries.map {
                    InsightsWeekdayDomainModel(
                        it,
                        if (it.value ==
                            1
                        ) {
                            4
                        } else {
                            1
                        }
                    )
                },
                DayOfWeek.MONDAY
            ),
            timesOfDay = InsightsTimesOfDayDomainModel(
                TIMES.mapIndexed { index, (time, _) ->
                    InsightsTimeOfDayCountDomainModel(time, index + 1)
                },
                InsightsTimeOfDayDomainModel.Evening
            ),
            cycle = InsightsCycleDomainModel(
                cycles = 6,
                migraines = 4,
                days = 24,
                periodDays = 183
            ),
            medications = listOf(
                InsightsMedicationDomainModel.Rated("Ibuprofen", helped = 7, answered = 9),
                InsightsMedicationDomainModel.NeedsAnswers("Sumatriptan", answered = 2)
            )
        )

        assertEquals(
            InsightsViewState.Ready(
                InsightsPresentationModel(
                    months = 6,
                    patterns = InsightsPatternsPresentationModel.Enough(
                        migraines = 10,
                        triggers = listOf(
                            InsightsTriggerPresentationModel("Stress", 6, 60),
                            InsightsTriggerPresentationModel("Long drive", 1, 10)
                        ),
                        weekdays = DayOfWeek.entries.map {
                            InsightsWeekdayPresentationModel(it, if (it.value == 1) 4 else 1)
                        },
                        busiestWeekday = DayOfWeek.MONDAY,
                        timesOfDay = TIMES.mapIndexed { index, (_, time) ->
                            InsightsTimeOfDayCountPresentationModel(time, index + 1)
                        },
                        busiestTimeOfDay = InsightsTimeOfDayPresentationModel.Evening,
                        cycle = InsightsCyclePresentationModel(
                            migraines = 4,
                            percent = 40,
                            daysPercent = 13
                        ),
                        medications = listOf(
                            InsightsMedicationPresentationModel.Rated("Ibuprofen", 7, 9, 78),
                            InsightsMedicationPresentationModel.NeedsAnswers("Sumatriptan", 2)
                        )
                    ),
                    trends = listOf(OCTOBER_PRESENTATION)
                )
            ),
            classUnderTest.toPresentation(insights(patterns))
        )
    }

    @Test
    fun `Each time of day maps to its own`() {
        TIMES.forEach { (domain, presentation) ->
            val patterns = InsightsPatternsDomainModel.Enough(
                5,
                emptyList(),
                InsightsWeekdaysDomainModel(emptyList(), null),
                InsightsTimesOfDayDomainModel(emptyList(), domain),
                null,
                emptyList()
            )
            val ready = classUnderTest.toPresentation(insights(patterns)) as InsightsViewState.Ready
            val enough = ready.insights.patterns as InsightsPatternsPresentationModel.Enough

            assertEquals(presentation, enough.busiestTimeOfDay)
        }
    }

    @Test
    fun `Given no single busiest, no cycle and no medications then none`() {
        val patterns = InsightsPatternsDomainModel.Enough(
            5,
            emptyList(),
            InsightsWeekdaysDomainModel(emptyList(), null),
            InsightsTimesOfDayDomainModel(emptyList(), null),
            null,
            listOf(InsightsMedicationDomainModel.Rated("Aspirin", helped = 0, answered = 3))
        )

        val ready = classUnderTest.toPresentation(insights(patterns)) as InsightsViewState.Ready

        assertEquals(
            InsightsPatternsPresentationModel.Enough(
                5,
                emptyList(),
                emptyList(),
                null,
                emptyList(),
                null,
                null,
                listOf(InsightsMedicationPresentationModel.Rated("Aspirin", 0, 3, 0))
            ),
            ready.insights.patterns
        )
    }

    @Test
    fun `Given not enough then what is missing, with the trends`() {
        assertEquals(
            InsightsViewState.Ready(
                InsightsPresentationModel(
                    6,
                    InsightsPatternsPresentationModel.NotEnough(2, 12),
                    listOf(OCTOBER_PRESENTATION)
                )
            ),
            classUnderTest.toPresentation(
                insights(
                    InsightsPatternsDomainModel.NotEnough(missingMigraines = 2, missingDays = 12)
                )
            )
        )
    }

    @Test
    fun `Given failed then unavailable`() {
        assertEquals(
            InsightsViewState.Unavailable,
            classUnderTest.toPresentation(InsightsResultDomainModel.Failed)
        )
    }
}
