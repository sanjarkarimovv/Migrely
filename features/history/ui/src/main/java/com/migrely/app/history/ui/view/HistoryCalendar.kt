package com.migrely.app.history.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.sky.CalendarDay
import com.migrely.app.designsystem.sky.DaySkyAppearance
import com.migrely.app.designsystem.sky.LegendEntry
import com.migrely.app.designsystem.sky.MonthSkyCalendar
import com.migrely.app.designsystem.sky.MonthSkyLegend
import com.migrely.app.history.ui.R
import com.migrely.app.history.ui.mapper.CalendarLayoutUiMapper
import com.migrely.app.history.ui.mapper.DaySkyAppearanceUiMapper
import com.migrely.app.history.ui.mapper.HistoryCalendarMappers
import com.migrely.app.history.ui.mapper.HistoryTimeUiFormatter
import com.migrely.app.history.ui.model.DaySkyUiModel
import com.migrely.app.history.ui.model.HistoryDayUiModel
import com.migrely.app.history.ui.model.HistoryMonthUiModel
import com.migrely.app.history.ui.model.PeakUiModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.Locale

/** A month of skies with its legend (spec: month-sky). [onDay] receives the epoch day of a tapped day. */
@Composable
internal fun HistoryCalendar(
    month: HistoryMonthUiModel,
    selectedEpochDay: Long?,
    mappers: HistoryCalendarMappers,
    onDay: (epochDay: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val layout = mappers.calendarLayoutUiMapper.toUi(month.year, month.month)
    val days = month.days.map { day ->
        val isToday = day.epochDay == month.todayEpochDay
        CalendarDay(
            dayOfMonth = LocalDate.ofEpochDay(day.epochDay).dayOfMonth,
            appearance = mappers.daySkyAppearanceUiMapper.toAppearance(day.sky),
            isToday = isToday,
            isSelected = day.epochDay == selectedEpochDay,
            isSelectable = day.epochDay <= month.todayEpochDay,
            description = dayDescription(day, isToday, mappers.historyTimeUiFormatter),
            hasPeriodMark = day.period
        )
    }
    Column(modifier = modifier) {
        MonthSkyCalendar(
            weekdayInitials = layout.weekdayInitials,
            leadingEmptyDays = layout.leadingEmptyDays,
            days = days,
            chosenStateDescription = stringResource(R.string.day_chosen),
            onDayClick = { dayOfMonth -> onDay(month.days[dayOfMonth - 1].epochDay) },
            modifier = Modifier.fillMaxWidth()
        )
        MonthSkyLegend(
            entries = listOf(
                LegendEntry(DaySkyAppearance.Clear, stringResource(R.string.legend_pain_free)),
                LegendEntry(DaySkyAppearance.Hazy, stringResource(R.string.legend_mild)),
                LegendEntry(DaySkyAppearance.Cloudy, stringResource(R.string.legend_moderate)),
                LegendEntry(DaySkyAppearance.Storm, stringResource(R.string.legend_severe)),
                LegendEntry(
                    DaySkyAppearance.NothingLogged,
                    stringResource(R.string.legend_not_logged)
                ),
                LegendEntry(
                    DaySkyAppearance.NothingLogged,
                    stringResource(R.string.legend_period),
                    periodMark = true
                )
            ),
            modifier = Modifier.padding(top = 14.dp, start = 2.dp)
        )
    }
}

/**
 * For example "Wednesday, 15 October: storm. 1 migraine. Peak: severe." (BRD EC-UX-15), followed by "Period." on a
 * period day (D49).
 */
@Composable
private fun dayDescription(
    day: HistoryDayUiModel,
    isToday: Boolean,
    timeUiFormatter: HistoryTimeUiFormatter
): String {
    val sky = skyDescription(day, isToday, timeUiFormatter)
    return when {
        !day.period -> sky
        // A future date has no sky to describe, so the mark takes its place: "Sunday, 25 October: period." (D50).
        day.sky == DaySkyUiModel.Future -> stringResource(R.string.day_future_with_period, sky)
        else -> stringResource(R.string.day_with_period, sky)
    }
}

@Composable
private fun skyDescription(
    day: HistoryDayUiModel,
    isToday: Boolean,
    timeUiFormatter: HistoryTimeUiFormatter
): String {
    val date = timeUiFormatter.formatDayTitle(day.epochDay)
    val named = if (isToday) stringResource(R.string.day_today, date) else date
    return when (day.sky) {
        DaySkyUiModel.Future -> named
        DaySkyUiModel.NothingLogged -> stringResource(R.string.day_nothing_logged, named)
        DaySkyUiModel.Clear -> stringResource(R.string.day_pain_free, named)
        DaySkyUiModel.Rain -> stringResource(R.string.day_in_progress, named)
        DaySkyUiModel.Hazy,
        DaySkyUiModel.Cloudy,
        DaySkyUiModel.Storm -> stringResource(
            R.string.day_attacks,
            named,
            skyName(day.sky),
            pluralStringResource(R.plurals.day_attack_count, day.attacks.size, day.attacks.size),
            peakWord(day.peak)
        )
    }
}

@Composable
private fun skyName(sky: DaySkyUiModel): String = when (sky) {
    DaySkyUiModel.Hazy -> stringResource(R.string.sky_hazy)
    DaySkyUiModel.Cloudy -> stringResource(R.string.sky_cloudy)
    DaySkyUiModel.Storm -> stringResource(R.string.sky_storm)
    DaySkyUiModel.NothingLogged,
    DaySkyUiModel.Future,
    DaySkyUiModel.Clear,
    DaySkyUiModel.Rain -> ""
}

@Composable
private fun peakWord(peak: PeakUiModel?): String = when (peak) {
    PeakUiModel.Mild -> stringResource(R.string.peak_mild)
    PeakUiModel.Moderate -> stringResource(R.string.peak_moderate)
    PeakUiModel.Severe -> stringResource(R.string.peak_severe)
    PeakUiModel.NotRecorded,
    null -> stringResource(R.string.peak_not_recorded)
}

private val PREVIEW_ZONE: ZoneId = ZoneId.of("Asia/Tashkent")

internal val previewFormatter = HistoryTimeUiFormatter(
    locale = { Locale.UK },
    is24HourFormat = { true },
    deviceZone = { PREVIEW_ZONE },
    bestDateTimePattern = { _, skeleton ->
        when (skeleton) {
            "MMMMy" -> "MMMM y"
            "MMMM" -> "LLLL"
            "EEEEdMMMM" -> "EEEE, d MMMM"
            "EEEdMMM" -> "EEE d MMM"
            "EEE" -> "EEE"
            else -> "HH:mm"
        }
    }
)

internal val previewMappers = HistoryCalendarMappers(
    calendarLayoutUiMapper = CalendarLayoutUiMapper { Locale.UK },
    daySkyAppearanceUiMapper = DaySkyAppearanceUiMapper(),
    historyTimeUiFormatter = previewFormatter
)

/** October 2026 seen on the 23rd, with a few attacks, for previews. */
internal fun previewMonth(): HistoryMonthUiModel {
    val october = YearMonth.of(2026, 10)
    val today = october.atDay(23)
    val skies = mapOf(
        3 to DaySkyUiModel.Storm,
        8 to DaySkyUiModel.Cloudy,
        12 to DaySkyUiModel.Hazy,
        15 to DaySkyUiModel.Storm,
        20 to DaySkyUiModel.Cloudy
    )
    return HistoryMonthUiModel(
        year = 2026,
        month = 10,
        todayEpochDay = today.toEpochDay(),
        hasPreviousMonth = true,
        hasNextMonth = false,
        days = (1..october.lengthOfMonth()).map { dayOfMonth ->
            val date = october.atDay(dayOfMonth)
            HistoryDayUiModel(
                epochDay = date.toEpochDay(),
                sky = skies[dayOfMonth] ?: if (date.isAfter(today)) {
                    DaySkyUiModel.Future
                } else {
                    DaySkyUiModel.NothingLogged
                },
                peak = if (dayOfMonth in skies) PeakUiModel.Severe else null,
                attacks = emptyList()
            )
        },
        attacks = emptyList()
    )
}
