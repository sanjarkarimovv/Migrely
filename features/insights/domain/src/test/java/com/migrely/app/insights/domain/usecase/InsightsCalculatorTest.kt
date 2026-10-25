package com.migrely.app.insights.domain.usecase

import com.migrely.app.insights.domain.model.InsightsAttackDomainModel
import com.migrely.app.insights.domain.model.InsightsBuiltInTriggerDomainModel
import com.migrely.app.insights.domain.model.InsightsCycleDomainModel
import com.migrely.app.insights.domain.model.InsightsDatesDomainModel
import com.migrely.app.insights.domain.model.InsightsEffectDomainModel
import com.migrely.app.insights.domain.model.InsightsIntakeDomainModel
import com.migrely.app.insights.domain.model.InsightsMedicationDomainModel
import com.migrely.app.insights.domain.model.InsightsMedicationKindDomainModel
import com.migrely.app.insights.domain.model.InsightsMonthDomainModel
import com.migrely.app.insights.domain.model.InsightsPatternsDomainModel
import com.migrely.app.insights.domain.model.InsightsRecordsDomainModel
import com.migrely.app.insights.domain.model.InsightsTimeOfDayCountDomainModel
import com.migrely.app.insights.domain.model.InsightsTimeOfDayDomainModel
import com.migrely.app.insights.domain.model.InsightsTimesOfDayDomainModel
import com.migrely.app.insights.domain.model.InsightsTriggerCountDomainModel
import com.migrely.app.insights.domain.model.InsightsTriggerDomainModel
import com.migrely.app.insights.domain.model.InsightsWeekdayDomainModel
import com.migrely.app.insights.domain.model.InsightsWeekdaysDomainModel
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private val TASHKENT: ZoneId = ZoneId.of("Asia/Tashkent")
private val TOKYO: ZoneId = ZoneId.of("Asia/Tokyo")
private val TODAY: LocalDate = LocalDate.of(2026, 10, 25)
private val NOW: Instant = TODAY.atTime(12, 0).atZone(TASHKENT).toInstant()
private val THREE_MONTHS = lastMonthsOn(3, TODAY)
private val ACUTE = InsightsMedicationKindDomainModel.Acute
private val A_LOT = InsightsEffectDomainModel.ALot
private val COMPLETELY = InsightsEffectDomainModel.Completely
private val NOT_AT_ALL = InsightsEffectDomainModel.NotAtAll

private fun at(text: String, zone: ZoneId = TASHKENT): Instant =
    LocalDateTime.parse(text).atZone(zone).toInstant()

private val STRESS = builtIn(InsightsBuiltInTriggerDomainModel.Stress)
private val PERIOD = builtIn(InsightsBuiltInTriggerDomainModel.Period)

private fun builtIn(trigger: InsightsBuiltInTriggerDomainModel) =
    InsightsTriggerDomainModel.BuiltIn(trigger)

private fun custom(name: String) = InsightsTriggerDomainModel.Custom(name)

private fun count(trigger: InsightsTriggerDomainModel, migraines: Int) =
    InsightsTriggerCountDomainModel(trigger, migraines)

private fun date(text: String): LocalDate = LocalDate.parse(text)

private fun attack(
    start: String,
    hours: Long? = 2,
    impact: Int? = null,
    triggers: List<InsightsTriggerDomainModel> = emptyList(),
    zone: ZoneId = TASHKENT
) = InsightsAttackDomainModel(
    start = at(start, zone),
    zone = zone,
    end = hours?.let { at(start, zone).plusSeconds(it * 3_600) },
    functionalImpact = impact,
    triggers = triggers
)

private fun intake(
    taken: String,
    name: String = "Ibuprofen",
    kind: InsightsMedicationKindDomainModel = ACUTE,
    effect: InsightsEffectDomainModel? = null,
    zone: ZoneId = TASHKENT
) = InsightsIntakeDomainModel(name, kind, at(taken, zone), zone, effect)

