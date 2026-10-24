package com.migrely.app.attack.domain.usecase

import com.migrely.app.attack.domain.model.CurrentTimeDomainModel
import com.migrely.app.attack.domain.model.PastAttackDraftDomainModel
import com.migrely.app.attack.domain.repository.GetCurrentTimeRepository
import com.migrely.app.coroutine.CoroutineContextProvider
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock

private val ZONE: ZoneId = ZoneId.of("Asia/Tashkent")

class GetPastAttackDraftUseCaseTest {
    private val getCurrentTimeRepository: GetCurrentTimeRepository = mock()
    private val classUnderTest = GetPastAttackDraftUseCase(
        getCurrentTimeRepository,
        CoroutineContextProvider.Default
    )

    private fun givenNow(local: String) {
        given(getCurrentTimeRepository.currentTime()).willReturn(
            CurrentTimeDomainModel(LocalDateTime.parse(local).atZone(ZONE).toInstant(), ZONE)
        )
    }

    private fun draft(start: String, end: String) = PastAttackDraftDomainModel(
        start = LocalDateTime.parse(start).atZone(ZONE).toInstant(),
        end = LocalDateTime.parse(end).atZone(ZONE).toInstant(),
        zone = ZONE
    )

    @Test
    fun `Given seconds past the minute when drafting then two hours to now, rounded down`() {
        given(getCurrentTimeRepository.currentTime()).willReturn(
            CurrentTimeDomainModel(Instant.parse("2026-10-23T11:42:30.250Z"), ZONE)
        )

        val actual = classUnderTest.executeInBackground(null)

        assertEquals(
            PastAttackDraftDomainModel(
                start = Instant.parse("2026-10-23T09:42:00Z"),
                end = Instant.parse("2026-10-23T11:42:00Z"),
                zone = ZONE
            ),
            actual
        )
    }

    @Test
    fun `Given today is chosen when drafting then the usual defaults`() {
        givenNow("2026-10-24T17:20:30")

        val actual = classUnderTest.executeInBackground(LocalDate.parse("2026-10-24"))

        assertEquals(draft("2026-10-24T15:20", "2026-10-24T17:20"), actual)
    }

    @Test
    fun `Given a future day when drafting then the usual defaults`() {
        givenNow("2026-10-24T17:20")

        val actual = classUnderTest.executeInBackground(LocalDate.parse("2026-10-26"))

        assertEquals(draft("2026-10-24T15:20", "2026-10-24T17:20"), actual)
    }

    @Test
    fun `Given a past day when drafting then the usual clock times on that day`() {
        givenNow("2026-10-24T17:20:30")

        val actual = classUnderTest.executeInBackground(LocalDate.parse("2026-10-20"))

        assertEquals(draft("2026-10-20T15:20", "2026-10-20T17:20"), actual)
    }

    @Test
    fun `Given yesterday late at night when drafting then the end is capped at now`() {
        givenNow("2026-10-24T01:10")

        val actual = classUnderTest.executeInBackground(LocalDate.parse("2026-10-23"))

        assertEquals(draft("2026-10-23T23:10", "2026-10-24T01:10"), actual)
    }

    @Test
    fun `Given two days ago late at night when drafting then the end runs into the next day`() {
        givenNow("2026-10-24T01:10")

        val actual = classUnderTest.executeInBackground(LocalDate.parse("2026-10-22"))

        assertEquals(draft("2026-10-22T23:10", "2026-10-23T01:10"), actual)
    }
}
