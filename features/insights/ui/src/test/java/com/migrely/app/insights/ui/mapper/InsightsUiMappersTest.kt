package com.migrely.app.insights.ui.mapper

import com.migrely.app.insights.presentation.model.InsightsCyclePresentationModel
import com.migrely.app.insights.presentation.model.InsightsMedicationPresentationModel
import com.migrely.app.insights.presentation.model.InsightsMonthPresentationModel
import com.migrely.app.insights.presentation.model.InsightsPatternsPresentationModel
import com.migrely.app.insights.presentation.model.InsightsPercentPresentationModel
import com.migrely.app.insights.presentation.model.InsightsPresentationModel
import com.migrely.app.insights.presentation.model.InsightsTimeOfDayCountPresentationModel
import com.migrely.app.insights.presentation.model.InsightsTimeOfDayPresentationModel
import com.migrely.app.insights.presentation.model.InsightsTriggerCountPresentationModel
import com.migrely.app.insights.presentation.model.InsightsTriggerPresentationModel
import com.migrely.app.insights.presentation.model.InsightsViewState
import com.migrely.app.insights.presentation.model.InsightsWeekdayPresentationModel
import com.migrely.app.insights.ui.model.InsightsCycleUiModel
import com.migrely.app.insights.ui.model.InsightsMedicationUiModel
import com.migrely.app.insights.ui.model.InsightsMonthUiModel
import com.migrely.app.insights.ui.model.InsightsPatternsUiModel
import com.migrely.app.insights.ui.model.InsightsPercentUiModel
import com.migrely.app.insights.ui.model.InsightsTimeOfDayBarUiModel
import com.migrely.app.insights.ui.model.InsightsTimeOfDayUiModel
import com.migrely.app.insights.ui.model.InsightsTriggerCountUiModel
import com.migrely.app.insights.ui.model.InsightsTriggerUiModel
import com.migrely.app.insights.ui.model.InsightsUiModel
import com.migrely.app.insights.ui.model.InsightsViewStateUiModel
import com.migrely.app.insights.ui.model.InsightsWeekdayBarUiModel
import java.time.DayOfWeek
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Test

private val WEEKDAYS = listOf(4, 2, 0, 1, 1, 1, 1)
private val TIMES = InsightsTimeOfDayPresentationModel.entries.zip(InsightsTimeOfDayUiModel.entries)

class InsightsUiMappersTest {
    private val classUnderTest = InsightsViewStateUiMapper(InsightsTriggerUiMapper())

    @Test
    fun `When mapping the sections then each bar is its count next to the highest of its chart`() {
        val viewState = InsightsViewState.Ready(
            InsightsPresentationModel(
                months = 6,
                patterns = InsightsPatternsPresentationModel.Enough(
                    migraines = 10,
                    triggers = listOf(
                        InsightsTriggerCountPresentationModel(
                            InsightsTriggerPresentationModel.Custom("Long drive"),
                            6,
                            InsightsPercentPresentationModel.Whole(60)
                        )
                    ),
                    weekdays = DayOfWeek.entries.map {
                        InsightsWeekdayPresentationModel(it, WEEKDAYS[it.ordinal])
                    },
                    busiestWeekday = DayOfWeek.MONDAY,
                    timesOfDay = TIMES.mapIndexed { index, (time, _) ->
                        InsightsTimeOfDayCountPresentationModel(time, index + 1)
                    },
                    busiestTimeOfDay = InsightsTimeOfDayPresentationModel.Evening,
                    cycle = InsightsCyclePresentationModel(
                        4,
                        InsightsPercentPresentationModel.Whole(40),
                        24,
                        183,
                        InsightsPercentPresentationModel.BelowOne
                    ),
                    medications = listOf(
                        InsightsMedicationPresentationModel.Rated(
                            "Ibuprofen",
                            7,
                            9,
                            InsightsPercentPresentationModel.AboveNinetyNine
                        ),
                        InsightsMedicationPresentationModel.NeedsAnswers("Sumatriptan", 2)
                    )
                ),
                trends = emptyList()
            )
        )

        assertEquals(
            InsightsViewStateUiModel.Ready(
                InsightsUiModel(
                    months = 6,
                    patterns = InsightsPatternsUiModel.Enough(
                        migraines = 10,
                        triggers = listOf(
                            InsightsTriggerCountUiModel(
                                InsightsTriggerUiModel.Custom("Long drive"),
                                6,
                                InsightsPercentUiModel.Whole(60)
                            )
                        ),
                        weekdays = DayOfWeek.entries.map {
                            InsightsWeekdayBarUiModel(
                                it,
                                WEEKDAYS[it.ordinal],
                                WEEKDAYS[it.ordinal] / 4f
                            )
                        },
                        busiestWeekday = DayOfWeek.MONDAY,
                        timesOfDay = TIMES.mapIndexed { index, (_, time) ->
                            InsightsTimeOfDayBarUiModel(time, index + 1, (index + 1) / 4f)
                        },
                        busiestTimeOfDay = InsightsTimeOfDayUiModel.Evening,
                        cycle = InsightsCycleUiModel(
                            4,
                            InsightsPercentUiModel.Whole(40),
                            24,
                            183,
                            InsightsPercentUiModel.BelowOne
                        ),
                        medications = listOf(
                            InsightsMedicationUiModel.Rated(
                                "Ibuprofen",
                                7,
                                9,
                                InsightsPercentUiModel.AboveNinetyNine
                            ),
                            InsightsMedicationUiModel.NeedsAnswers("Sumatriptan", 2)
                        )
                    ),
                    trends = emptyList()
                )
            ),
            classUnderTest.toUi(viewState)
        )
    }