private fun records(
    attacks: List<InsightsAttackDomainModel> = emptyList(),
    intakes: List<InsightsIntakeDomainModel> = emptyList(),
    periodDates: Set<LocalDate> = emptySet()
) = InsightsRecordsDomainModel(attacks, intakes, periodDates)

private fun marks(vararg ranges: Pair<String, String>): Set<LocalDate> =
    ranges.flatMap { (from, to) ->
        generateSequence(date(from)) { it.plusDays(1) }.takeWhile { it <= date(to) }.toList()
    }.toSet()

/** Five migraines in the last three months, the first more than 30 days ago: enough for the sections. */
private val FIVE = listOf(
    attack("2026-08-03T09:00"),
    attack("2026-08-25T09:00"),
    attack("2026-09-14T09:00"),
    attack("2026-10-02T09:00"),
    attack("2026-10-21T09:00")
)

class InsightsCalculatorTest {
    private val classUnderTest = InsightsCalculator(THREE_MONTHS, NOW)

    @Test
    fun `The last months end today and start the day after the same date months before`() {
        assertEquals(InsightsDatesDomainModel(date("2026-07-26"), TODAY), THREE_MONTHS)
        assertEquals(InsightsDatesDomainModel(date("2026-04-26"), TODAY), lastMonthsOn(6, TODAY))
        assertEquals(InsightsDatesDomainModel(date("2025-10-26"), TODAY), lastMonthsOn(12, TODAY))
    }

    @Test
    fun `Given no migraines then five migraines and thirty days are missing`() {
        assertEquals(
            InsightsPatternsDomainModel.NotEnough(missingMigraines = 5, missingDays = 30),
            classUnderTest.patterns(records(), periodTracking = true)
        )
    }

    @Test
    fun `Given three migraines in the period, the first long ago, then two more migraines`() {
        assertEquals(
            InsightsPatternsDomainModel.NotEnough(missingMigraines = 2, missingDays = 0),
            classUnderTest.notEnough(records(FIVE.take(3)))
        )
    }

    @Test
    fun `Given five migraines, the first 29 days ago, then one more day, and at 30 days enough`() {
        val recent = listOf(
            attack("2026-09-26T09:00"),
            attack("2026-10-01T09:00"),
            attack("2026-10-08T09:00"),
            attack("2026-10-15T09:00"),
            attack("2026-10-24T09:00")
        )
        assertEquals(
            InsightsPatternsDomainModel.NotEnough(missingMigraines = 0, missingDays = 1),
            classUnderTest.notEnough(records(recent))
        )

        assertNull(classUnderTest.notEnough(records(recent + attack("2026-09-25T09:00"))))
    }

    @Test
    fun `A migraine before the period counts toward the days but not the migraines`() {
        val old = attack("2026-03-01T09:00")

        assertEquals(
            InsightsPatternsDomainModel.NotEnough(missingMigraines = 1, missingDays = 0),
            classUnderTest.notEnough(records(FIVE.take(4) + old))
        )
    }

    @Test
    fun `A migraine is in the period by its start date in the zone it was logged in`() {
        // 01:00 on the period's first day in Tokyo is 21:00 the day before in Tashkent: in the period.
        val tokyo = attack("2026-07-26T01:00", zone = TOKYO)
        // 23:00 the day before the period in Tashkent is already the first day in Tokyo: not in the period.
        val tashkent = attack("2026-07-25T23:00")

        assertEquals(
            InsightsPatternsDomainModel.NotEnough(missingMigraines = 4, missingDays = 0),
            classUnderTest.notEnough(records(listOf(tokyo, tashkent)))
        )
    }

