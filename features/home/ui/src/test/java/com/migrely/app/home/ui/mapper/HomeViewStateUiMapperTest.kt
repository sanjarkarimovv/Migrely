package com.migrely.app.home.ui.mapper

import com.migrely.app.home.presentation.model.AttackInProgressPresentationModel
import com.migrely.app.home.presentation.model.AttackSummaryPresentationModel
import com.migrely.app.home.presentation.model.HomeViewState
import com.migrely.app.home.presentation.model.IntensityLevelPresentationModel
import com.migrely.app.home.presentation.model.PeakPresentationModel
import com.migrely.app.home.presentation.model.SkyPresentationModel
import com.migrely.app.home.presentation.model.TodayPresentationModel
import com.migrely.app.home.ui.model.AttackInProgressUiModel
import com.migrely.app.home.ui.model.AttackSummaryUiModel
import com.migrely.app.home.ui.model.HomeViewStateUiModel
import com.migrely.app.home.ui.model.IntensityLevelUiModel
import com.migrely.app.home.ui.model.PeakUiModel
import com.migrely.app.home.ui.model.SkyUiModel
import com.migrely.app.home.ui.model.TodayUiModel
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class HomeViewStateUiMapperTest(
    private val given: HomeViewState,
    private val expected: HomeViewStateUiModel
) {
    companion object {
        private val START = Instant.parse("2026-10-23T09:05:00Z")

        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun data(): Collection<Array<*>> = listOf(
            arrayOf<Any>(HomeViewState.Loading, HomeViewStateUiModel.Loading),
            arrayOf<Any>(
                HomeViewState.Today(TodayPresentationModel(SkyPresentationModel.Calm, 0, null)),
                HomeViewStateUiModel.Today(TodayUiModel(SkyUiModel.Calm, 0, null))
            ),
            arrayOf<Any>(
                HomeViewState.Today(
                    TodayPresentationModel(
                        SkyPresentationModel.Clear,
                        0,
                        null,
                        periodToday = true
                    )
                ),
                HomeViewStateUiModel.Today(
                    TodayUiModel(SkyUiModel.Clear, 0, null, periodToday = true)
                )
            ),
            arrayOf<Any>(
                HomeViewState.Today(
                    TodayPresentationModel(SkyPresentationModel.Hazy, 1, PeakPresentationModel.Mild)
                ),
                HomeViewStateUiModel.Today(TodayUiModel(SkyUiModel.Hazy, 1, PeakUiModel.Mild))
            ),
            arrayOf<Any>(
                HomeViewState.Today(
                    TodayPresentationModel(
                        SkyPresentationModel.Cloudy,
                        1,
                        PeakPresentationModel.NotRecorded
                    )
                ),
                HomeViewStateUiModel.Today(
                    TodayUiModel(SkyUiModel.Cloudy, 1, PeakUiModel.NotRecorded)
                )
            ),
            arrayOf<Any>(
                HomeViewState.Today(
                    TodayPresentationModel(
                        SkyPresentationModel.Storm,
                        2,
                        PeakPresentationModel.Severe
                    )
                ),
                HomeViewStateUiModel.Today(TodayUiModel(SkyUiModel.Storm, 2, PeakUiModel.Severe))
            ),
            arrayOf<Any>(
                HomeViewState.Today(
                    TodayPresentationModel(
                        SkyPresentationModel.Cloudy,
                        1,
                        PeakPresentationModel.Moderate,
                        listOf(
                            AttackSummaryPresentationModel(
                                "a",
                                START,
                                ZoneId.of("Asia/Tashkent"),
                                START.plusSeconds(3_600),
                                5
                            )
                        )
                    )
                ),
                HomeViewStateUiModel.Today(
                    TodayUiModel(
                        SkyUiModel.Cloudy,
                        1,
                        PeakUiModel.Moderate,
                        listOf(
                            AttackSummaryUiModel(
                                "a",
                                START.toEpochMilli(),
                                "Asia/Tashkent",
                                START.plusSeconds(3_600).toEpochMilli(),
                                5
                            )
                        )
                    )
                )
            ),
            arrayOf<Any>(
                HomeViewState.AttackInProgress(
                    AttackInProgressPresentationModel(
                        "a",
                        START,
                        ZoneId.of("Asia/Tashkent"),
                        IntensityLevelPresentationModel.Moderate,
                        longAttackCheckDue = true
                    )
                ),
                HomeViewStateUiModel.AttackInProgress(
                    AttackInProgressUiModel(
                        "a",
                        START.toEpochMilli(),
                        "Asia/Tashkent",
                        IntensityLevelUiModel.Moderate,
                        true
                    )
                )
            ),
            arrayOf<Any>(
                HomeViewState.AttackInProgress(
                    AttackInProgressPresentationModel(
                        "a",
                        START,
                        ZoneId.of("UTC"),
                        null,
                        longAttackCheckDue = false
                    )
                ),
                HomeViewStateUiModel.AttackInProgress(
                    AttackInProgressUiModel("a", START.toEpochMilli(), "UTC", null, false)
                )
            )
        )
    }

    @Test
    fun `When toUi then returns the expected UI model`() {
        assertEquals(expected, HomeViewStateUiMapper().toUi(given))
    }
}