    @Test
    fun `When mapping the trends then each month's bar is next to the busiest month`() {
        val viewState = InsightsViewState.Ready(
            InsightsPresentationModel(
                months = 3,
                patterns = InsightsPatternsPresentationModel.NotEnough(2, 0),
                trends = listOf(
                    InsightsMonthPresentationModel(YearMonth.of(2026, 9), 8, 3, 4, 1),
                    InsightsMonthPresentationModel(YearMonth.of(2026, 10), 2, 1, 0, 0)
                )
            )
        )

        assertEquals(
            InsightsViewStateUiModel.Ready(
                InsightsUiModel(
                    months = 3,
                    patterns = InsightsPatternsUiModel.NotEnough(2, 0),
                    trends = listOf(
                        InsightsMonthUiModel(2026, 9, 8, 3, 4, 1, 1f),
                        InsightsMonthUiModel(2026, 10, 2, 1, 0, 0, 0.25f)
                    )
                )
            ),
            classUnderTest.toUi(viewState)
        )
    }

    @Test
    fun `Given nothing to draw then empty bars, and no busiest`() {
        val viewState = InsightsViewState.Ready(
            InsightsPresentationModel(
                months = 12,
                patterns = InsightsPatternsPresentationModel.Enough(
                    5,
                    emptyList(),
                    listOf(InsightsWeekdayPresentationModel(DayOfWeek.MONDAY, 0)),
                    null,
                    emptyList(),
                    null,
                    null,
                    emptyList()
                ),
                trends = listOf(InsightsMonthPresentationModel(YearMonth.of(2026, 10), 0, 0, 0, 0))
            )
        )

        assertEquals(
            InsightsViewStateUiModel.Ready(
                InsightsUiModel(
                    months = 12,
                    patterns = InsightsPatternsUiModel.Enough(
                        5,
                        emptyList(),
                        listOf(InsightsWeekdayBarUiModel(DayOfWeek.MONDAY, 0, 0f)),
                        null,
                        emptyList(),
                        null,
                        null,
                        emptyList()
                    ),
                    trends = listOf(InsightsMonthUiModel(2026, 10, 0, 0, 0, 0, 0f))
                )
            ),
            classUnderTest.toUi(viewState)
        )
    }

    @Test
    fun `Each time of day maps to its own, and loading and unavailable stay as they are`() {
        TIMES.forEach { (presentation, ui) ->
            val viewState = InsightsViewState.Ready(
                InsightsPresentationModel(
                    6,
                    InsightsPatternsPresentationModel.Enough(
                        5,
                        emptyList(),
                        emptyList(),
                        null,
                        emptyList(),
                        presentation,
                        null,
                        emptyList()
                    ),
                    emptyList()
                )
            )
            val ready = classUnderTest.toUi(viewState) as InsightsViewStateUiModel.Ready

            assertEquals(
                ui,
                (ready.insights.patterns as InsightsPatternsUiModel.Enough).busiestTimeOfDay
            )
        }
        assertEquals(
            InsightsViewStateUiModel.Loading,
            classUnderTest.toUi(InsightsViewState.Loading)
        )
        assertEquals(
            InsightsViewStateUiModel.Unavailable,
            classUnderTest.toUi(InsightsViewState.Unavailable)
        )
    }
}