    @Test
    fun `Given enough then the sections, each on the period's migraines`() {
        val patterns = classUnderTest.patterns(records(FIVE), periodTracking = true)

        assertTrue(patterns is InsightsPatternsDomainModel.Enough)
        patterns as InsightsPatternsDomainModel.Enough
        assertEquals(5, patterns.migraines)
        assertEquals(emptyList<Any>(), patterns.triggers)
        assertEquals(5, patterns.weekdays.days.sumOf { it.migraines })
        assertEquals(5, patterns.timesOfDay.times.sumOf { it.migraines })
        assertEquals(emptyList<Any>(), patterns.medications)
    }

    @Test
    fun `The five triggers logged with the most migraines come first, ties in a stable order`() {
        val weather = builtIn(InsightsBuiltInTriggerDomainModel.Weather)
        val caffeine = builtIn(InsightsBuiltInTriggerDomainModel.Caffeine)
        val sleep = builtIn(InsightsBuiltInTriggerDomainModel.TooLittleSleep)
        val attacks = listOf(
            listOf(STRESS, weather, custom("Long drive")),
            listOf(STRESS, caffeine, custom("anger")),
            listOf(STRESS, weather, caffeine, custom("Long drive")),
            listOf(STRESS, sleep, custom("anger")),
            listOf(STRESS, sleep),
            listOf(STRESS, builtIn(InsightsBuiltInTriggerDomainModel.Smell))
        ).mapIndexed { day, triggers ->
            attack("2026-10-${10 + day}T09:00", triggers = triggers)
        }

        // Five share 2: the built-ins in the app's order, then the user's own by name, so
        // "Long drive" falls at the fifth place.
        assertEquals(
            listOf(
                count(STRESS, 6),
                count(sleep, 2),
                count(caffeine, 2),
                count(weather, 2),
                count(custom("anger"), 2)
            ),
            classUnderTest.triggers(records(attacks), periodTracking = true)
        )
    }

    @Test
    fun `A tie at the fifth place goes to the built-in first in the app's order`() {
        val attack = attack(
            "2026-10-10T09:00",
            triggers = listOf(
                builtIn(InsightsBuiltInTriggerDomainModel.Travel),
                STRESS,
                custom("Zebra"),
                custom("apple"),
                builtIn(InsightsBuiltInTriggerDomainModel.Alcohol),
                PERIOD
            )
        )

        assertEquals(
            listOf(
                count(STRESS, 1),
                count(builtIn(InsightsBuiltInTriggerDomainModel.Alcohol), 1),
                count(PERIOD, 1),
                count(builtIn(InsightsBuiltInTriggerDomainModel.Travel), 1),
                count(custom("apple"), 1)
            ),
            classUnderTest.triggers(records(listOf(attack)), periodTracking = true)
        )
    }

    @Test
    fun `A trigger counts once a migraine, only for migraines started in the period`() {
        val attacks = listOf(
            attack("2026-10-10T09:00", triggers = listOf(STRESS, STRESS, custom("Long drive"))),
            attack(
                "2026-10-11T09:00",
                triggers = listOf(custom("long drive"), custom("Long drive"))
            ),
            attack("2026-10-12T09:00", triggers = listOf(custom("LONG DRIVE"), custom("Stress"))),
            attack("2026-07-20T09:00", triggers = listOf(custom("Long drive"), STRESS))
        )

        // The user's names that differ only in case are one trigger; one named like a built-in is
        // its own.
        assertEquals(
            listOf(
                count(custom("Long drive"), 3),
                count(STRESS, 1),
                count(custom("Stress"), 1)
            ),
            classUnderTest.triggers(records(attacks), periodTracking = true)
        )
        assertEquals(emptyList<Any>(), classUnderTest.triggers(records(FIVE), true))
    }

