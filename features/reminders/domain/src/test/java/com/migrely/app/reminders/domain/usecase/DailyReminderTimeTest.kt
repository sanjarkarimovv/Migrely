package com.migrely.app.reminders.domain.usecase

import com.migrely.app.reminders.domain.model.CurrentTimeDomainModel
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

private val TASHKENT = ZoneId.of("Asia/Tashkent")
private val BERLIN = ZoneId.of("Europe/Berlin")
private val EIGHT_PM = LocalTime.of(20, 0)

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
    fun `Given a run that came early when planning the next then tomorrow, not later today`() {
        assertEquals(
            at("2026-10-25T20:00"),
            classUnderTest.tomorrow(now("2026-10-24T19:50"), EIGHT_PM)
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
}
