package com.migrely.app.reminders.domain.usecase

import com.migrely.app.reminders.domain.model.CurrentTimeDomainModel
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

private val TASHKENT = ZoneId.of("Asia/Tashkent")
private val BERLIN = ZoneId.of("Europe/Berlin")
private val NEW_YORK = ZoneId.of("America/New_York")
private val EIGHT_PM = LocalTime.of(20, 0)
private val TODAY = LocalDate.of(2026, 10, 24)

class DailyReminderTimeTest {
    private val classUnderTest = DailyReminderTime()

    private fun now(local: String, zone: ZoneId = TASHKENT) =
        CurrentTimeDomainModel(LocalDateTime.parse(local).atZone(zone).toInstant(), zone)

    private fun at(local: String, zone: ZoneId = TASHKENT) =
        LocalDateTime.parse(local).atZone(zone).toInstant()

    @Test
    fun `Given the time is still ahead today when planning then today`() {
        assertEquals(at("2026-10-24T20:00"), classUnderTest.next(now("2026-10-24T15:00"), EIGHT_PM))
    }

    @Test
    fun `Given the time has passed or is now when planning then tomorrow`() {
        assertEquals(at("2026-10-25T20:00"), classUnderTest.next(now("2026-10-24T20:30"), EIGHT_PM))
        assertEquals(at("2026-10-25T20:00"), classUnderTest.next(now("2026-10-24T20:00"), EIGHT_PM))
    }

    @Test
    fun `Given a run that came early then the next day, not later today`() {
        assertEquals(
            at("2026-10-25T20:00"),
            classUnderTest.after(
                java.time.LocalDate.of(2026, 10, 24),
                now("2026-10-24T19:50"),
                EIGHT_PM
            )
        )
    }

    @Test
    fun `Given a run that came a day late then the next time from now, so no day is lost`() {
        // The run for 23 October arrives at 21:00 on 24 October.
        assertEquals(
            at("2026-10-25T20:00"),
            classUnderTest.after(
                java.time.LocalDate.of(2026, 10, 23),
                now("2026-10-24T21:00"),
                EIGHT_PM
            )
        )
    }

    @Test
    fun `Given the phone is in Berlin when planning then 8 PM Berlin time`() {
        assertEquals(
            at("2026-10-24T20:00", BERLIN),
            classUnderTest.next(now("2026-10-24T10:00", BERLIN), EIGHT_PM)
        )
    }

    @Test
    fun `Given a time skipped by the clocks going forward when planning then the hour after`() {
        // Berlin skips 02:00–03:00 on 29 March 2026.
        assertEquals(
            at("2026-03-29T03:30", BERLIN),
            classUnderTest.next(now("2026-03-28T23:00", BERLIN), LocalTime.of(2, 30))
        )
    }

    @Test
    fun `Given the time is still ahead when a start plans then today at the time`() {
        assertEquals(
            at("2026-10-24T20:00"),
            classUnderTest.due(now("2026-10-24T15:00"), EIGHT_PM, handledOn = null)
        )
    }

    @Test
    fun `Given the time passed, today not handled when a start plans then now, for today`() {
        val now = now("2026-10-24T20:01")

        val due = classUnderTest.due(now, EIGHT_PM, handledOn = TODAY.minusDays(1))

        assertEquals(now.instant, due)
        assertEquals(TODAY, classUnderTest.dateOf(due, TASHKENT))
    }

    @Test
    fun `Given the time has passed and today handled when a start plans then tomorrow`() {
        assertEquals(
            at("2026-10-25T20:00"),
            classUnderTest.due(now("2026-10-24T20:01"), EIGHT_PM, handledOn = TODAY)
        )
    }

    @Test
    fun `Given exactly the time, today not handled when a start plans then now, for today`() {
        val now = now("2026-10-24T20:00")

        val due = classUnderTest.due(now, EIGHT_PM, handledOn = null)

        assertEquals(now.instant, due)
        assertEquals(TODAY, classUnderTest.dateOf(due, TASHKENT))
    }

    @Test
    fun `Given a time skipped by DST when a start plans then the moved time, or now`() {
        // Berlin skips 02:00–03:00 on 29 March 2026, so 02:30 that day is 03:30.
        val skipped = LocalTime.of(2, 30)
        assertEquals(
            at("2026-03-29T03:30", BERLIN),
            classUnderTest.due(now("2026-03-29T01:30", BERLIN), skipped, handledOn = null)
        )
        val late = now("2026-03-29T04:00", BERLIN)
        assertEquals(late.instant, classUnderTest.due(late, skipped, handledOn = null))
    }

    @Test
    fun `Given the clock moved back past the handled date when a start plans then today's rule`() {
        // Handled on 25 October, then the clock was set back to 24 October.
        val handledOn = TODAY.plusDays(1)
        assertEquals(
            at("2026-10-24T20:00"),
            classUnderTest.due(now("2026-10-24T15:00"), EIGHT_PM, handledOn)
        )
        val now = now("2026-10-24T21:00")
        assertEquals(now.instant, classUnderTest.due(now, EIGHT_PM, handledOn))
    }

    @Test
    fun `Given a run after the time when asked how late then the minutes since that date's time`() {
        fun lateAt(local: String) = classUnderTest.lateBy(TODAY, now(local), EIGHT_PM)

        assertEquals(Duration.ofMinutes(59), lateAt("2026-10-24T20:59"))
        assertEquals(Duration.ofMinutes(60), lateAt("2026-10-24T21:00"))
        assertEquals(Duration.ofMinutes(61), lateAt("2026-10-24T21:01"))
        assertEquals(Duration.ofMinutes(190), lateAt("2026-10-24T23:10"))
    }

    @Test
    fun `Given the clock is before the time when asked how late then negative`() {
        assertEquals(
            Duration.ofMinutes(-60),
            classUnderTest.lateBy(TODAY, now("2026-10-24T19:00"), EIGHT_PM)
        )
    }

    @Test
    fun `Given a time skipped by DST when asked how late then read from where the plan put it`() {
        // Berlin skips 02:00–03:00 on 29 March 2026, so 02:30 that day is 03:30.
        assertEquals(
            Duration.ofMinutes(5),
            classUnderTest.lateBy(
                LocalDate.of(2026, 3, 29),
                now("2026-03-29T03:35", BERLIN),
                LocalTime.of(2, 30)
            )
        )
    }

    @Test
    fun `Given a repeated hour by DST when asked how late at the second time then 60 minutes`() {
        // New York repeats 01:00–02:00 on 1 November 2026; the plan takes the first 01:30.
        val secondHalfPastOne = LocalDateTime.parse("2026-11-01T01:30")
            .atZone(NEW_YORK)
            .withLaterOffsetAtOverlap()
            .toInstant()

        assertEquals(
            Duration.ofMinutes(60),
            classUnderTest.lateBy(
                LocalDate.of(2026, 11, 1),
                CurrentTimeDomainModel(secondHalfPastOne, NEW_YORK),
                LocalTime.of(1, 30)
            )
        )
    }

    @Test
    fun `Given the phone moved to another zone when asked how late then read in the new zone`() {
        // 23:00 in Berlin is 02:00 on 25 October in Tashkent; Berlin's 8 PM was 3 hours ago.
        assertEquals(
            Duration.ofHours(3),
            classUnderTest.lateBy(TODAY, now("2026-10-24T23:00", BERLIN), EIGHT_PM)
        )
    }
}