    @Test
    fun `Given Track periods is off then the built-in Period trigger is left out`() {
        val attacks = listOf(
            attack("2026-10-10T09:00", triggers = listOf(PERIOD, STRESS)),
            attack("2026-10-11T09:00", triggers = listOf(PERIOD)),
            attack("2026-10-12T09:00", triggers = listOf(custom("Period pain")))
        )

        assertEquals(
            listOf(count(PERIOD, 2), count(STRESS, 1), count(custom("Period pain"), 1)),
            classUnderTest.triggers(records(attacks), periodTracking = true)
        )
        assertEquals(
            listOf(count(STRESS, 1), count(custom("Period pain"), 1)),
            classUnderTest.triggers(records(attacks), periodTracking = false)
        )
        val enough = classUnderTest.patterns(records(FIVE + attacks), periodTracking = false)
        assertEquals(
            listOf(count(STRESS, 1), count(custom("Period pain"), 1)),
            (enough as InsightsPatternsDomainModel.Enough).triggers
        )
    }

    @Test
    fun `Weekdays count in the zone each migraine was logged in, and the busiest is named`() {
        val attacks = listOf(
            attack("2026-10-19T09:00"),
            attack("2026-10-12T09:00"),
            // A Monday in Tokyo, still Sunday in Tashkent.
            attack("2026-10-05T01:00", zone = TOKYO),
            attack("2026-10-20T09:00"),
            attack("2026-10-25T09:00")
        )

        assertEquals(
            InsightsWeekdaysDomainModel(
                listOf(3, 1, 0, 0, 0, 0, 1).mapIndexed { index, migraines ->
                    InsightsWeekdayDomainModel(DayOfWeek.of(index + 1), migraines)
                },
                busiest = DayOfWeek.MONDAY
            ),
            classUnderTest.weekdays(records(attacks))
        )
    }

    @Test
    fun `Given two days share the most then no day is named`() {
        val attacks = listOf(
            attack("2026-10-19T09:00"),
            attack("2026-10-12T09:00"),
            attack("2026-10-20T09:00"),
            attack("2026-10-13T09:00"),
            attack("2026-10-25T09:00")
        )

        val weekdays = classUnderTest.weekdays(records(attacks))

        assertNull(weekdays.busiest)
        assertEquals(2, weekdays.days.first { it.day == DayOfWeek.TUESDAY }.migraines)
    }

    @Test
    fun `Times of day split at 6, 12 and 18 in the zone each was logged in`() {
        val starts = listOf(
            "2026-10-01T00:00",
            "2026-10-02T05:59",
            "2026-10-03T06:00",
            "2026-10-04T11:59",
            "2026-10-05T12:00",
            "2026-10-06T18:00",
            "2026-10-07T23:59"
        )
        // 07:00 in Tokyo is 03:00 in Tashkent: a morning.
        val attacks = starts.map(::attack) + attack("2026-10-08T07:00", zone = TOKYO)

        assertEquals(
            InsightsTimesOfDayDomainModel(
                listOf(
                    InsightsTimeOfDayCountDomainModel(InsightsTimeOfDayDomainModel.Night, 2),
                    InsightsTimeOfDayCountDomainModel(InsightsTimeOfDayDomainModel.Morning, 3),
                    InsightsTimeOfDayCountDomainModel(InsightsTimeOfDayDomainModel.Afternoon, 1),
                    InsightsTimeOfDayCountDomainModel(InsightsTimeOfDayDomainModel.Evening, 2)
                ),
                busiest = InsightsTimeOfDayDomainModel.Morning
            ),
            classUnderTest.timesOfDay(records(attacks))
        )
        assertNull(classUnderTest.timesOfDay(records(starts.map(::attack))).busiest)
    }

    @Test
    fun `The cycle counts migraines from two days before a period through its third day`() {
        val periodDates = marks(
            "2026-08-10" to "2026-08-14",
            "2026-09-08" to "2026-09-12",
            "2026-10-06" to "2026-10-10"
        )
        val attacks = listOf(
            attack("2026-08-08T09:00"),
            attack("2026-08-12T09:00"),
            attack("2026-08-13T09:00"),
            attack("2026-09-05T09:00"),
            attack("2026-09-07T09:00"),
            attack("2026-10-20T09:00"),
            // 4 October in Tokyo, 3 October in Tashkent.
            attack("2026-10-04T01:00", zone = TOKYO)
        )

        assertEquals(
            InsightsCycleDomainModel(cycles = 3, migraines = 4, days = 15, periodDays = 92),
            classUnderTest.cycle(records(attacks, periodDates = periodDates))
        )
    }

