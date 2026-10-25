package com.migrely.app.history.ui.mapper

import com.migrely.app.history.presentation.model.DaySkyPresentationModel
import com.migrely.app.history.presentation.model.HistoryDayPresentationModel
import com.migrely.app.history.presentation.model.HistoryMonthPresentationModel
import com.migrely.app.history.presentation.model.MonthStatsPresentationModel
import com.migrely.app.history.ui.model.DaySkyUiModel
import com.migrely.app.history.ui.model.HistoryDayUiModel
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Test

private val DAY: LocalDate = LocalDate.of(2026, 10, 20)

class HistoryMonthUiMapperDayTest {
    private val classUnderTest = HistoryMonthUiMapper()

    private fun mapped(day: HistoryDayPresentationModel): HistoryDayUiModel = classUnderTest.toUi(
        HistoryMonthPresentationModel(
            month = YearMonth.of(2026, 10),
            today = LocalDate.of(2026, 10, 23),
            hasPreviousMonth = true,
            hasNextMonth = false,
            days = listOf(day),
            attacks = emptyList(),
            stats = MonthStatsPresentationModel(0, 0, 0, null, 1, chronic = false, progress = null)
        )
    ).days.single()

    @Test
    fun `Given a day with attack medication only when mapped then that sky and the flag`() {
        assertEquals(
            HistoryDayUiModel(
                epochDay = DAY.toEpochDay(),
                sky = DaySkyUiModel.MedicationOnly,
                peak = null,
                attacks = emptyList(),
                attackMedication = true
            ),
            mapped(
                HistoryDayPresentationModel(
                    date = DAY,
                    sky = DaySkyPresentationModel.MedicationOnly,
                    peak = null,
                    attacks = emptyList(),
                    attackMedication = true
                )
            )
        )
    }

    @Test
    fun `Given a pain-free day with attack medication when mapped then clear with the flag`() {
        val actual = mapped(
            HistoryDayPresentationModel(
                date = DAY,
                sky = DaySkyPresentationModel.Clear,
                peak = null,
                attacks = emptyList(),
                painFree = true,
                attackMedication = true
            )
        )

        assertEquals(DaySkyUiModel.Clear to true, actual.sky to actual.attackMedication)
    }

    @Test
    fun `Given a day with nothing logged when mapped then no attack medication`() {
        val actual = mapped(
            HistoryDayPresentationModel(
                date = DAY,
                sky = DaySkyPresentationModel.NothingLogged,
                peak = null,
                attacks = emptyList()
            )
        )

        assertEquals(DaySkyUiModel.NothingLogged to false, actual.sky to actual.attackMedication)
    }
}
