package com.migrely.app.history.ui.mapper

import com.migrely.app.history.presentation.model.DaySkyPresentationModel
import com.migrely.app.history.presentation.model.HistoryAttackPresentationModel
import com.migrely.app.history.presentation.model.HistoryDayPresentationModel
import com.migrely.app.history.presentation.model.HistoryMonthPresentationModel
import com.migrely.app.history.presentation.model.HistoryViewState
import com.migrely.app.history.presentation.model.MonthSkyViewState
import com.migrely.app.history.presentation.model.MonthStatsPresentationModel
import com.migrely.app.history.presentation.model.PeakPresentationModel
import com.migrely.app.history.ui.model.DaySkyUiModel
import com.migrely.app.history.ui.model.HistoryAttackUiModel
import com.migrely.app.history.ui.model.HistoryDayUiModel
import com.migrely.app.history.ui.model.HistoryMonthUiModel
import com.migrely.app.history.ui.model.HistoryViewStateUiModel
import com.migrely.app.history.ui.model.MonthSkyViewStateUiModel
import com.migrely.app.history.ui.model.MonthStatsUiModel
import com.migrely.app.history.ui.model.PeakUiModel
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

private val START: Instant = Instant.parse("2026-10-14T04:10:00Z")
private val END: Instant = Instant.parse("2026-10-14T05:00:00Z")
private val DAY: LocalDate = LocalDate.parse("2026-10-14")
private val TODAY: LocalDate = LocalDate.parse("2026-10-23")

private val ATTACK = HistoryAttackPresentationModel("a", START, ZoneId.of("Asia/Tashkent"), END, 4)
private val ATTACK_UI = HistoryAttackUiModel(
    "a",
    START.toEpochMilli(),
    "Asia/Tashkent",
    END.toEpochMilli(),
    4
)

private fun month(sky: DaySkyPresentationModel, peak: PeakPresentationModel?) =
    HistoryMonthPresentationModel(
        month = YearMonth.of(2026, 10),
        today = TODAY,
        hasPreviousMonth = true,
        hasNextMonth = false,
        days = listOf(HistoryDayPresentationModel(DAY, sky, peak, listOf(ATTACK))),
        attacks = listOf(ATTACK),
        stats = MonthStatsPresentationModel(0, 0, 0, null, 0, chronic = false, progress = null)
    )

private fun monthUi(sky: DaySkyUiModel, peak: PeakUiModel?) = HistoryMonthUiModel(
    year = 2026,
    month = 10,
    todayEpochDay = TODAY.toEpochDay(),
    hasPreviousMonth = true,
    hasNextMonth = false,
    days = listOf(HistoryDayUiModel(DAY.toEpochDay(), sky, peak, listOf(ATTACK_UI))),
    attacks = listOf(ATTACK_UI),
    stats = MonthStatsUiModel(0, 0, 0, null, 0, chronic = false, progress = null)
)

class HistoryViewStateUiMapperTest {
    private val historyMonthUiMapper = HistoryMonthUiMapper()
    private val classUnderTest = HistoryViewStateUiMapper(historyMonthUiMapper)

    @Test
    fun `When mapping loading then loading`() {
        assertEquals(
            HistoryViewStateUiModel.Loading,
            classUnderTest.toUi(HistoryViewState.Loading)
        )
    }

    @Test
    fun `When mapping a ready month then every field`() {
        assertEquals(
            HistoryViewStateUiModel.Ready(
                monthUi(DaySkyUiModel.Storm, PeakUiModel.Severe),
                requestedMonth = "2026-09"
            ),
            classUnderTest.toUi(
                HistoryViewState.Ready(
                    month(DaySkyPresentationModel.Storm, PeakPresentationModel.Severe),
                    requestedMonth = YearMonth.of(2026, 9)
                )
            )
        )
    }

    @Test
    fun `When mapping no attacks then no attacks`() {
        assertEquals(
            HistoryViewStateUiModel.NoAttacks(
                monthUi(DaySkyUiModel.NothingLogged, null),
                requestedMonth = null
            ),
            classUnderTest.toUi(
                HistoryViewState.NoAttacks(
                    month(DaySkyPresentationModel.NothingLogged, null),
                    requestedMonth = null
                )
            )
        )
    }

    @Test
    fun `When mapping every sky and peak then each has its counterpart`() {
        val pairs = listOf(
            DaySkyPresentationModel.Future to DaySkyUiModel.Future,
            DaySkyPresentationModel.Clear to DaySkyUiModel.Clear,
            DaySkyPresentationModel.Hazy to DaySkyUiModel.Hazy,
            DaySkyPresentationModel.Cloudy to DaySkyUiModel.Cloudy,
            DaySkyPresentationModel.Rain to DaySkyUiModel.Rain
        )
        val peaks = listOf(
            PeakPresentationModel.Mild to PeakUiModel.Mild,
            PeakPresentationModel.Moderate to PeakUiModel.Moderate,
            PeakPresentationModel.NotRecorded to PeakUiModel.NotRecorded
        )
        pairs.zip(peaks + peaks.last() + peaks.last()).forEach { (sky, peak) ->
            assertEquals(
                monthUi(sky.second, peak.second),
                historyMonthUiMapper.toUi(month(sky.first, peak.first))
            )
        }
    }

    @Test
    fun `When mapping the month card then ready and unavailable`() {
        val cardMapper = MonthSkyViewStateUiMapper(historyMonthUiMapper)

        assertEquals(MonthSkyViewStateUiModel.Loading, cardMapper.toUi(MonthSkyViewState.Loading))
        assertEquals(
            MonthSkyViewStateUiModel.Unavailable,
            cardMapper.toUi(MonthSkyViewState.Unavailable)
        )
        assertEquals(
            MonthSkyViewStateUiModel.Ready(monthUi(DaySkyUiModel.Storm, PeakUiModel.Severe)),
            cardMapper.toUi(
                MonthSkyViewState.Ready(
                    month(DaySkyPresentationModel.Storm, PeakPresentationModel.Severe)
                )
            )
        )
    }

    @Test
    fun `When mapping a day's marks then each is kept`() {
        val day = HistoryDayPresentationModel(
            DAY,
            DaySkyPresentationModel.Storm,
            PeakPresentationModel.Severe,
            listOf(ATTACK),
            period = true,
            painFree = false,
            painFreeAllowed = false
        )

        val actual = historyMonthUiMapper.toUi(
            month(DaySkyPresentationModel.Storm, null).copy(days = listOf(day))
        )

        assertEquals(
            Triple(true, false, false),
            actual.days.single().let { Triple(it.period, it.painFree, it.painFreeAllowed) }
        )
    }
}