    @Test
    fun `Windows at the period's edges count only their days within it`() {
        val periodDates = marks(
            // Starts the day before the period: not a cycle of the period, but its window reaches into it.
            "2026-07-25" to "2026-07-29",
            "2026-08-20" to "2026-08-24",
            // Starts today: its window ends today.
            "2026-10-25" to "2026-10-25"
        )
        val attacks = listOf(
            attack("2026-07-25T09:00"),
            attack("2026-07-26T09:00"),
            attack("2026-10-25T09:00")
        )

        assertEquals(
            InsightsCycleDomainModel(cycles = 2, migraines = 2, days = 2 + 5 + 3, periodDays = 92),
            classUnderTest.cycle(records(attacks, periodDates = periodDates))
        )
    }

    @Test
    fun `Overlapping windows count their days once`() {
        val periodDates = setOf(date("2026-08-10"), date("2026-08-13"))

        assertEquals(
            InsightsCycleDomainModel(cycles = 2, migraines = 0, days = 8, periodDays = 92),
            classUnderTest.cycle(records(periodDates = periodDates))
        )
    }

    @Test
    fun `Given fewer than two cycles in the period then no cycle`() {
        assertNull(classUnderTest.cycle(records(periodDates = marks("2026-08-10" to "2026-08-14"))))
        assertNull(
            classUnderTest.cycle(
                records(
                    periodDates = marks(
                        "2026-07-20" to "2026-07-24",
                        "2026-08-10" to "2026-08-14"
                    )
                )
            )
        )
        assertNull(classUnderTest.cycle(records()))
    }

    @Test
    fun `A cycle that starts three days before the period adds no days to it`() {
        val inside = arrayOf("2026-08-10" to "2026-08-10", "2026-09-08" to "2026-09-08")

        assertEquals(
            InsightsCycleDomainModel(cycles = 2, migraines = 0, days = 10, periodDays = 92),
            classUnderTest.cycle(
                records(periodDates = marks("2026-07-23" to "2026-07-23", *inside))
            )
        )
        // Two days before, its window reaches the period's first day.
        assertEquals(
            InsightsCycleDomainModel(cycles = 2, migraines = 0, days = 11, periodDays = 92),
            classUnderTest.cycle(
                records(periodDates = marks("2026-07-24" to "2026-07-24", *inside))
            )
        )
    }

    @Test
    fun `Given Track periods is off then the sections say nothing about periods`() {
        val periodDates = marks("2026-08-02" to "2026-08-06", "2026-09-13" to "2026-09-17")

        val off = classUnderTest.patterns(records(FIVE, periodDates = periodDates), false)
        val on = classUnderTest.patterns(records(FIVE, periodDates = periodDates), true)

        assertNull((off as InsightsPatternsDomainModel.Enough).cycle)
        assertEquals(
            InsightsCycleDomainModel(cycles = 2, migraines = 2, days = 10, periodDays = 92),
            (on as InsightsPatternsDomainModel.Enough).cycle
        )
    }

