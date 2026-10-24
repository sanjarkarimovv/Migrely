package com.migrely.app.history.presentation.mapper

import com.migrely.app.history.domain.model.HistoryAttackDomainModel
import com.migrely.app.history.domain.model.HistoryDayDomainModel
import com.migrely.app.history.domain.model.HistoryMonthDomainModel
import com.migrely.app.history.domain.model.MonthStatsDomainModel
import com.migrely.app.history.presentation.model.DaySkyPresentationModel
import com.migrely.app.history.presentation.model.HistoryAttackPresentationModel
import com.migrely.app.history.presentation.model.PeakPresentationModel
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

private val ZONE: ZoneId = ZoneId.of("Asia/Tashkent")
private val START: Instant = Instant.parse("2026-10-14T05:00:00Z")
private val END: Instant = Instant.parse("2026-10-14T06:00:00Z")
private val TODAY: LocalDate = LocalDate.parse("2026-10-23")
private val OCTOBER: YearMonth = YearMonth.of(2026, 10)

private fun attack(peak: Int?, end: Instant? = END) =
    HistoryAttackDomainModel("a", START, ZONE, end, peak)

private val classUnderTest = HistoryMonthPresentationMapper(
    HistoryAttackPresentationMapper(),
    IntensityLevelPresentationMapper()
)

private fun month(
    days: List<HistoryDayDomainModel> = emptyList(),
    attacks: List<HistoryAttackDomainModel> = emptyList(),
    shown: YearMonth = OCTOBER,
    earliest: YearMonth = YearMonth.of(2026, 8)
) = HistoryMonthDomainModel(
    shown,
    TODAY,
    earliest,
    true,
    days,
    attacks,
    MonthStatsDomainModel(0, 0, 0, null, 0, chronic = false, progress = null)
)

@RunWith(Parameterized::class)
class HistoryMonthPresentationMapperDayTest(
    private val date: String,
    private val attacks: List<HistoryAttackDomainModel>,
    private val expectedSky: DaySkyPresentationModel,
    private val expectedPeak: PeakPresentationModel?
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0} {1} -> {2}, {3}")
        fun data(): Collection<Array<Any?>> = listOf(
            arrayOf("2026-10-06", emptyList<Any>(), DaySkyPresentationModel.NothingLogged, null),
            arrayOf("2026-10-23", emptyList<Any>(), DaySkyPresentationModel.NothingLogged, null),
            arrayOf("2026-10-25", emptyList<Any>(), DaySkyPresentationModel.Future, null),
            arrayOf(
                "2026-10-14",
                listOf(attack(3)),
                DaySkyPresentationModel.Hazy,
                PeakPresentationModel.Mild
            ),
            arrayOf(
                "2026-10-14",
                listOf(attack(6)),
                DaySkyPresentationModel.Cloudy,
                PeakPresentationModel.Moderate
            ),
            arrayOf(
                "2026-10-14",
                listOf(attack(3), attack(8)),
                DaySkyPresentationModel.Storm,
                PeakPresentationModel.Severe
            ),
            arrayOf(
                "2026-10-14",
                listOf(attack(null)),
                DaySkyPresentationModel.Cloudy,
                PeakPresentationModel.NotRecorded
            ),
            arrayOf(
                "2026-10-14",
                listOf(attack(2), attack(null)),
                DaySkyPresentationModel.Cloudy,
                PeakPresentationModel.Mild
            ),
            arrayOf(
                "2026-10-14",
                listOf(attack(9), attack(null)),
                DaySkyPresentationModel.Storm,
                PeakPresentationModel.Severe
            ),
            arrayOf(
                "2026-10-23",
                listOf(attack(9), attack(4, end = null)),
                DaySkyPresentationModel.Rain,
                PeakPresentationModel.Severe
            ),
            // An attack logged in a zone ahead of the device can touch a day the device has not reached (design risks).
            arrayOf(
                "2026-10-24",
                listOf(attack(5)),
                DaySkyPresentationModel.Cloudy,
                PeakPresentationModel.Moderate
            )
        )
    }

    @Test
    fun `When mapping a day then sky and peak follow the data`() {
        val day = HistoryDayDomainModel(LocalDate.parse(date), attacks)

        val actual = classUnderTest.toPresentation(month(days = listOf(day))).days.single()

        assertEquals(expectedSky, actual.sky)
        assertEquals(expectedPeak, actual.peak)
        assertEquals(attacks.size, actual.attacks.size)
    }
}

class HistoryMonthPresentationMapperTest {
    @Test
    fun `Given a pain-free period day when mapping then a clear sky with both marks`() {
        val day = HistoryDayDomainModel(
            LocalDate.parse("2026-10-06"),
            emptyList(),
            period = true,
            painFree = true
        )

        val actual = classUnderTest.toPresentation(month(days = listOf(day))).days.single()

        assertEquals(DaySkyPresentationModel.Clear, actual.sky)
        assertEquals(
            Triple(true, true, true),
            Triple(actual.period, actual.painFree, actual.painFreeAllowed)
        )
    }

    @Test
    fun `Given a period day with a storm when mapping then the storm, and pain-free not allowed`() {
        val day = HistoryDayDomainModel(
            LocalDate.parse("2026-10-14"),
            listOf(attack(9)),
            period = true
        )

        val actual = classUnderTest.toPresentation(month(days = listOf(day))).days.single()

        assertEquals(DaySkyPresentationModel.Storm, actual.sky)
        assertEquals(
            Triple(true, false, false),
            Triple(actual.period, actual.painFree, actual.painFreeAllowed)
        )
    }

    @Test
    fun `Given a month between the first and the current when mapping then both arrows`() {
        val actual = classUnderTest.toPresentation(month(shown = YearMonth.of(2026, 9)))

        assertEquals(true, actual.hasPreviousMonth)
        assertEquals(true, actual.hasNextMonth)
    }

    @Test
    fun `Given the first month when mapping then no previous month`() {
        val actual = classUnderTest.toPresentation(month(shown = YearMonth.of(2026, 8)))

        assertEquals(false, actual.hasPreviousMonth)
    }

    @Test
    fun `Given the current month when mapping then no next month`() {
        val actual = classUnderTest.toPresentation(month(shown = OCTOBER))

        assertEquals(false, actual.hasNextMonth)
        assertEquals(TODAY, actual.today)
    }

    @Test
    fun `When mapping then the month's attacks keep their order and fields`() {
        val first = HistoryAttackDomainModel("new", END, ZONE, null, null)
        val second = HistoryAttackDomainModel("old", START, ZONE, END, 7)

        val actual = classUnderTest.toPresentation(month(attacks = listOf(first, second)))

        assertEquals(
            listOf(
                HistoryAttackPresentationModel("new", END, ZONE, null, null),
                HistoryAttackPresentationModel("old", START, ZONE, END, 7)
            ),
            actual.attacks
        )
    }
}