    @Test
    fun `Attack medications in the period show what helped once they have three answers`() {
        val ibuprofen = listOf(A_LOT, A_LOT, A_LOT, A_LOT, COMPLETELY, COMPLETELY, COMPLETELY) +
            listOf(InsightsEffectDomainModel.ALittle, NOT_AT_ALL, null)
        val intakes = ibuprofen.mapIndexed { day, effect ->
            intake("2026-09-${10 + day}T09:00", effect = effect)
        } + listOf(
            intake("2026-10-01T09:00", "Sumatriptan", effect = A_LOT),
            intake("2026-10-02T09:00", "Sumatriptan"),
            // The period's first day in Tokyo, the day before it in Tashkent: in the period.
            intake("2026-07-26T01:00", "Sumatriptan", effect = COMPLETELY, zone = TOKYO),
            // Before the period.
            intake("2026-07-25T09:00", "Sumatriptan", effect = A_LOT),
            intake("2026-10-03T09:00", "Aspirin", effect = NOT_AT_ALL),
            intake("2026-10-04T09:00", "Aspirin", effect = NOT_AT_ALL),
            intake("2026-10-05T09:00", "Aspirin", effect = NOT_AT_ALL)
        ) + (1..5).map {
            intake(
                "2026-10-0${it}T08:00",
                "Propranolol",
                InsightsMedicationKindDomainModel.Preventive,
                A_LOT
            )
        }

        assertEquals(
            listOf(
                InsightsMedicationDomainModel.Rated("Ibuprofen", helped = 7, answered = 9),
                InsightsMedicationDomainModel.Rated("Aspirin", helped = 0, answered = 3),
                InsightsMedicationDomainModel.NeedsAnswers("Sumatriptan", answered = 2)
            ),
            classUnderTest.medications(records(intakes = intakes))
        )
    }

    @Test
    fun `The trends are the last 12 calendar months, this one included`() {
        val trends = classUnderTest.trends(records())

        assertEquals(
            (11L downTo 0L).map { months ->
                InsightsMonthDomainModel(YearMonth.of(2026, 10).minusMonths(months), 0, 0, 0, 0)
            },
            trends
        )
        assertEquals(YearMonth.of(2025, 11), trends.first().month)
    }

    @Test
    fun `Each month counts its migraine days, migraines, medication days and bed-rest migraines`() {
        val attacks = listOf(
            // Touches 30 September and 1 October.
            attack("2026-09-30T22:00", hours = 5),
            attack("2026-10-01T10:00"),
            // Ends at midnight exactly: only 5 October.
            attack("2026-10-05T20:00", hours = 4),
            // In progress: 23 October to today.
            attack("2026-10-23T10:00", hours = null),
            // Before the 12 months.
            attack("2025-10-31T10:00", impact = 3),
            attack("2025-11-01T10:00", impact = 3),
            // 1 June in Tokyo, 31 May in Tashkent.
            attack("2026-06-01T01:00", impact = 3, zone = TOKYO),
            attack("2026-06-10T10:00", impact = 2)
        )
        val intakes = listOf(
            intake("2026-10-01T09:00"),
            intake("2026-10-01T15:00", "Sumatriptan"),
            intake("2026-10-02T09:00"),
            intake("2026-10-03T09:00", "Propranolol", InsightsMedicationKindDomainModel.Preventive),
            intake("2026-06-01T01:00", zone = TOKYO)
        )

        val trends = classUnderTest.trends(records(attacks, intakes)).associateBy { it.month }

        assertEquals(
            InsightsMonthDomainModel(YearMonth.of(2026, 10), 5, 3, 2, 0),
            trends[YearMonth.of(2026, 10)]
        )
        assertEquals(
            InsightsMonthDomainModel(YearMonth.of(2026, 9), 1, 1, 0, 0),
            trends[YearMonth.of(2026, 9)]
        )
        assertEquals(
            InsightsMonthDomainModel(YearMonth.of(2026, 6), 2, 2, 1, 1),
            trends[YearMonth.of(2026, 6)]
        )
        assertEquals(
            InsightsMonthDomainModel(YearMonth.of(2026, 5), 0, 0, 0, 0),
            trends[YearMonth.of(2026, 5)]
        )
        assertEquals(
            InsightsMonthDomainModel(YearMonth.of(2025, 11), 1, 1, 0, 1),
            trends[YearMonth.of(2025, 11)]
        )
        assertEquals(12, trends.size)
    }
}
